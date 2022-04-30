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

package org.opengroup.osdu.wd.gcp.dataaccess.db.postgres;

import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildEndTimeParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildEntityIdParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildEntityTypeParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildExistenceKindParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildFacilityNameParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildRelationshipEntityTypeParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildRelationshipIdParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildRelationshipMultipleIdsParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildStartTimeParam;
import static org.opengroup.osdu.wd.gcp.util.QueryArgsConcatUtil.buildVersionParam;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.gcp.model.JdbcEntity;
import org.opengroup.osdu.wd.gcp.util.StatementBuilder;
import org.postgresql.util.PGTimestamp;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcEntityRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEntity insertEntity(EntityDto dto, List<Relationship> relationships) {
        JdbcEntity entity = new JdbcEntity();
        entity.setDataFromEntityDto(dto);
        entity.setRelationshipsFromDto(relationships);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO jdbc_entity(data, relationships) VALUES (?, ?) RETURNING id",
                Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, entity.getData());
            ps.setObject(2, entity.getRelationships());

            return ps;
        }, keyHolder);

        entity.setId((Long) keyHolder.getKey());

        return entity;
    }

    public JdbcEntity updateEntity(Long id, EntityDto dto, List<Relationship> relationships) {
        JdbcEntity jdbcEntity = new JdbcEntity(id, dto, relationships);
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("UPDATE jdbc_entity SET data = ?, relationships = ? WHERE id = ?");
            ps.setObject(1, jdbcEntity.getData());
            ps.setObject(2, jdbcEntity.getRelationships());
            ps.setObject(3, id);

            return ps;
        });

        return jdbcEntity;
    }

    public Optional<JdbcEntity> getLatestByIdAndType(String entityType, String entityId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildEntityIdParam(entityId))
            .stream()
            .findFirst();
    }

    public Optional<JdbcEntity> getSpecificVersionByIdAndType(String entityType, String entityId, long version) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildEntityIdParam(entityId), buildVersionParam(version))
            .stream()
            .findFirst();
    }

    public Optional<JdbcEntity> getSpecificEntityVersionByIdAndTypeAndExistenceKind(String entityType, String existenceKind, String name, long version) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildFacilityNameParam(name),
            buildVersionParam(version))
            .stream()
            .findFirst();
    }

    public List<Long> getAllVersionsForIdAndType(String entityType, String entityId) {
        String sql = new StatementBuilder()
            .select().selectVersion().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .orderBy().selectVersion().descending()
            .build();

        return jdbcTemplate.queryForList(sql, new Object[]{buildEntityTypeParam(entityType), buildEntityIdParam(entityId)}, Long.class);
    }

    public long deleteEntity(String entityType, String entityId) {
        String sql = new StatementBuilder()
            .update().jdbcEntity()
            .setDeletedAt()
            .where().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setObject(1, PGTimestamp.from(Instant.now()));
            ps.setObject(2, buildEntityTypeParam(entityType));
            ps.setObject(3, buildEntityIdParam(entityId));

            return ps;
        });
    }

    public long purgeEntity(String entityType, String entityId) {
        String sql = new StatementBuilder()
            .delete().fromJdbcEntity()
            .where().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setObject(1, buildEntityTypeParam(entityType));
            ps.setObject(2, buildEntityIdParam(entityId));

            return ps;
        });
    }

    public long deleteSpecificEntityVersion(String entityType, String entityId, long version) {
        String sql = new StatementBuilder()
            .update().jdbcEntity()
            .setDeletedAt()
            .where().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setObject(1, PGTimestamp.from(Instant.now()));
            ps.setObject(2, buildEntityTypeParam(entityType));
            ps.setObject(3, buildEntityIdParam(entityId));
            ps.setObject(4, buildVersionParam(version));

            return ps;
        });
    }

    public List<JdbcEntity> getEntitiesByExistenceKindAndRelationshipEntityId(String entityType, String existenceKind, String relationshipType,
        String relationshipEntityId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipIdLike()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildRelationshipEntityTypeParam(relationshipType),
            buildRelationshipIdParam(relationshipEntityId));
    }

    public List<JdbcEntity> getEntitiesByExistenceKindAndRelationshipEntityMultipleIds(String entityType, String existenceKind, String relationshipType,
        List<String> relationshipEntityId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipMultipleIdsLike(relationshipEntityId)
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildRelationshipEntityTypeParam(relationshipType),
            buildRelationshipMultipleIdsParam(relationshipEntityId));
    }

    public Optional<JdbcEntity> getLatestEntityByExistenceKindAndRelationshipEntityId(String entityType, String existenceKind, String relationshipType,
        String relationshipEntityId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildRelationshipEntityTypeParam(relationshipType),
            buildRelationshipIdParam(relationshipEntityId))
            .stream()
            .findFirst();
    }

    public Optional<JdbcEntity> getSpecificEntityByExistenceKindAndRelationshipEntityId(String entityType, String existenceKind, long version,
        String relationshipType, String relationshipEntityId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildVersionParam(version),
            buildRelationshipEntityTypeParam(relationshipType), buildRelationshipIdParam(relationshipEntityId))
            .stream()
            .findFirst();
    }

    public List<JdbcEntity> getLatestEntityByExistenceKindAndRelationshipId(String entityType, String existenceKind, String relationshipType,
        String relationshipId) {
        String sql = new StatementBuilder()
            .select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildRelationshipEntityTypeParam(relationshipType),
            buildRelationshipIdParam(relationshipId));
    }

    public List<Long> getAllVersionsByRelationshipEntityId(String entityType, String existenceKind, String relationshipType, String relationshipEntityId) {

        String sql = new StatementBuilder()
            .select().selectVersion().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByRelationship()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return jdbcTemplate.queryForList(sql,
            new Object[]{buildEntityTypeParam(entityType), buildExistenceKindParam(existenceKind), buildRelationshipEntityTypeParam(relationshipType),
                buildRelationshipIdParam(relationshipEntityId)}, Long.class);
    }


    public List<JdbcEntity> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().entityTypeIsDrillingReport()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildStartTimeParam(startTimeString), buildEndTimeParam(endTimeString));
    }

    public Optional<JdbcEntity> getLatestDrillingReportByWellbore(String existenceKind, String wellboreId) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().entityTypeIsDrillingReport()
            .and().filterByJsonB()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildRelationshipIdParam(wellboreId))
            .stream()
            .findFirst();
    }

    public Optional<JdbcEntity> getLatestDrillingReport(String existenceKind, String drillingReportId) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().entityTypeIsDrillingReport()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .orderBy().id().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildEntityIdParam(drillingReportId))
            .stream()
            .findFirst();
    }

    public List<JdbcEntity> getDrillingReportsByWellbore(String existenceKind, String wellboreId) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().entityTypeIsDrillingReport()
            .and().filterByJsonB()
            .and().filterByRelationshipIdLike()
            .orderBy().id().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildRelationshipIdParam(wellboreId));
    }

    public List<JdbcEntity> getFluidReportsByWellbore(String existenceKind, String wellboreId) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().entityTypeIsFluidReport()
            .and().filterByJsonB()
            .and().filterByRelationshipIdLike()
            .orderBy().id().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildRelationshipIdParam(wellboreId));
    }

    public Optional<JdbcEntity> getLatestActivityProgramByExistenceKindAndWellId(String existenceKind, String wellId) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity().comma().selectRelationshipsAsOneToMany().value()
            .where().entityNotDeleted()
            .and().entityTypeIsActivityProgram()
            .and().filterByJsonB()
            .and().filterByRelationshipIdLike()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildRelationshipIdParam(wellId))
            .stream()
            .findFirst();
    }

    public List<Long> getWellVersionNumbersByExistenceKindAndWellName(String existenceKind, String name) {
        String sql = new StatementBuilder()
            .select().selectVersion().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().entityTypeIsWell()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .orderBy().selectVersion().descending()
            .build();

        return jdbcTemplate.queryForList(sql, new Object[]{buildExistenceKindParam(existenceKind), buildFacilityNameParam(name)}, Long.class);
    }

    public Optional<JdbcEntity> getLatestWellVersionByExistenceKindAndWellName(String existenceKind, String name) {
        String sql = new StatementBuilder().select().all().fromJdbcEntity()
            .where().entityNotDeleted()
            .and().entityTypeIsWell()
            .and().filterByJsonB()
            .and().filterByJsonB()
            .orderBy().selectVersion().descending()
            .build();

        return getJdbcEntity(sql, buildExistenceKindParam(existenceKind), buildFacilityNameParam(name))
            .stream()
            .findFirst();
    }

    private List<JdbcEntity> getJdbcEntity(String sqlQuery, Object... args) {
        List<JdbcEntity> entities = null;
        try {
            entities = jdbcTemplate.query(sqlQuery,
                args,
                new BeanPropertyRowMapper<>(JdbcEntity.class));
        } catch (DataAccessException e) {
            if (log.isWarnEnabled()) {
                log.warn("Database access was finished with error: {}", e.getMessage());
            }
            return Collections.emptyList();
        }
        return entities;
    }
}
