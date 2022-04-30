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
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;

import static org.junit.Assert.assertTrue;

public class SchemaUtil {

    public static ClientResponse create(String body, String token) throws Exception {
        ClientResponse response = TestUtils.send(getSchemaUrl(), "schema", "PUT", HeaderUtils.getHeaders(TenantUtils.getTenantName(), token), body,"");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        assertTrue((response.getStatus() == HttpStatus.SC_OK) || (response.getStatus() == HttpStatus.SC_CREATED));
        Thread.sleep(100);
        SchemaMock schema = getSchema(response);
        System.out.println(String.format("created schema %s.", schema.schemaIdentity.id));
        return response;
    }

    protected static String getSchemaUrl() {
        String schemaUrl = System.getProperty("SCHEMA_URL", System.getenv("SCHEMA_URL"));
        if (schemaUrl == null || schemaUrl.contains("-null")) {
            schemaUrl = "https://os-schema-dot-opendes.appspot.com/api/schema/v1/";
        }
        return schemaUrl;
    }

    public static String buildWellActivityProgram() {
        JsonObject phasesObj = new JsonObject();
        phasesObj.addProperty("type", "array");
        phasesObj.add("items", buildPhaseObject());

        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("RigID", buildMasterData("Rig ID", "Rig"));
        properties.add("Phases", phasesObj);
        JsonObject data = buildData(properties);
        JsonObject wapObj = buildBody("WellActivityProgram", data);
        return wapObj.toString();
    }

    private static JsonObject buildPhaseObject()
    {
        JsonObject properties = new JsonObject();
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        properties.add("ActivityPlanID", buildMasterData("ActivityPlan ID", "ActivityPlan"));

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");
        obj.add("properties", properties);
        return obj;
    }

    public static String buildActivityPlan()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("ActivityPlan", data);
        return obj.toString();
    }

    public static String buildWell()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("Name",buildSimple("Well Name", "string"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("Well", data);
        return obj.toString();
    }

    public static String buildWellbore()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellID", buildMasterData("Well ID", "Well"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("Wellbore", data);
        return obj.toString();
    }

    public static String buildWellboreTrajectory()
    {
        JsonObject properties = new JsonObject();
        JsonObject data = buildData(properties);
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        JsonObject obj = buildBody("WellboreTrajectory", data);
        return obj.toString();
    }

    public static String buildSection()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("HoleSection", data);
        return obj.toString();
    }

    public static String buildBHARun()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        properties.add("HoleSectionID", buildMasterData("Hole Section ID", "HoleSection"));
        properties.add("Name",buildSimple("Name", "string"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("BHARun", data);
        return obj.toString();
    }

    public static String buildDrillingReport()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("DrillingReport", data);
        return obj.toString();
    }

    public static String buildFluidsReport()
    {
        JsonObject properties = new JsonObject();
        properties.add("ExistenceKind", buildRefData("Existence Kind", "ExistenceKind"));
        properties.add("WellboreID", buildMasterData("Wellbore ID", "Wellbore"));
        JsonObject data = buildData(properties);
        JsonObject obj = buildBody("FluidsReport", data);
        return obj.toString();
    }

    private static JsonObject buildBody(String type, JsonObject data) {
        JsonObject identity = new JsonObject();
        identity.addProperty("authority", "slb");
        identity.addProperty("source", "well-delivery");
        identity.addProperty("entityType", type.toLowerCase());
        identity.addProperty("schemaVersionMajor", 3);
        identity.addProperty("schemaVersionMinor", 0);
        identity.addProperty("schemaVersionPatch", 0);

        JsonObject info = new JsonObject();
        info.add("schemaIdentity", identity);
        info.addProperty("status", "DEVELOPMENT");
        JsonObject body = new JsonObject();

        body.add("schemaInfo", info);
        body.add("schema", buildSchema(type, data));
        return body;
    }

    private static JsonObject buildSchema(String type, JsonObject data) {
        JsonObject schema = new JsonObject();
        schema.addProperty("$id", String.format("https://schema.osdu.opengroup.org/json/work-product-component/%s.1.0.0.json", type));
        schema.addProperty("$schema", "http://json-schema.org/draft-07/schema#");
        schema.addProperty("title", type);
        schema.addProperty("type", "object");
        schema.add("properties", buildTopProperties(type, data));
        return  schema;
    }

    private static JsonObject buildTopProperties(String type, JsonObject data) {
        JsonObject obj = new JsonObject();
        obj.add("id", buildID("Entity ID", "string", type));
        obj.add("kind", buildSimple("Entity Kind", "string"));
        obj.add("version", buildSimple("Version Number", "integer"));
        obj.add("acl", buildAcl());
        obj.add("legal", buildLegal());
        obj.add("data", data);
        return obj;
    }

    private static JsonObject buildAcl() {
        JsonObject properties = new JsonObject();
        JsonObject owners = buildArray("List of Owners", "string");
        JsonObject viewers = buildArray("List of viewers", "string");
        properties.add("owners", owners);
        properties.add("viewers", viewers);

        JsonObject obj = new JsonObject();
        obj.addProperty("title", "Access Control List");
        obj.addProperty("type", "object");
        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject buildLegal() {
        JsonObject properties = new JsonObject();
        JsonObject legaltags = buildArray("Legal Tags", "string");
        JsonObject otherRelevantDataCountries = buildArray("Other Relevant Data Countries", "string");
        JsonObject status = buildSimple("Legal Status", "string");
        properties.add("legaltags", legaltags);
        properties.add("otherRelevantDataCountries", otherRelevantDataCountries);
        properties.add("status", status);

        JsonObject obj = new JsonObject();
        obj.addProperty("title", "Legal Tags");
        obj.addProperty("type", "object");
        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject buildData(JsonObject properties) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");
        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject buildSimple(String title, String type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", type);
        return obj;
    }

    private static JsonObject buildRefData(String title, String entityType) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", "string");
        String pattern = "^[\\w\\-\\.]+:reference-data\\-\\-" + entityType + ":[\\w\\-\\.\\:\\%]+:[0-9]*$";
        obj.addProperty("pattern", pattern);
        return obj;
    }

    private static JsonObject buildMasterData(String title, String entityType) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", "string");
        String pattern = "^[\\w\\-\\.]+:master-data\\-\\-" + entityType + ":[\\w\\-\\.\\:\\%]+$";
        obj.addProperty("pattern", pattern);
        return obj;
    }

    private static JsonObject buildID(String title, String type, String entityType) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", type);
        String pattern = "^[\\w\\-\\.]+:master-data\\-\\-" + entityType + ":[\\w\\-\\.\\:\\%]+$";
        obj.addProperty("pattern", pattern);
        return obj;
    }

    private static JsonObject buildConst(String title, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("const", value);
        return obj;
    }

    private static JsonObject buildArray(String title, String itemType) {
        JsonObject items = new JsonObject();
        items.addProperty("type", itemType);

        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", "array");
        obj.add("items", items);
        return obj;
    }

    private static JsonObject buildArray(String title, String itemType, String entityType) {
        JsonObject items = new JsonObject();
        String pattern = "^[\\w\\-\\.]+:master-data\\-\\-" + entityType + ":[\\w\\-\\.\\:\\%]+:[0-9]*$";
        items.addProperty("pattern", pattern);
        items.addProperty("type", itemType);

        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("type", "array");
        obj.add("items", items);
        return obj;
    }

    private static SchemaMock getSchema(ClientResponse response) {
        Gson gson = new Gson();
        SchemaMock body = gson.fromJson(response.getEntity(String.class), SchemaMock.class);
        return body ;
    }

    public class SchemaMock {
        public schemaIdentityMock schemaIdentity;
    }

    public class schemaIdentityMock {
        public String id;
    }
}
