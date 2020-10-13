/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.array;

import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BUnionType;
import org.ballerinalang.langlib.array.utils.GetFunction;

import java.util.Arrays;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:enumerate(Type[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "enumerate",
//        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.TUPLE)},
//        isPublic = true
//)
public class Enumerate {

    public static BArray enumerate(BArray arr) {
        Type arrType = arr.getType();
        int size = arr.size();
        BTupleType elemType;
        GetFunction getFn;

        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                elemType = new BTupleType(Arrays.asList(Types.TYPE_INT, arr.getElementType()));
                getFn = BArray::get;
                break;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) arrType;
                BUnionType tupElemType = new BUnionType(tupleType.getTupleTypes(), tupleType.getTypeFlags());
                elemType = new BTupleType(Arrays.asList(Types.TYPE_INT, tupElemType));
                getFn = BArray::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "enumerate()");
        }

        BArrayType newArrType = new BArrayType(elemType);
        BArray newArr = BValueCreator.createArrayValue(newArrType); // TODO: 7/8/19 Verify whether this needs to be
        // sealed

        for (int i = 0; i < size; i++) {
            BArray entry = BValueCreator.createTupleValue(elemType);
            entry.add(0, Long.valueOf(i));
            entry.add(1, getFn.get(arr, i));
            newArr.add(i, entry);
        }

        return newArr;
    }
}
