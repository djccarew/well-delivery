// Copyright 2020 Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.wd.test.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.Map;

public class EntityUtil {

    public static long create(String type, String id, String body, String token)
            throws Exception {
        String path = String.format("storage/v1/%s/", type.toLowerCase());
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "PUT", headers, body, "");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_CREATED) {
            String message = getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
        Thread.sleep(100);
        long version = getVersion(response, type, id);
        System.out.println(String.format("created entity: %s:%d", id, version));
        return version;
    }

    public static long getEntity(String type, String id, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s", type, id);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "GET", headers, "","");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = getVersion(response, type, id);
        return version;
    }

    public static ClientResponse getVersionNumbers(String type, String id, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s", type, id);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "GET", headers, "","");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        return response;
    }

    public static ClientResponse deleteEntity(String type, String id, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s", type, id);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "DELETE", headers, "", "");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_NO_CONTENT) {
            String message = getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        System.out.println(String.format("deleted entity: %s", id));
        return response;
    }

    public static ClientResponse purgeEntity(String type, String id, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s:purge", type, id);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "DELETE", headers, "", "");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_NO_CONTENT) {
            String message = getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        System.out.println(String.format("purged entity: %s", id));
        return response;
    }

    public static ClientResponse getEntityVersion(String type, String id, long version, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s/%d", type, id, version);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "GET", headers, "","");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long retVersion = getVersion(response, type, id);
        Assert.assertEquals(version, retVersion);
        return response;
    }

    public static ClientResponse deleteEntityVersion(String type, String id, long version, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s/%d", type, id, version);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "DELETE", headers, "", "");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_NO_CONTENT) {
            String message = getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        System.out.println(String.format("deleted entity version: %s:%d", id, version));
        return response;
    }

    public static ClientResponse purgeEntityVersion(String type, String id, long version, String token) throws Exception {
        String path = String.format("storage/v1/%s/%s/%d:purge", type, id, version);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(getEntityUrl(), path, "DELETE", headers, "", "");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_NO_CONTENT) {
            String message = getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        System.out.println(String.format("purged entity version: %s:%d", id, version));
        return response;
    }


    public static String getEntityUrl() throws Exception {
        String entityUrl = System.getProperty("ENTITY_URL", System.getenv("ENTITY_URL"));
        if (entityUrl == null || entityUrl.contains("-null")) {
            entityUrl = "https://localhost:8443/api/v1/";
        }
        return entityUrl;
    }

    public static String buildWellActivityProgram(String id, String kind, String legalTag, String existenceKind,
                                                  String wellboreId, String planId) {
        JsonObject data = new JsonObject();
        JsonArray arrObj = new JsonArray();
        JsonObject phase = new JsonObject();
        phase.addProperty("WellboreID", wellboreId);
        phase.addProperty("ActivityPlanID", planId);
        arrObj.add(phase);
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.add("Phases", arrObj);
        JsonObject body = buildBody("WellActivityProgram", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildActivityPlan(String id, String schemaID, String legalTag, String existenceKind,
                                      String wellboreId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        JsonObject body = buildBody("ActivityPlan", id, schemaID, legalTag, data);
        return body.toString();
    }

    public static String buildWell(String id, String kind, String legalTag, String existenceKind,
                                   String wellName) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("FacilityName", wellName);
        JsonObject body = buildBody("Well", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildWellbore(String id, String kind, String legalTag, String existenceKind,
                                      String wellId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellID", wellId);
        data.addProperty("Name","Demo wellbore name");
        JsonObject body = buildBody("Wellbore", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildWellboreTrajectory(String id, String kind, String legalTag, String existenceKind,
                                      String wellboreId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        JsonObject body = buildBody("WellboreTrajectory", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildSection(String id, String kind, String legalTag, String existenceKind,
                                      String wellboreId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        JsonObject body = buildBody("HoleSection", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildBHARun(String id, String kind, String legalTag, String existenceKind,
                                      String wellboreId, String segmentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        data.addProperty("WellboreSegmentID", segmentId);
        data.addProperty("Name","Demo BDArun name");
        JsonObject body = buildBody("BHARun", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildDrillingReport(String id, String kind, String legalTag, String existenceKind,
                                     String wellboreId, String startTime, String endTime) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        data.addProperty("StartDateTime", startTime);
        data.addProperty("EndDateTime", endTime);
        JsonObject body = buildBody("DrillingReport", id, kind, legalTag, data);
        return body.toString();
    }

    public static String buildFluidsReport(String id, String kind, String legalTag, String existenceKind,
                                     String wellboreId) {
        JsonObject data = new JsonObject();
        data.addProperty("ExistenceKind", "namespace:reference-data--ExistenceKind:" + existenceKind + ":");
        data.addProperty("WellboreID", wellboreId);
        JsonObject body = buildBody("FluidsReport", id, kind, legalTag, data);
        return body.toString();
    }

    private static JsonObject buildBody(String type, String id, String kind, String legalTag, JsonObject data) {
        String acl = TestUtils.getAcl();
        JsonObject entity = new JsonObject();
        entity.addProperty("id", String.format("namespace:master-data--%s:%s", type, id));
        entity.addProperty("kind", kind);
        entity.add("acl", buildAcl(acl));
        entity.add("legal", buildLegal(legalTag));
        entity.add("data", data);
        return entity;
    }

    private static JsonObject buildAcl(String acl)
    {
        JsonObject aclObj = new JsonObject();
        JsonArray aclArray = new JsonArray();
        aclArray.add(acl);
        aclObj.add("viewers", aclArray);
        aclObj.add("owners", aclArray);
        return aclObj;
    }

    private static JsonObject buildLegal(String legalTag)
    {
        JsonArray tagsArray = new JsonArray();
        tagsArray.add(legalTag);

        JsonArray ordcArray = new JsonArray();
        ordcArray.add("US");

        JsonObject legalObj = new JsonObject();
        legalObj.add("legaltags", tagsArray);
        legalObj.add("otherRelevantDataCountries", ordcArray);
        return  legalObj;
    }

    private static JsonArray buildArray(String[] arr) {
        JsonArray arrObj = new JsonArray();
        for (String item : arr)
            arrObj.add(item);
        return arrObj;
    }

    public static long getVersion(ClientResponse response, String type, String id)
    {
        String idType = String.format("namespace:master-data--%s:%s", type, id);
        Gson gson = new Gson();
        EnitityMock entity = gson.fromJson(response.getEntity(String.class), EntityUtil.EnitityMock.class);
        Assert.assertEquals(entity.id, idType);
        return entity.version;
    }

    public static String getMessage(ClientResponse response)
    {
        Gson gson = new Gson();
        MessageMock mock = gson.fromJson(response.getEntity(String.class), EntityUtil.MessageMock.class);
        return mock.message;
    }

    public class EnitityMock {
        public String id;
        public long version;
    }

    public class MessageMock {
        public String message;
    }

    public class ReferenceTreeMock {
        public String id;
        public long version;
        public String type;
    }
}
