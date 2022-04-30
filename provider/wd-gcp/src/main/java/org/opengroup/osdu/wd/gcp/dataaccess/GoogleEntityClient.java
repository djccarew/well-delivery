/*
 *   Copyright 2020-2021 Google LLC
 *   Copyright 2020-2021 EPAM Systems, Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.opengroup.osdu.wd.gcp.dataaccess;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IEntityDBClient;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.JdbcEntityRepository;
import org.opengroup.osdu.wd.gcp.model.JdbcEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@ConditionalOnProperty(name = "app.entity.source", havingValue = "postgresql", matchIfMissing = true)
public class GoogleEntityClient implements IEntityDBClient {

    private final JdbcEntityRepository jdbcEntityRepository;

    @Override
    public EntityDtoReturn saveEntity(EntityDto dto, List<Relationship> relationships) {
        Optional<JdbcEntity> entity = jdbcEntityRepository.getSpecificVersionByIdAndType(dto.getEntityType(), dto.getEntityId(), dto.getVersion());
        EntityDto entityDto = null;
        if (entity.isPresent()) {
            entityDto = jdbcEntityRepository.updateEntity(entity.get().getId(), dto, relationships).getEntityDtoFromData();
        } else {
            entityDto = jdbcEntityRepository.insertEntity(dto, relationships).getEntityDtoFromData();
        }

        return new EntityDtoReturn(entityDto);
    }

    @Override
    public EntityDtoReturn getLatestEntityVersion(String entityType, String entityId) {
        EntityDto entityDto = jdbcEntityRepository.getLatestByIdAndType(entityType, entityId).orElseThrow(() ->
            new AppException(HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "There is no objects with id: " + entityId)).getEntityDtoFromData();

        return new EntityDtoReturn(entityDto);
    }

    @Override
    public EntityDtoReturn getSpecificEntityVersion(String entityType, String entityId, long version) {
        EntityDto entityDto = jdbcEntityRepository.getSpecificVersionByIdAndType(entityType, entityId, version).orElseThrow(() ->
            new AppException(HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "There is no objects with id: " + entityId + ", and version: " + version)).getEntityDtoFromData();

        return new EntityDtoReturn(entityDto);
    }

    @Override
    public List<Long> getEntityVersionNumbers(String entityType, String entityId) {
        return jdbcEntityRepository.getAllVersionsForIdAndType(entityType, entityId);
    }

    @Override
    public long deleteEntity(String entityType, String entityId) {
        return jdbcEntityRepository.deleteEntity(entityType, entityId);
    }

    @Override
    public long purgeEntity(String entityType, String entityId) {
        return jdbcEntityRepository.purgeEntity(entityType, entityId);
    }

    @Override
    public long deleteEntityVersion(String entityType, String entityId, long version) {
        return jdbcEntityRepository.deleteSpecificEntityVersion(entityType, entityId, version);
    }

    @Override
    public long purgeEntityVersion(String entityType, String entityId, long version) {
        return deleteEntityVersion(entityType, entityId, version);
    }

}
