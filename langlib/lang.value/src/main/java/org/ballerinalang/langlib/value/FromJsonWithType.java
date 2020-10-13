/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langlib.value;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.TypeConverter;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.BErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.values:fromJsonWithType.
 *
 * @since 2.0
 */
public class FromJsonWithType {
    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object fromJsonWithType(Object v, BTypedesc t) {
        Type describingType = t.getDescribingType();
        try {
            return convert(v, describingType, new ArrayList<>(), t);
        } catch (BError e) {
            return e;
        } catch (BallerinaException e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                               BStringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t) {

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        Type sourceType = TypeChecker.getType(value);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, sourceType).getValue());
        }

        unresolvedValues.add(typeValuePair);

        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            throw createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        List<Type> convertibleTypes = TypeConverter.getConvertibleTypesFromJson(value, targetType, new ArrayList<>());
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        Type matchingType = convertibleTypes.get(0);

        Object newValue;
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((BMap<?, ?>) value, matchingType, unresolvedValues, t);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((BArray) value, matchingType, unresolvedValues, t);
                break;
            default:
                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    try {
                        newValue = XMLFactory.parse(((BString) value).getValue());
                        break;
                    } catch (Throwable e) {
                        throw createConversionError(value, targetType, e.getMessage());
                    }
                }

                // handle primitive values
                if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
                    if (TypeChecker.checkIsType(value, matchingType)) {
                        newValue = value;
                    } else {
                        // Has to be a numeric conversion.
                        newValue = TypeConverter.convertValues(matchingType, value);
                    }
                    break;
                }
                // should never reach here
                throw ErrorUtils.createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, List<TypeValuePair> unresolvedValues,
                                     BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                BMap<BString, Object> newMap = BValueCreator.createMapValue(targetType);
                Type constraintType = ((BMapType) targetType).getConstrainedType();
                for (Map.Entry entry : map.entrySet()) {
                    putToMap(newMap, entry, constraintType, unresolvedValues, t);
                }
                return newMap;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) targetType;
                BMap<BString, Object> newRecord;
                if (t.getDescribingType() == targetType) {
                    newRecord = (BMap<BString, Object>) t.instantiate(Scheduler.getStrand());
                } else {
                    newRecord = (BMap<BString, Object>) BValueCreator
                            .createRecordValue(recordType.getPackage(), recordType.getName());
                }

                Type restFieldType = recordType.restFieldType;
                Map<String, Type> targetTypeField = new HashMap<>();
                for (BField field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }

                for (Map.Entry entry : map.entrySet()) {
                    Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
                    putToMap(newRecord, entry, fieldType, unresolvedValues, t);
                }
                return newRecord;
            case TypeTags.JSON_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t);
        }
        // should never reach here
        throw ErrorUtils.createConversionError(map, targetType);
    }


    private static Object convertArray(BArray array, Type targetType, List<TypeValuePair> unresolvedValues,
                                       BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                BArrayType arrayType = (BArrayType) targetType;
                BArray newArray = BValueCreator.createArrayValue(arrayType);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues, t);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) targetType;
                BArray newTuple = BValueCreator.createTupleValue(tupleType);
                int minLen = tupleType.getTupleTypes().size();
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues, t);
                    newTuple.add(i, newValue);
                }
                return newTuple;
            case TypeTags.JSON_TAG:
                newArray = BValueCreator.createArrayValue((BArrayType) Types.TYPE_JSON_ARRAY);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), targetType, unresolvedValues, t);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TABLE_TAG:
                BTableType tableType = (BTableType) targetType;
                BTable newTable = BValueCreator.createTableValue(tableType);
                for (int i = 0; i < array.size(); i++) {
                    BMap bMap = (BMap) convert(array.get(i), tableType.getConstrainedType(),
                            unresolvedValues, t);
                    newTable.add(bMap);
                }
                return newTable;
        }
        // should never reach here
        throw ErrorUtils.createConversionError(array, targetType);
    }

    private static void putToMap(BMap<BString, Object> map, Map.Entry entry, Type fieldType,
                                 List<TypeValuePair> unresolvedValues, BTypedesc t) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, t);
        map.put(BStringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static BError createConversionError(Object inputValue, Type targetType) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType));
    }

    private static BError createConversionError(Object inputValue, Type targetType, String detailMessage) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR, BLangExceptionHelper.getErrorMessage(
                INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                .concat(BStringUtils.fromString(": ".concat(detailMessage))));
    }
}
