/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.test;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.BErrorCreator;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Native implementation of assertValueEqual(anydata expected, anydata actual).
 *
 * @since 1.3.0
 */
public class AssertValueEqual {
    public static void assertValueEqual(Object expected, Object actual) {
        if (!TypeChecker.isEqual(expected, actual)) {
            BString reason = BStringUtils.fromString("{ballerina/lang.test}AssertionError");
            BString msg = BStringUtils
                    .fromString("expected " + expected.toString() + " but found " + actual.toString());
            throw BErrorCreator.createError(reason, msg);
        }
    }
}
