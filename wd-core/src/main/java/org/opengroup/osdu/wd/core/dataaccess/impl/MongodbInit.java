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

package org.opengroup.osdu.wd.core.dataaccess.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IMongodbConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MongodbInit {

    @Autowired
    IMongodbConnection mongodbConnection;
    @Autowired
    private RequestInfo requestInfo;

    @Value("${mongodb.database}")
    private String mongoDBName;

    @Value("${mongodb.api.cosmosdb}")
    private String cosmosdbAPI;

    private MongoClient mongoClient;
    private MongoCollection<Document> entityCollection;

    public MongoCollection<Document> getEntityCollection(String entityType) {
        if (mongoClient == null)
            mongoClient = getMongoClient();
        String collectionName = entityType + "Container";
        if (entityCollection == null || !collectionName.equalsIgnoreCase(entityCollection.getNamespace().getCollectionName())) {
            entityCollection = MongodbFacade.getCollection(mongoClient, this.mongoDBName, collectionName);
        }
        return entityCollection;
    }

    public MongoCollection<Document> getEntityCollectionOrNull(String entityType) {
        if (mongoClient == null)
            mongoClient = getMongoClient();
        String collectionName = entityType + "Container";
        if (entityCollection == null || !collectionName.equalsIgnoreCase(entityCollection.getNamespace().getCollectionName())) {
            entityCollection = MongodbFacade.getCollectionOrNull(mongoClient, this.mongoDBName, collectionName);
        }
        return entityCollection;
    }

    public MongoCollection<Document> createOrGetEntityCollection(String entityType) {
        if (mongoClient == null)
            mongoClient = getMongoClient();
        String collectionName = entityType + "Container";
        if (entityCollection == null || !collectionName.equalsIgnoreCase(entityCollection.getNamespace().getCollectionName())){
            Boolean isCosmosAPI =  "true".equalsIgnoreCase(this.cosmosdbAPI);
            entityCollection = MongodbFacade.createOrGetCollection(mongoClient, this.mongoDBName, collectionName, isCosmosAPI);
        }
        return entityCollection;
    }

    private MongoClient getMongoClient() {
        String partitionId = requestInfo.getDpsHeaders().getPartitionId();
        String connectionString = mongodbConnection.get(partitionId);
        return MongodbFacade.createClient(connectionString);
    }
}
