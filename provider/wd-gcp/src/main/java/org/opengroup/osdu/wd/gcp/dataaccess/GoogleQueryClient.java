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

import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.ACTIVITY_PLAN;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.BHA_RUN;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.HOLE_SECTION;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELL;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELLBORE;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELLBORE_TRAJECTORY;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELL_ACTIVITY_PROGRAM;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.JdbcEntityRepository;
import org.opengroup.osdu.wd.gcp.model.JdbcEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.entity.source", havingValue = "postgresql", matchIfMissing = true)
public class GoogleQueryClient implements IQueryClient {

    private final JdbcEntityRepository jdbcEntityRepository;

    private final JdbcTreeTraversal treeTraversal;

    @Override
    public List<EntityDtoReturn> getBHARunsBySection(String existenceKind, String sectionId) {
        return jdbcEntityRepository
            .getEntitiesByExistenceKindAndRelationshipEntityId(BHA_RUN, existenceKind, HOLE_SECTION, sectionId).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWells(String existenceKind, List<String> wellIds) {
        List<JdbcEntity> wellboresByWells =
            jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityMultipleIds(WELLBORE, existenceKind, WELL, wellIds);

        List<String> notPresentWellIds = wellIds.stream().filter(id -> idNotPresentInRelationships(id, wellboresByWells)).collect(Collectors.toList());
        if (!notPresentWellIds.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find wellbores for provided well ids: " + notPresentWellIds);
        }
        List<String> wellboreIds = wellboresByWells.stream()
            .map(e -> e.getEntityDtoFromData().getEntityId())
            .collect(Collectors.toList());

        return jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityMultipleIds(BHA_RUN, existenceKind, WELLBORE, wellboreIds).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWellbore(String existenceKind, String wellboreId) {
        return jdbcEntityRepository
            .getEntitiesByExistenceKindAndRelationshipEntityId(BHA_RUN, existenceKind, WELLBORE, wellboreId).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public List<EntityDtoReturn> getHoleSectionsByWellbore(String existenceKind, String wellboreId) {
        return jdbcEntityRepository
            .getEntitiesByExistenceKindAndRelationshipEntityId(HOLE_SECTION, existenceKind, WELLBORE, wellboreId).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public List<EntityDtoReturn> getWellboreTrajectoriesByWells(String existenceKind, List<String> wellIds) {
        List<JdbcEntity> wellboresByWells =
            jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityMultipleIds(WELLBORE, existenceKind, WELL, wellIds);

        List<String> notPresentWellIds = wellIds.stream().filter(id -> idNotPresentInRelationships(id, wellboresByWells)).collect(Collectors.toList());
        if (!notPresentWellIds.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find wellbores for provided well ids: " + notPresentWellIds);
        }
        List<String> wellboreIds = wellboresByWells.stream()
            .map(e -> e.getEntityDtoFromData().getEntityId())
            .collect(Collectors.toList());

        return jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityMultipleIds(WELLBORE_TRAJECTORY, existenceKind, WELLBORE, wellboreIds)
            .stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellId) {
        String wellboreEntityId = getWellboreIdByWell(existenceKind, wellId);

        JdbcEntity entity = jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(ACTIVITY_PLAN, existenceKind, WELLBORE, wellboreEntityId)
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find latest activity plan with well id: " + wellId));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public EntityDtoReturn getLatestWellActivityProgramByWell(String existenceKind, String wellId) {
        String wellboreEntityId = getWellboreIdByWell(existenceKind, wellId);

        JdbcEntity entity =
            jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, existenceKind, WELLBORE, wellboreEntityId)
                .orElseThrow(() -> new AppException(
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    "Could not find latest well activity program with well id:" + wellId));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public EntityDtoReturn getSpecificWellActivityProgramByWell(String existenceKind, String wellId, long dpVersion) {
        String wellboreEntityId = getWellboreIdByWell(existenceKind, wellId);

        JdbcEntity entity = jdbcEntityRepository
            .getSpecificEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, existenceKind, dpVersion, WELLBORE, wellboreEntityId)
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find specific well activity program with well id: " + wellId + ", version: " + dpVersion));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public Object getLatestWellActivityProgramRefTreeByWell(String existenceKind, String wellId) {
        String wellboreIdByWell = getWellboreIdByWell(existenceKind, wellId);
        JdbcEntity entity =
            jdbcEntityRepository.getLatestActivityProgramByExistenceKindAndWellId(existenceKind, wellboreIdByWell).orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find latest activity program by well-id: " + wellId));

        return treeTraversal.buildEntityRef(entity);
    }

    @Override
    public Object getSpecificWellActivityProgramRefTreeByWell(String existenceKind, String wellId, long dpVersion) {
        String wellboreIdByWell = getWellboreIdByWell(existenceKind, wellId);
        JdbcEntity entity = jdbcEntityRepository
            .getSpecificEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, existenceKind, dpVersion, WELLBORE, wellboreIdByWell)
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find specific well activity program with well id: " + wellId + ", version: " + dpVersion));

        return treeTraversal.buildEntityRef(entity);
    }

    @Override
    public List<Long> getWellActivityProgramVersionNumbersByWell(String existenceKind, String wellId) {
        String wellboreEntityId = getWellboreIdByWell(existenceKind, wellId);
        return jdbcEntityRepository.getAllVersionsByRelationshipEntityId(WELL_ACTIVITY_PROGRAM, existenceKind, WELLBORE, wellboreEntityId);
    }

    @Override
    public List<EntityDtoReturn> getLatestWellActivityProgramChildrenListByWell(String existenceKind, String wellId) {
        String wellboreEntityId = getWellboreIdByWell(existenceKind, wellId);
        JdbcEntity entity =
            jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, existenceKind, WELLBORE, wellboreEntityId)
                .orElseThrow(() -> new AppException(
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    "Could not find specific well activity program with well id: " + wellId));

        List<JdbcEntity> entities = treeTraversal.buildEntityList(entity);
        return entities.stream().map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> getWellboreVersionNumbersByWell(String existenceKind, String wellId) {
        return jdbcEntityRepository.getAllVersionsByRelationshipEntityId(WELLBORE, existenceKind, WELL, wellId);

    }

    @Override
    public EntityDtoReturn getLatestWellboreVersionByWell(String existenceKind, String wellId) {
        JdbcEntity entity = jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELLBORE, existenceKind, WELL, wellId)
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find latest wellbore with well id: " + wellId));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public EntityDtoReturn getSpecificWellboreVersionByWell(String existenceKind, String wellId, long wellboreVersion) {
        JdbcEntity entity = jdbcEntityRepository.getSpecificEntityByExistenceKindAndRelationshipEntityId(WELLBORE, existenceKind, wellboreVersion, WELL, wellId)
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find specific wellbore with well id: " + wellId + ", version: " + wellboreVersion));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public List<Long> getWellVersionNumbers(String existenceKind, String name) {
        return jdbcEntityRepository.getWellVersionNumbersByExistenceKindAndWellName(existenceKind, name);
    }

    @Override
    public EntityDtoReturn getLatestWellVersion(String existenceKind, String name) {
        JdbcEntity entity = jdbcEntityRepository.getLatestWellVersionByExistenceKindAndWellName(existenceKind, name).orElseThrow(() -> new AppException(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Could not find well with well name: " + name + ", and existence kind: " + existenceKind));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public EntityDtoReturn getSpecificWellVersion(String existenceKind, String name, long version) {
        JdbcEntity entity =
            jdbcEntityRepository.getSpecificEntityVersionByIdAndTypeAndExistenceKind(WELL, existenceKind, name, version).orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find well with well name: " + name + ", and existence kind: " + existenceKind + ", and version: " + version));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public List<EntityDtoReturn> getDrillingReportsByWellbore(String existenceKind, String wellboreId) {
        return jdbcEntityRepository.getDrillingReportsByWellbore(existenceKind, wellboreId).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public EntityDtoReturn getLatestDrillingReportByWellbore(String existenceKind, String wellboreId) {
        JdbcEntity entity = jdbcEntityRepository.getLatestDrillingReportByWellbore(existenceKind, wellboreId).orElseThrow(() -> new AppException(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Could not find latest drilling report by well bore id: " + wellboreId));

        return new EntityDtoReturn(entity.getEntityDtoFromData());
    }

    @Override
    public List<EntityDtoReturn> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString) {
        return jdbcEntityRepository.getDrillingReportsByTimeRange(existenceKind, startTimeString, endTimeString).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    @Override
    public Object getLatestDrillingReportRefTree(String existenceKind, String drillingReportId) {
        JdbcEntity entity = jdbcEntityRepository.getLatestDrillingReport(existenceKind, drillingReportId).orElseThrow(() -> new AppException(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Could not find latest drilling report by id: " + drillingReportId));

        return treeTraversal.buildEntityRef(entity);
    }

    @Override
    public List<EntityDtoReturn> getFluidsReportsByWellbore(String existenceKind, String wellboreId) {
        return jdbcEntityRepository.getFluidReportsByWellbore(existenceKind, wellboreId).stream()
            .map(e -> new EntityDtoReturn(e.getEntityDtoFromData()))
            .collect(Collectors.toList());
    }

    private String getWellboreIdByWell(String existenceKind, String wellEntityId) {
        return jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELLBORE, existenceKind, WELL, wellEntityId)
            .map(jdbcEntity -> jdbcEntity.getEntityDtoFromData().getEntityId())
            .orElseThrow(() -> new AppException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Could not find wellbore with well id: " + wellEntityId));
    }

    private boolean idNotPresentInRelationships(String id, List<JdbcEntity> entities) {
        return entities.stream()
            .map(JdbcEntity::getRelationshipsDto)
            .flatMap(Collection::stream)
            .noneMatch(relationship -> relationship.getId().split(":")[0].equals(id));
    }

}
