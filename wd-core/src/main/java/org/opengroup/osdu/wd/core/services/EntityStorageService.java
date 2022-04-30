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

package org.opengroup.osdu.wd.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import org.apache.commons.lang3.StringUtils;

import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.storage.Record;
import org.opengroup.osdu.core.client.storage.IStorageClient;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IEntityDBClient;
import org.opengroup.osdu.wd.core.models.*;
import org.opengroup.osdu.wd.core.util.Common;
import org.opengroup.osdu.wd.core.util.DateTimeUtil;
import org.opengroup.osdu.wd.core.util.RecordConversion;
import org.opengroup.osdu.wd.core.util.Helper;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;


@Service
public class EntityStorageService {
    @Autowired
    private IEntityDBClient entityDBClient;
    @Autowired
    private IStorageClient StorageClient;
    @Autowired
    private RecordConversion conversion;
    @Autowired
    private RequestInfo requestInfo;
    @Autowired
    private Helper helper;
    @Autowired
    private EntityValidateService validate;
    @Autowired
    private SchemaAndCacheService schemaService;

    @Value("${app.entity.storage}")
    private String saveStorage;

    private final String fullIdRegex = "^[\\w\\-\\.]+:[0-9a-zA-Z\\-]+\\-\\-[\\w\\-]*:[\\w\\-\\.\\:\\%]+$";
    private final String entityTypeRegex = "^[0-9a-zA-Z\\-]*$";
    private final String fullRefIdRegex = "^[\\w\\-\\.]+:[0-9a-zA-Z\\-]+\\-\\-[\\w\\-]*:[\\w\\-\\.\\:\\%]+:[0-9]+$";
    private final String refIdRegex = "^[\\w\\-\\.\\:\\%]+:[0-9]+$";
    private final String fullRefDataRegex = "^[\\w\\-\\.]+:[0-9a-zA-Z\\-]+\\-\\-[\\w\\-]*:[\\w\\-\\.\\:\\%]+:[0-9]*$";
    private final long invalid_version = 20000;

    public EntityDtoReturn createOrUpdateEntities(String type, Object entity) {
        EntityDto dto = new EntityDto();
        JsonNode node = helper.toJsonNode(entity);

        //Verify ID field
        if (checkIfStringPropertyEmpty(node, ENTITY_PROPERTY.ID))
            throw new ValidationException("Entity Id is empty.");
        dto.setId(node.get(ENTITY_PROPERTY.ID).textValue());
        if (!dto.getId().matches(fullIdRegex))
            throw new ValidationException("Entity Id is invalid.");
        IdType idType = helper.getEntityIdType(dto.getId());
        if (!idType.getEntityType().equalsIgnoreCase(type))
            throw new ValidationException("Entity type in API(" + type + ")" + " and body(" + idType.getEntityType() + ") are not same.");
        dto.setEntityId(idType.getId());
        dto.setEntityType(idType.getEntityType().toLowerCase());

        //Verify Kind field
        if (checkIfStringPropertyEmpty(node, ENTITY_PROPERTY.KIND))
            throw new ValidationException(String.format("The Kind of Entity %s is empty.", dto.getId()));
        dto.setKind(node.get(ENTITY_PROPERTY.KIND).textValue());

        //Verify version field
        long version = Instant.now().toEpochMilli();
        if (node.get(ENTITY_PROPERTY.VERSION) != null) {
            if (!node.get(ENTITY_PROPERTY.VERSION).isLong())
                throw new ValidationException(String.format("The version of Entity %s is invalid.", dto.getId()));
            if (node.get(ENTITY_PROPERTY.VERSION).longValue() > 0)
                version = node.get(ENTITY_PROPERTY.VERSION).longValue();
        }
        dto.setVersion(version);

        //Verify Legal and ACL fields
        validate.ValidateEntity(node, dto);

        //Verify SchemaID field and load schemaObj
        Object schemaObj = schemaService.getSchema(dto.getKind());
        if (schemaObj == null)
            throw new ValidationException(String.format("The schema is not existed for Kind %s.", dto.getKind()));

        //Verify entity against schemaObj
        JsonSchema schema = helper.toJsonSchema(schemaObj);
        List<String> errors = helper.validateJson(node, schema);
        dto.setValid(errors == null || errors.size() == 0);

        //Verify if data is null
        JsonNode data = node.get(ENTITY_PROPERTY.DATA);
        if (data == null)
            throw new ValidationException("Entity data is empty");
        dto.setData(((LinkedHashMap) entity).get(ENTITY_PROPERTY.DATA));

        //Set meta
        if (node.get(ENTITY_PROPERTY.META) != null)
            dto.setMeta(((LinkedHashMap) entity).get(ENTITY_PROPERTY.META));

        //existenceKind
        if (checkIfStringPropertyEmpty(data, ENTITY_PROPERTY.EXISTENCE_KIND))
            throw new ValidationException("ExistenceKind is empty.");
        String existenceKindString = data.get(ENTITY_PROPERTY.EXISTENCE_KIND).textValue();
        if (!existenceKindString.matches(fullRefDataRegex))
            throw new ValidationException("ExistenceKind is not reference data format.");
        String existenceKind = helper.getValueFromRefID(existenceKindString);
        //if (!EXISTENCE_KIND.ACTUAL.equalsIgnoreCase(existenceKind) && !EXISTENCE_KIND.PLANNED.equalsIgnoreCase(existenceKind))
        //    throw new ValidationException("ExistenceKind value is invalid.");
        dto.setExistenceKind(existenceKind.toLowerCase());

        //StartDataTime
        if (!checkIfStringPropertyEmpty(data, "StartDateTime"))
        {
            String startDataTimeString = formatDateTimeString(data, "StartDateTime");
            dto.setStartTime(startDataTimeString);
        }
        //EndDataTime
        if (!checkIfStringPropertyEmpty(data, "EndDateTime"))
        {
            String endDataTimeString = formatDateTimeString(data, "EndDateTime");
            dto.setEndTime(endDataTimeString);
        }

        //Verify if WellActivityProgram only has one phase
        //if(dto.getEntityType().equalsIgnoreCase("WellActivityProgram")) {
        //    JsonNode phasesProperties = data.get("Phases");
        //    if (phasesProperties != null && phasesProperties.isArray()) {
        //        if (phasesProperties.size() != 1)
        //            throw new ValidationException("Only support phases which size is 1.");
        //    }
        //}

        //Build relationships
        List<Relationship> relationships = helper.buildRelationships(data, dto.getEntityType(), fullRefIdRegex);
        JsonNode extensionProperties = data.get("ExtensionProperties");
        if (extensionProperties != null) {
            List<Relationship> relationships1 = helper.buildRelationships(extensionProperties, dto.getEntityType(), fullRefIdRegex);
            relationships.addAll(relationships1);
        }
        //updateRelationshipVersions(relationships, refIdRegex);

        //Save entity to storage service
        if (this.saveStorage.equalsIgnoreCase("true") && version > invalid_version) {
            Record rec = conversion.toRecord(dto);
            StorageClient.saveRecord(requestInfo.getDpsHeaders(), rec);
        }

        //Save entity to db
        this.entityDBClient.saveEntity(dto, relationships);

        //Return EntityDtoReturn object
        return new EntityDtoReturn(dto, errors);
    }

    private void updateRelationshipVersions(List<Relationship> relationships, String refIdRegex) {
        for (Relationship relationship : relationships) {
            //Check if ref id has version
            if (relationship.getId().matches(refIdRegex))
                continue;
            //Get entity id
            String entityId = relationship.getId().split(":")[0];
            //get entity latest version and re-set relationship's id
            List<Long> list = this.entityDBClient.getEntityVersionNumbers(relationship.getEntityType(), entityId);
            int size = list.size();
            if (size > 0) {
                Long version = list.get(size - 1);
                relationship.setId(Common.buildId(entityId, version));
            }
        }
    }

    private  boolean checkIfStringPropertyEmpty(JsonNode node, String propertyName)
    {
        return  node.get(propertyName) == null || StringUtils.isBlank(node.get(propertyName).textValue());
    }

    private  String formatDateTimeString(JsonNode node, String propertyName) {
        try {
            String dateTimeString = node.get(propertyName).textValue();
            LocalDateTime dateTime = DateTimeUtil.parse(dateTimeString);
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }catch (Exception ex){
            return null;
        }
    }

    public EntityDtoReturn getLatestEntityVersion(String type, String id) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        EntityDtoReturn dto = this.entityDBClient.getLatestEntityVersion(type.toLowerCase(), id);
        if (dto == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find entity with id: " + id);
        validate.ValidateEntityReturn(dto);
        return dto;
    }

    public EntityDtoReturn getSpecificEntityVersion(String type, String id, long version) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        EntityDtoReturn dto = this.entityDBClient.getSpecificEntityVersion(type.toLowerCase(), id, version);
        if (dto == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, String.format("Could not find entity version with id: %s_%s ", id, version));
        validate.ValidateEntityReturn(dto);
        return dto;
    }

    public VersionNumbers getEntityVersionNumbers(String type, String id) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        List<Long> list = this.entityDBClient.getEntityVersionNumbers(type.toLowerCase(), id);
        if (list.size() == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find entity with id: " + id);
        return new VersionNumbers(id, list);
    }

    public void deleteEntity(String type, String id) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        long cnt = this.entityDBClient.deleteEntity(type, id);
        if (cnt == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find entity with id: " + id);
    }

    public void purgeEntity(String type, String id) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        long cnt = this.entityDBClient.purgeEntity(type.toLowerCase(), id);
        if (cnt == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find entity with id: " + id);
    }

    public void deleteEntityVersion(String type, String id, long version) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        long cnt = this.entityDBClient.deleteEntityVersion(type.toLowerCase(), id, version);
        if (cnt == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", String.format("Could not find entity version with id: %s_%s", id, version));
    }

    public void purgeEntityVersion(String type, String id, long version) {
        if (!type.matches(entityTypeRegex))
            throw new ValidationException("Invalid entity type: " + type);
        long cnt = this.entityDBClient.purgeEntityVersion(type.toLowerCase(), id, version);
        if (cnt == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, String.format("Could not find entity version with id: %s_%s ", id, version));
    }
}
