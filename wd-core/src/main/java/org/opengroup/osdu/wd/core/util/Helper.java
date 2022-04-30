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

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.client.model.http.AppException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import org.opengroup.osdu.wd.core.models.IdType;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Helper {

    public JsonNode toJsonNode(Object dataObj){
        try {
            Gson gson = new Gson();
            String jsonInString = gson.toJson(dataObj);
            ObjectMapper treeMapper = new ObjectMapper();
            JsonNode nodes = treeMapper.readTree(jsonInString);
            return nodes;
        }catch (Exception ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", ex.getMessage());
        }
    }

    public JsonSchema toJsonSchema(Object schemaObj) {
        try {
            Gson gson = new Gson();
            String schemaInString = gson.toJson(schemaObj);
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(mapper).build();
            JsonSchema schema = factory.getSchema(schemaInString);
            return schema;
        } catch (Exception ex) {
            String error = ex.getMessage() != null ? ex.getMessage() : "The schema is invalid";
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", error);
        }
    }

    public List<String> validateJson(JsonNode node, JsonSchema schema) {
        try {
            Set<ValidationMessage> errors = schema.validate(node);
            if (errors.size() == 0) {
                return null;
            }
            List<String> messages = errors.stream().map(x -> x.getMessage()).collect(Collectors.toList());
            return messages;
        } catch (AppException ex) {
            throw ex;
        } catch (ValidationException ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", ex.getMessage());
        } catch (Exception ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", ex.getMessage());
        }
    }

    public IdType getEntityIdType(String id){
        int start = id.indexOf("--");
        int mid = id.indexOf(':', start);
        int end = id.length();
        String entityType = id.substring(start + 2, mid).toLowerCase();
        String entityId = id.substring(mid + 1, end);
        return new IdType(entityId, entityType);
    }

    public String getValueFromRefID(String refId){
        int start = refId.indexOf("--");
        int mid = refId.indexOf(':', start);
        int end = refId.indexOf(':', mid + 1);
        String value = refId.substring(mid + 1, end);
        return value;
    }

    public List<Relationship> buildRelationships(JsonNode root, String type, String regex) {
        try {
            List<Relationship> relationships = new ArrayList<>();
            Iterator<JsonNode> it = root.iterator();
            while (it.hasNext()) {
                JsonNode node = it.next();
                if (node.isArray()) {
                    Iterator<JsonNode> arr = node.iterator();
                    while (arr.hasNext()) {
                        JsonNode itemNode = arr.next();
                        appendNodeRelationships(itemNode, type, regex, relationships);
                    }
                } else {
                    appendNodeRelationships(node, type, regex, relationships);
                }
            }
            return relationships;
        } catch (Exception ex) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
        }
    }

    private  void appendNodeRelationships(JsonNode node, String type, String regex, List<Relationship> relationships)
    {
        if (node.isTextual()) {
            String str = node.textValue();
            Relationship relationship = buildRelationship(str, type, regex);
            if (relationship != null) {
                relationships.add(relationship);
            }
        } else if (node.isObject()) {
            List<Relationship> subRelationships = buildRelationships(node, type, regex);
            relationships.addAll(subRelationships);
        }
    }

    private Relationship buildRelationship(String str, String type, String regex){
        if(StringUtils.isBlank(str))
            return null;
        if(!str.matches(regex))
            return null;
        int start = str.indexOf("--");
        int mid = str.indexOf(':', start);
        int end = str.length();
        String entityType = str.substring(start + 2, mid).toLowerCase();
        if(type.equalsIgnoreCase(entityType))
            return  null;
        String id = str.substring(mid + 1, end);
        return new Relationship(id, entityType);
    }
}
