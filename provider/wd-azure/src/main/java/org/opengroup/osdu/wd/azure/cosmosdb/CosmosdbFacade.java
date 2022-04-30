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

package org.opengroup.osdu.wd.azure.cosmosdb;

import com.azure.cosmos.*;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.mongodb.MongoClient;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.models.Relationship;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CosmosdbFacade {

    private static final Logger LOGGER = Logger.getLogger(CosmosdbFacade.class.getName());
    private static final String selectSQL = "c.id, c.origId, c.entityId, c.entityType, c.kind, c.version, c.deleted, c.acl, c.legal, c.existenceKind, c.timeStamp, c.startTime, c.endTime, c.relationships, c.valid, c.data, c.meta";

    /////////////////////////////////////////
    // General Entity
    ////////////////////////////////////////

    public static void upsertItem(CosmosContainer container, CosmosEntity item) {
        try {
            container.upsertItem(item);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to put item into CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static CosmosEntity findSpecificItem(CosmosContainer container, String id, String partitionKey) {
        try {
            CosmosEntity item = container.getItem(id, partitionKey)
                    .read(new CosmosItemRequestOptions(partitionKey))
                    .getProperties()
                    .getObject(CosmosEntity.class);
            return item;
        } catch (NotFoundException e) {
            LOGGER.info(String.format("Unable to find item with ID=%s and PK=%s", id, partitionKey));
            return null;
        } catch (IOException e) {
            LOGGER.warning(String.format("Malformed document for item with ID=%s and PK=%s", id, partitionKey));
            return null;
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly encountered error calling CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    public static CosmosEntity findLatestItem(CosmosContainer container, String entityId) {
        String query = String.format("SELECT top 1 * FROM c WHERE c.entityId = '%s' ORDER BY c.id desc", entityId);
        List<CosmosEntity> list = queryItems(container, query, CosmosEntity.class);
        if (list.size() > 0)
            return list.iterator().next();
        else
            return null;
    }

    public static List<Long> findVersions(CosmosContainer container, String entityId) {
        List<CosmosEntity> list = getEntites(container, entityId);
        return list.stream().map(x -> x.getVersion()).sorted().collect(Collectors.toList());
    }

    public static void createItem(CosmosContainer container, CosmosEntity item) {
        try {
            container.createItem(item);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to put item into CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static int deleteEntity(CosmosContainer container, String entityId) {
        try {
            FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
            String query = String.format("SELECT * FROM c WHERE c.entityId = '%s'", entityId);
            Iterator<FeedResponse<CosmosItemProperties>> paginatedResponse = container.queryItems(query, options);
            int cnt = 0;
            while (paginatedResponse.hasNext()) {
                for (CosmosItemProperties properties : paginatedResponse.next().getResults()) {
                    CosmosEntity item = properties.getObject(CosmosEntity.class);
                    item.setDeleted(true);
                    container.upsertItem(item);
                    cnt++;
                }
            }
            return  cnt;
        } catch (IOException e) {
            String errorMessage = String.format("Malformed document for item with ID=%s", entityId);
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to soft delete item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    public static int purgeEntity(CosmosContainer container, String entityId, String partitionKey) {
        try {
            FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
            String query = String.format("SELECT * FROM c WHERE c.entityId = '%s'", entityId);
            Iterator<FeedResponse<CosmosItemProperties>> paginatedResponse = container.queryItems(query, options);
            ArrayList<String> list = new ArrayList();
            while (paginatedResponse.hasNext()) {
                for (CosmosItemProperties properties : paginatedResponse.next().getResults()) {
                    list.add(properties.getId());
                }
            }
            for (String id : list) {
                container.getItem(id, partitionKey).delete(new CosmosItemRequestOptions(partitionKey));
            }
            return list.size();
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to soft delete item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    public static int deleteItem(CosmosContainer container, String id, String partitionKey) {
        try {
            CosmosEntity item = container.getItem(id, partitionKey)
                    .read(new CosmosItemRequestOptions(partitionKey))
                    .getProperties()
                    .getObject(CosmosEntity.class);
            item.setDeleted(true);
            container.upsertItem(item);
            return 1;
        } catch (NotFoundException e) {
            return  0;
        } catch (IOException e) {
            String errorMessage = "Unexpectedly malformed document";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to delete item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    public static int purgeItem(CosmosContainer container, String id, String partitionKey) {
        try {
            container.getItem(id, partitionKey).delete(new CosmosItemRequestOptions(partitionKey));
            return 1;
        } catch (NotFoundException e) {
            return 0;
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to delete item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }


    /////////////////////////////////////////
    // Query Operation
    ////////////////////////////////////////

    public static CosmosEntity getLatestEntity_ByName(CosmosContainer container, String existenceKind, String name) {
        String query = String.format("SELECT top 1 %s FROM c WHERE c.deleted = false and c.existenceKind = '%s' and c.data.FacilityName = '%s' ORDER BY c.id desc",
                selectSQL, existenceKind.toLowerCase(), name);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity;
    }

    public static CosmosEntity getSpecificEntity_ByName(CosmosContainer container, String existenceKind, String name, long verison) {
        String query = String.format("SELECT top 1 %s FROM c WHERE c.deleted = false and c.existenceKind = '%s' and c.data.FacilityName = '%s' and c.version = %d",
                selectSQL, existenceKind.toLowerCase(), name, verison);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity;
    }

    public static List<Long> getEntityVersionNumberList_ByName(CosmosContainer container, String existenceKind, String name) {
        String query = String.format("SELECT c.version FROM c WHERE c.deleted = false and c.existenceKind = '%s' and c.data.FacilityName = '%s' ORDER BY c.version desc",
                existenceKind.toLowerCase(), name);

        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        List<Long> list = new ArrayList<>();
        for (CosmosEntity entity : entities) {
            list.add(entity.getVersion());
        }
        if (list.size() > 0)
            Collections.sort(list);
        return list;
    }

    public static List<String> getIdList_Relationships_ByEntityId(CosmosContainer container, String existenceKind, String entityId, String relationshipType){
        String query = String.format("SELECT top 1 %s FROM c WHERE c.deleted = false and c.existenceKind = '%s' and c.entityId = '%s' ORDER BY c.id desc",
                selectSQL, existenceKind.toLowerCase(), entityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if(entities.size() < 1 || entities.get(0) == null)
            return new ArrayList<>();
        CosmosEntity entity = entities.get(0);
        List<Relationship> relationships =  entity.getRelationships();
        if(relationships == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (Relationship relationship : relationships) {
            if(relationship.getId() != null && relationship.getEntityType() != null
                    && relationship.getEntityType().equalsIgnoreCase(relationshipType)){
                list.add(relationship.getId());
            }
        }
        return list;
    }

    public static String getId_Relationship_ByEntityId(CosmosContainer container, String existenceKind, String entityId, String relationshipType) {
        String query = String.format("SELECT top 1 %s FROM c WHERE c.deleted = false and c.existenceKind = '%s' and c.entityId = '%s' ORDER BY c.id desc",
                selectSQL, existenceKind.toLowerCase(), entityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        List<Relationship> relationships = entity.getRelationships();
        if (relationships == null)
            return null;
        for (Relationship relationship : relationships) {
            if (relationship.getId() != null && relationship.getEntityType() != null
                    && relationship.getEntityType().equalsIgnoreCase(relationshipType)) {
                return relationship.getId();
            }
        }
        return null;
    }

    public static CosmosEntity getLatestEntity_ByRelatedEntityId(CosmosContainer container, String existenceKind, String relatedType, String relatedEntityId) {
        String query = String.format("SELECT top 1 %s FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and STARTSWITH(a.id, '%s:') ORDER BY c.version desc",
                selectSQL, existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedEntityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity;
    }

    public static String getLatestId_ByRelatedEntityId(CosmosContainer container, String existenceKind, String relatedType, String relatedEntityId) {
        String query = String.format("SELECT top 1 %s FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and STARTSWITH(a.id, '%s:') ORDER BY c.version desc",
                selectSQL, existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedEntityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity.getId();
    }

    public static CosmosEntity getLatestEntity_ByRelatedId(CosmosContainer container, String existenceKind, String relatedType, String relatedId) {
        String query = String.format("SELECT top 1 %s FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and a.id = '%s' ORDER BY c.version desc",
                selectSQL, existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity;
    }

    public static String getLatestId_ByRelatedId(CosmosContainer container, String existenceKind, String relatedType, String relatedId) {
        String query = String.format("SELECT top 1 %s FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and a.id = '%s' ORDER BY c.version desc",
                selectSQL, existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity.getId();
    }

    public static CosmosEntity getSpecificEntity_ByRelatedEntityId(CosmosContainer container, String existenceKind, long verison, String relatedType, String relatedEntityId) {
        String query = String.format("SELECT top 1 %s FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and c.version = %d and a.entityType = '%s' and STARTSWITH(a.id, '%s:') ORDER BY c.version desc",
                selectSQL, existenceKind.toLowerCase(), verison, relatedType.toLowerCase(), relatedEntityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        if (entities.size() < 1 || entities.get(0) == null)
            return null;
        CosmosEntity entity = entities.get(0);
        return  entity;
    }

    public static List<String> getIdList_LatestPerEntity_ByRelatedEntityId(CosmosContainer container, String existenceKind, String relatedType, String relatedEntityId) {
        String query = String.format("SELECT c.entityId, c.id FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and STARTSWITH(a.id, '%s:')",
                existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedEntityId);
        List<ReturnRelationship> relationships = queryItems(container, query, ReturnRelationship.class);
        Map<String, String> map = new HashMap<>();
        for (ReturnRelationship relationship : relationships) {
            if (!map.containsKey(relationship.getEntityId()))
                map.put(relationship.getEntityId(), relationship.getId());
            else if (map.get(relationship.getEntityId()).compareTo(relationship.getId()) < 0)
                map.replace(relationship.getEntityId(), relationship.getId());
        }
        return map.values().stream().collect(Collectors.toList());
    }

    public static List<String> getIdList_LatestPerEntity_ByRelatedId(CosmosContainer container, String existenceKind, String relatedType, String relatedId) {
        String query = String.format("SELECT c.entityId, c.id FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and a.entityType = '%s' and a.id = '%s'",
                existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedId);
        List<ReturnRelationship> relationships = queryItems(container, query, ReturnRelationship.class);
        Map<String, String> map = new HashMap<>();
        for (ReturnRelationship relationship : relationships) {
            if (!map.containsKey(relationship.getEntityId()))
                map.put(relationship.getEntityId(), relationship.getId());
            else if (map.get(relationship.getEntityId()).compareTo(relationship.getId()) < 0)
                map.replace(relationship.getEntityId(), relationship.getId());
        }
        return map.values().stream().collect(Collectors.toList());
    }

    public static List<String> getIdList_LatestPerEntity_ByTimeRange(CosmosContainer container, String existenceKind, String startTime, String endTime) {
        String query = String.format("SELECT c.entityId, c.id FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s' and c.endTime >= '%s' and c.startTime <= '%s'",
                existenceKind.toLowerCase(), startTime, endTime);
        List<ReturnRelationship> relationships = queryItems(container, query, ReturnRelationship.class);
        Map<String, String> map = new HashMap<>();
        for (ReturnRelationship relationship : relationships) {
            if (!map.containsKey(relationship.getEntityId()))
                map.put(relationship.getEntityId(), relationship.getId());
            else if (map.get(relationship.getEntityId()).compareTo(relationship.getId()) < 0)
                map.replace(relationship.getEntityId(), relationship.getId());
        }
        return map.values().stream().collect(Collectors.toList());
    }

    public static List<Long> getEntityVersionNumberList_ByRelatedEntityId(CosmosContainer container, String existenceKind, String relatedType, String relatedEntityId) {
        String query = String.format("SELECT c.version FROM c join a in c.relationships WHERE c.deleted = false and c.existenceKind = '%s'  and a.entityType = '%s' and STARTSWITH(a.id, '%s:') ORDER BY c.version desc",
                existenceKind.toLowerCase(), relatedType.toLowerCase(), relatedEntityId);
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        List<Long> list = new ArrayList<>();
        for (CosmosEntity entity : entities) {
            list.add(entity.getVersion());
        }
        if (list.size() > 0)
            Collections.sort(list);
        return list;
    }

    public static List<CosmosEntity> getEntityList_ByIdList(CosmosContainer container, List<String> idList) {
        if(idList == null || idList.size() == 0)
            return new ArrayList<>();

        String inClause = idList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
        String query = "SELECT " + selectSQL + " FROM c WHERE c.id in (" + inClause + ")";
        List<CosmosEntity> entities = queryItems(container, query, CosmosEntity.class);
        return entities;
    }

    private static List<CosmosEntity> getEntites(CosmosContainer container, String entityId) {
        String query = String.format("SELECT * FROM c WHERE c.entityId = '%s'", entityId);
        List<CosmosEntity> list = queryItems(container, query, CosmosEntity.class);
        return list;
    }

    private static <T> List<T> queryItems(CosmosContainer container, String query, Class<T> clazz) {
        try {
            FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            ArrayList<T> results = new ArrayList<>();
            Iterator<FeedResponse<CosmosItemProperties>> paginatedResponse = container.queryItems(querySpec, options);

            while (paginatedResponse.hasNext()) {
                for (CosmosItemProperties properties : paginatedResponse.next().getResults()) {
                    results.add(properties.getObject(clazz));
                }
            }
            return results;
        } catch (IOException e) {
            String errorMessage = "Unexpectedly malformed document";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        } catch (Exception e) {
            String errorMessage = "Unexpectedly failed to get item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }


    /////////////////////////////////////////
    // Cosmos operation
    ////////////////////////////////////////

    public static CosmosClient getClient(String endpoint, String key) {

        if (StringUtils.isBlank(endpoint)) {
            String errorMessage = "Cosmos endpoint is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(key)) {
            String errorMessage = "Cosmos key is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }

        return CosmosClient.builder()
                .setEndpoint(endpoint)
                .setKey(key)
                .buildClient();
    }

    public static CosmosContainer getContainer(CosmosClient cosmosClient, String dbName, String containerName) {
        validateParameters(cosmosClient, dbName, containerName);
        CosmosDatabase db = cosmosClient.getDatabase(dbName);
        if (checkIfEntityExisted(db, containerName)) {
            return db.getContainer(containerName);
        } else {
            String errorMessage = "The Container " + containerName + " is not existed ";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_BAD_REQUEST, errorMessage, errorMessage);
        }
    }

    public static CosmosContainer getContainerOrNull(CosmosClient cosmosClient, String dbName, String containerName) {
        validateParameters(cosmosClient, dbName, containerName);
        CosmosDatabase db = cosmosClient.getDatabase(dbName);
        if (checkIfEntityExisted(db, containerName)) {
            return db.getContainer(containerName);
        } else {
            return null;
        }
    }

    public static CosmosContainer createOrGetContainer(CosmosClient cosmosClient, String dbName, String containerName) {
        validateParameters(cosmosClient, dbName, containerName);
        CosmosDatabase db = createOrGetDatabase(cosmosClient, dbName);
        CosmosContainer container = createOrGetContainer(db, containerName);
        return container;
    }

    public static CosmosDatabase createOrGetDatabase(CosmosClient cosmosClient, String dbName) {
        try {
            cosmosClient.createDatabaseIfNotExists(dbName);
            return cosmosClient.getDatabase(dbName);
        }catch (CosmosClientException e){
            String errorMessage = "Failed to create Cosmos database " + dbName;
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
    }

    private static CosmosContainer createOrGetContainer(CosmosDatabase db, String containerName){
        try {
            CosmosContainerResponse response = db.createContainerIfNotExists(containerName, "/entityId", 1200);
            return response.getContainer();
        }catch (CosmosClientException ex){
            String errorMessage = "Create Cosmos container failure.";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, ex.getMessage());
        }
    }

    private static void validateParameters(CosmosClient cosmosClient, String dbName, String containerName){
        if (cosmosClient == null) {
            String errorMessage = "Cosmos client is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(dbName)) {
            String errorMessage = "Cosmos DB Name is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(containerName)) {
            String errorMessage = "Cosmos container name is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
    }

    public static boolean checkIfEntityExisted(CosmosDatabase db, String containerName) {
        Iterator<FeedResponse<CosmosContainerProperties>> containers = db.readAllContainers();
        while (containers.hasNext()) {
            FeedResponse<CosmosContainerProperties> page = containers.next();
            List<CosmosContainerProperties> list = page.getResults();
            for (CosmosContainerProperties containerProperties : list) {
                if (containerProperties.getId().equalsIgnoreCase(containerName))
                    return true;
            }
        }
        return false;
    }

    public DefaultAzureCredential azureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }
}
