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

import com.azure.cosmos.CosmosContainer;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IEntityDBClient;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.core.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "cosmosdb", matchIfMissing = true)
public class CosmosEntityClient implements IEntityDBClient {

    @Autowired
    private CosmosdbInit cosmosInit;

    private ReentrantLock mutex = new ReentrantLock();

    @Override
    public EntityDtoReturn saveEntity(EntityDto dto, List<Relationship> relationships) {
        CosmosContainer container = cosmosInit.createOrGetEntityContainer(dto.getEntityType());

        CosmosEntity entity = new CosmosEntity(dto, relationships);
        try {
            mutex.lock();
            CosmosdbFacade.upsertItem(container, entity);
        } finally {
            mutex.unlock();
        }
        return new EntityDtoReturn(dto, null);
    }

    @Override
    public EntityDtoReturn getLatestEntityVersion(String entityType, String entityId) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        CosmosEntity res = CosmosdbFacade.findLatestItem(container, entityId);
        if (res == null)
            return null;
        return res.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificEntityVersion(String entityType, String entityId, long version) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        String id = Common.buildId(entityId, version);
        CosmosEntity res = CosmosdbFacade.findSpecificItem(container, id, entityId);
        if (res == null)
            return null;
        return res.ToEntityDtoReturn();
    }

    @Override
    public List<Long> getEntityVersionNumbers(String entityType, String entityId) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        List<Long> res = CosmosdbFacade.findVersions(container, entityId);
        return res;
    }

    @Override
    public long deleteEntity(String entityType, String entityId) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        int cnt = CosmosdbFacade.deleteEntity(container, entityId);
        return cnt;
    }

    @Override
    public long purgeEntity(String entityType, String entityId) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        int cnt = CosmosdbFacade.purgeEntity(container, entityId, entityId);
        return cnt;
    }

    @Override
    public long deleteEntityVersion(String entityType, String entityId, long version) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        String id = Common.buildId(entityId, version);
        int cnt = CosmosdbFacade.deleteItem(container, id, entityId);
        return cnt;
    }

    @Override
    public long purgeEntityVersion(String entityType, String entityId, long version) {
        CosmosContainer container = cosmosInit.getEntityContainer(entityType);

        String id = Common.buildId(entityId, version);
        int cnt = CosmosdbFacade.purgeItem(container, id, entityId);
        return cnt;
    }
}
