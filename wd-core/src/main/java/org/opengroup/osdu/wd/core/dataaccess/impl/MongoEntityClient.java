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

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IEntityDBClient;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.core.util.Common;
import org.opengroup.osdu.wd.core.util.RecordConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "mongodb", matchIfMissing = true)
public class MongoEntityClient implements IEntityDBClient {

    @Autowired
    MongodbInit mongodbInit;

    @Autowired
    RecordConversion conversion;

    @Override
    public EntityDtoReturn saveEntity(EntityDto dto, List<Relationship> relationships) {
        MongoCollection<Document> collection = mongodbInit.createOrGetEntityCollection(dto.getEntityType());

        MongoEntity entity = new MongoEntity(dto, relationships);
        String _id = entity.get_id();
        Document doc = entity.ToDocument();
        MongodbFacade.upsert(collection, _id, doc);
        return new EntityDtoReturn(dto, null);
    }

    @Override
    public EntityDtoReturn getLatestEntityVersion(String entityType, String entityId) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        Document doc = MongodbFacade.findLatestOne(collection, entityId);
        if (doc == null || doc.isEmpty())
            return null;

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificEntityVersion(String entityType, String entityId, long version) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        String _id = Common.buildId(entityId, version);
        Document doc = MongodbFacade.findSpecificOne(collection, _id);
        if (doc == null || doc.isEmpty())
            return null;

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public List<Long> getEntityVersionNumbers(String entityType, String entityId) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        List<Long> list = MongodbFacade.findVersionNumbers(collection, entityId);
        return list;
    }

    @Override
    public long deleteEntity(String entityType, String entityId) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        long cnt = MongodbFacade.deleteEntity(collection, entityId);
        return cnt;
    }

    @Override
    public long purgeEntity(String entityType, String entityId) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        long cnt = MongodbFacade.purgeEntity(collection, entityId);
        return cnt;
    }

    @Override
    public long deleteEntityVersion(String entityType, String entityId, long version) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        String _id = Common.buildId(entityId, version);
        int cnt = MongodbFacade.deleteOne(collection, _id);
        return cnt;
    }

    @Override
    public long purgeEntityVersion(String entityType, String entityId, long version) {
        MongoCollection<Document> collection = mongodbInit.getEntityCollection(entityType);

        String _id = Common.buildId(entityId, version);
        int cnt = MongodbFacade.purgeOne(collection, _id);
        return cnt;
    }
}
