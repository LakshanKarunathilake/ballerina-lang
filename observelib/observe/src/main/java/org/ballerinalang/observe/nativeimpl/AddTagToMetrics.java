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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.jvm.api.BErrorCreator;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.metrics.Tag;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_CUSTOM_METRIC_TAGS;
import org.ballerinalang.jvm.scheduling.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.jvm.observability.tracer.TraceConstants.KEY_CUSTOM_METRIC_TAGS;

/**
 * This function adds tags to a span.
 */
public class AddTagToMetrics {
    private static final OpenTracerBallerinaWrapper otWrapperInstance = OpenTracerBallerinaWrapper.getInstance();

    public static Object addTagToMetrics(BString tagKey, BString tagValue) {
        Optional<ObserverContext> observer = ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());


        Map<String, Tag> customTags = new HashMap<String, Tag>();
        boolean tagAdded = false;
        customTags.put(tagKey.getValue(), Tag.of(tagKey.getValue(), tagValue.getValue()));
        if (observer.isPresent()) {
            observer.get().addProperty(KEY_CUSTOM_METRIC_TAGS, customTags);
            tagAdded =  true;
        }

        if (tagAdded) {
            return null;
        }

        // TODO : Fix error msg
        return BErrorCreator.createError(
                BStringUtils.fromString(("Can not add tag {" + tagKey + ":" + tagValue + "}")));
    }
}
