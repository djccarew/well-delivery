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

package org.opengroup.osdu.wd.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.networknt.schema.JsonSchema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.models.IdType;
import org.opengroup.osdu.wd.core.models.Relationship;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HelperTest {
    @InjectMocks
    Helper sut;

    @Test
    public void should_returnJsonNode_when_JsonIsValid() {
        Gson gson = new Gson();
        String str = "{'Name': 4324}";
        Object MyObject = gson.fromJson(str , Map.class);
        JsonNode node = this.sut.toJsonNode(MyObject);
        int actual = node.get("Name").asInt();
        assertEquals(4324, actual);
    }

    @Test
    public void should_returnJsonSchema_when_SchemaIsValid() {
        Gson gson = new Gson();
        String str = "{'properties': { 'name': { 'type': 'string' }}}";
        Object MyObject = gson.fromJson(str, Map.class);
        JsonSchema schema = this.sut.toJsonSchema(MyObject);
        JsonNode node = schema.getSchemaNode();
        String actual = node.get("properties").get("name").get("type").asText();
        assertEquals("string", actual);
    }

    @Test
    public void should_returnAppException_when_SchemaIsInvalid() {
        Gson gson = new Gson();
        String str = "{'properties': { 'items': { 'type': 'string', 'items':{ 'type' : 'string'} }}}";
        Object MyObject = gson.fromJson(str, Map.class);
        try {
            JsonSchema schema = this.sut.toJsonSchema(MyObject);
        }catch (AppException ex){
            String error = "AppError(code=400, reason=Bad request, message=#/properties/name/$ref: Reference ../abstract/AbstractAliasNames.1.0.0.json cannot be resolved";
            String message = ex.getError().toString().substring(0, error.length());
            assertEquals(error, message);
        }
    }

    @Test
    public void should_returnValid_when_JsonAndSchemaAreValid() {
        Gson gson = new Gson();
        String schemaStr = "{'properties': { 'name': { 'type': 'string' }}}";
        Object schemaObject = gson.fromJson(schemaStr, Map.class);
        JsonSchema schema = this.sut.toJsonSchema(schemaObject);
        String nodeStr = "{'name': 'my name'}";
        Object nodeObject = gson.fromJson(nodeStr , Map.class);
        JsonNode node = this.sut.toJsonNode(nodeObject);
        this.sut.validateJson(node, schema);
    }

    @Test
    public void should_returnAppException_when_JsonAndSchemaIsInvalid() {
        Gson gson = new Gson();
        String schemaStr = "{'properties': { 'name': { 'type': 'string' }}}";
        Object schemaObject = gson.fromJson(schemaStr, Map.class);
        JsonSchema schema = this.sut.toJsonSchema(schemaObject);
        String nodeStr = "{'name': 4343}";
        Object nodeObject = gson.fromJson(nodeStr , Map.class);
        JsonNode node = this.sut.toJsonNode(nodeObject);
        try {
            this.sut.validateJson(node, schema);
        }catch (AppException ex){
            String error ="AppError(code=400, reason=Bad request, message=$.name: number found";
            String message = ex.getError().toString().substring(0, error.length());
            assertEquals(error, message);
        }
    }

    @Test
    public void should_returnIdType_when_IdValid() {
        String id = "3283-4343-2897";
        String entityType = "Well";
        String idString = String.format("namespace:master-data--%s:%s", entityType, id);
        IdType result = this.sut.getEntityIdType(idString);
        assertEquals(id, result.getId());
        assertEquals(entityType.toLowerCase(), result.getEntityType());
    }

    @Test
    public void should_returnIdType_when_IdInvalid() {
        String id = "";
        try {
            IdType idType = this.sut.getEntityIdType(id);
            fail("Test failed.");
        } catch (Exception ex) {
            assert (ex != null);
        }
    }

    @Test
    public void should_returnId_when_RefIDValid() {
        String id = "3283-4343-2897";
        String entityType = "Well";
        String refid = String.format("namespace:master-data--%s:%s:", entityType, id);
        String result = this.sut.getValueFromRefID(refid);
        assertEquals(id, result);
    }

    @Test
    public void should_returnId_when_RefIDInvalid() {
        String refid = "";
        try {
            String id = this.sut.getValueFromRefID(refid);
            fail("Test failed.");
        } catch (Exception ex) {
            assert (ex != null);
        }
    }

    @Test
    public void should_returnRelationships_when_RootValid() {
        Gson gson = new Gson();
        String nodeStr = "{'ID1': 'namespace:work-product-component--ActivityPlan:9ced4cc5-02c8-5208-883d-520cbec39df7:547654785',"
                + "'ID2': ['namespace:work-product-component--Risk:5ead54cc5-02c8-5208-883d-520cbec39df7:543546554565',"
                + "'namespace:work-product-component--Risk:7ead54cc5-02c8-5208-883d-520cbec39df7:54432454785'],"
                + "'ID3': 'namespace:master-data--Well:825e82e7-69f8-5a50-97f7-5cf368db2218:423647832',"
                + "'ID4': ['namespace:work-product-component--Section:8ebc4cc5-02c8-5208-883d-520cbec39df7:3284673463']}";
        Object nodeObject = gson.fromJson(nodeStr , Map.class);
        JsonNode node = this.sut.toJsonNode(nodeObject);
        List<Relationship> relationships = this.sut.buildRelationships(node, "wellbore", "^[\\w\\-\\.]+:[0-9a-zA-Z\\-]+\\-\\-[0-9a-zA-Z\\-]*:[\\w\\-\\.\\:\\%]+:[0-9]+$");
        assertEquals(5, relationships.size());
        assertEquals("activityplan", relationships.get(0).getEntityType());
        assertEquals("9ced4cc5-02c8-5208-883d-520cbec39df7:547654785", relationships.get(0).getId());
        assertEquals("risk", relationships.get(1).getEntityType());
        assertEquals("5ead54cc5-02c8-5208-883d-520cbec39df7:543546554565", relationships.get(1).getId());
        assertEquals("risk", relationships.get(2).getEntityType());
        assertEquals("7ead54cc5-02c8-5208-883d-520cbec39df7:54432454785", relationships.get(2).getId());
        assertEquals("well", relationships.get(3).getEntityType());
        assertEquals("825e82e7-69f8-5a50-97f7-5cf368db2218:423647832", relationships.get(3).getId());
        assertEquals("section", relationships.get(4).getEntityType());
        assertEquals("8ebc4cc5-02c8-5208-883d-520cbec39df7:3284673463", relationships.get(4).getId());
    }
}



