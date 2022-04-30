/*
 *  Copyright 2020-2021 Google LLC
 *  Copyright 2020-2021 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.wd.gcp.dataaccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.JdbcEntityRepository;
import org.opengroup.osdu.wd.gcp.model.JdbcEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class JdbcTreeTraversal {

    private final JdbcEntityRepository jdbcEntityRepository;
    private Set<String> idSet = new HashSet<>();

    public JsonObject buildEntityRef(JdbcEntity entity) {
        JsonObject res = new JsonObject();

        EntityDto entityDto = entity.getEntityDtoFromData();
        String entityId = entityDto.getEntityId();
        String entityType = entityDto.getEntityType();

        idSet.add(entityType + entityId);

        res.addProperty("id", entityId);
        res.addProperty("version", entityDto.getVersion());
        res.addProperty("type", entityType);

        List<Relationship> relationships = entity.getRelationshipsDto();
        if (relationships == null || relationships.isEmpty()) {
            return res;
        }

        JsonArray arr = new JsonArray();
        for (Relationship rs : relationships) {
            String id = rs.getId();
            String type = rs.getEntityType();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type) || !id.matches("^[^:]+:[0-9]+$")) {
                continue;
            }
            if (idSet.add(type + id.split(":")[0])) {
                Optional<JdbcEntity> latestByIdAndType = getSpecificEntity(type, id);
                if (latestByIdAndType.isPresent()) {
                    JsonObject children = buildEntityRef(latestByIdAndType.get());
                    arr.add(children);
                }
            }
        }
        if (arr.size() > 0) {
            res.add("items", arr);
        }

        return res;
    }

    public List<JdbcEntity> buildEntityList(JdbcEntity entity) {
        List<JdbcEntity> res = new ArrayList<>();

        EntityDto entityDto = entity.getEntityDtoFromData();
        String entityId = entityDto.getEntityId();
        String entityType = entityDto.getEntityType();

        idSet.add(entityType + entityId);
        res.add(entity);

        List<Relationship> relationships = entity.getRelationshipsDto();
        if (relationships == null || relationships.isEmpty()) {
            return res;
        }

        for (Relationship rs : relationships) {
            String id = rs.getId();
            String type = rs.getEntityType();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type)) {
                continue;
            }
            if (idSet.add(type + id.split(":")[0])) {
                Optional<JdbcEntity> latestByIdAndType = getSpecificEntity(type, id);
                if (latestByIdAndType.isPresent()) {
                    List<JdbcEntity> children = buildEntityList(latestByIdAndType.get());
                    res.addAll(children);
                }
            }
        }
        return res;
    }

    private Optional<JdbcEntity> getSpecificEntity(String entityType, String idWithVersion) {
        if (StringUtils.isBlank(idWithVersion)) {
            return Optional.empty();
        }
        String id = idWithVersion.split(":")[0];
        return jdbcEntityRepository.getLatestByIdAndType(entityType, id);
    }
}
