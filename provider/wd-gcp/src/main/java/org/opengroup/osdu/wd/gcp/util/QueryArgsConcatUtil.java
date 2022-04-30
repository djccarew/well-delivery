/*
 *  Copyright 2020-2021 Google LLC
 *  Copyright 2020-2021 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.wd.gcp.util;

import java.util.List;

public class QueryArgsConcatUtil {

    private QueryArgsConcatUtil() {
    }

    public static String buildEntityTypeParam(String value) {
        return "{\"entityType\":\"" + value.toLowerCase() + "\"}";
    }

    public static String buildEntityIdParam(String value) {
        return "{\"entityId\":\"" + value.toLowerCase() + "\"}";
    }

    public static String buildExistenceKindParam(String value) {
        return "{\"existenceKind\":\"" + value.toLowerCase() + "\"}";
    }

    public static String buildRelationshipEntityTypeParam(String value) {
        return "[{\"entityType\":\"" + value.toLowerCase() + "\"}]";
    }

    public static String buildVersionParam(long value) {
        return "{\"version\":" + value + "}";
    }

    public static String buildStartTimeParam(String value) {
        return "{\"startTime\":\"" + value + "\"}";
    }

    public static String buildEndTimeParam(String value) {
        return "{\"endTime\":\"" + value + "\"}";
    }

    public static String buildFacilityNameParam(String value) {
        return "{\"data\":{\"FacilityName\":\"" + value + "\"}}";
    }

    public static String buildRelationshipIdParam(String value) {
        return value.toLowerCase() + "%";
    }

    public static String[] buildRelationshipMultipleIdsParam(List<String> value) {
        return value.stream().map(id -> id + "%").toArray(String[]::new);
    }


}
