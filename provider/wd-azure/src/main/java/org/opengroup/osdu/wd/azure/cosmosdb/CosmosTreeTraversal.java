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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequestScope
public class CosmosTreeTraversal {
    @Autowired
    CosmosdbInit cosmosdbInit;

    private Set<String> idSet;

    public CosmosTreeTraversal()
    {
        this.idSet = new HashSet<>();
    }

    public JsonObject buildEntityRef(CosmosEntity entity) {
        JsonObject res = new JsonObject();

        String entityId = entity.getEntityId();
        String entityType = entity.getEntityType();
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);

        res.addProperty("id", entityId);
        res.addProperty("version", entity.getVersion());
        res.addProperty("type", entityType);

        List<Relationship> relationships = entity.getRelationships();
        if (relationships == null || relationships.size() == 0)
            return res;

        JsonArray arr = new JsonArray();
        for (Relationship rs : relationships) {
            String id = rs.getId();
            String type = rs.getEntityType();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type) || !id.matches("^[^:]+:[0-9]+$"))
                continue;
            if (this.idSet.contains(type + id.split(":")[0]))
                continue;
            else
                this.idSet.add(type + id.split(":")[0]);

            CosmosContainer subCollection = cosmosdbInit.getEntityContainerOrNull(type);
            if(subCollection != null) {
                CosmosEntity sub = getSpecificEntity(subCollection, id);
                if (sub != null) {
                    JsonObject children = buildEntityRef(sub);
                    arr.add(children);
                }
            }
        }
        if (arr.size() > 0)
            res.add("items", arr);

        return res;
    }

    public List<CosmosEntity> buildEntityList(CosmosEntity entity) {
        List<CosmosEntity> res = new ArrayList<>();

        String entityId = entity.getEntityId();
        String entityType = entity.getEntityType();
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);
        res.add(entity);

        List<Relationship> relationships = entity.getRelationships();
        if (relationships == null || relationships.size() == 0)
            return res;

        for (Relationship rs : relationships) {
            String id = rs.getId();
            String type = rs.getEntityType();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type))
                continue;
            if (this.idSet.contains(type + id.split(":")[0]))
                continue;
            else
                this.idSet.add(type + id.split(":")[0]);

            CosmosContainer subCollection = cosmosdbInit.getEntityContainerOrNull(type);
            if(subCollection != null) {
                CosmosEntity sub = getSpecificEntity(subCollection, id);
                if (sub != null) {
                    List<CosmosEntity> children = buildEntityList(sub);
                    res.addAll(children);
                }
            }
        }
        return res;
    }

    private CosmosEntity getLatestEntity(CosmosContainer container, String entityId) {
        if (StringUtils.isBlank(entityId))
            return null;
        CosmosEntity entity = CosmosdbFacade.findLatestItem(container, entityId);
        if (entity == null)
            return null;
        return  entity;
    }

    private CosmosEntity getSpecificEntity(CosmosContainer container, String id) {
        if (StringUtils.isBlank(id))
            return null;
        String partitionKey = id.split(":")[0];
        CosmosEntity entity = CosmosdbFacade.findSpecificItem(container, id, partitionKey);
        if (entity == null)
            return null;
        return  entity;
    }
}
