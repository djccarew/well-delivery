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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.ACTIVITY_PLAN;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.BHA_RUN;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.HOLE_SECTION;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELL;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELLBORE;
import static org.opengroup.osdu.wd.core.models.ENTITY_TYPE.WELL_ACTIVITY_PROGRAM;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.JdbcEntityRepository;
import org.opengroup.osdu.wd.gcp.model.JdbcEntity;

@RunWith(MockitoJUnitRunner.class)
public class GoogleQueryClientTest {

    private static final String WELL_ID = "well-1";

    private static final String ACTIVITY_PLAN_ID = "activityplan-1";

    private static final String WELLBORE_ID = "wellbore-1";

    private static final String ID = "test_id";

    public static final String EXISTENCE_KIND_PLANNED = "planned";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JdbcEntityRepository jdbcEntityRepository;

    private JdbcTreeTraversal treeTraversal;

    private GoogleQueryClient queryClient;

    private JdbcEntity drillingReportEntity;
    private JdbcEntity wellboreEntity;
    private JdbcEntity wellEntity;
    private JsonObject drillingReportRefTreeFromFile;
    private JsonObject activityProgramRefTreeFromFile;
    private JdbcEntity activityProgramEntity;
    private JdbcEntity activityPlanEntity;
    private JdbcEntity bharunEntity;
    private EntityDto wellboreEntityDto;
    private EntityDtoReturn bharunEntityDtoReturn;

    @Before
    public void setUp() throws IOException {
        treeTraversal = new JdbcTreeTraversal(jdbcEntityRepository);
        queryClient = new GoogleQueryClient(jdbcEntityRepository, treeTraversal);
        drillingReportEntity =
            getJdbcEntityFromFile("/entities/drillingreport/drillingreport.json", "/entities/drillingreport/drillingreport-relationships.json");
        wellboreEntity = getJdbcEntityFromFile("/entities/wellbore/wellbore.json", "/entities/wellbore/wellbore-relationships.json");
        wellEntity = getJdbcEntityFromFile("/entities/well/well.json", "/entities/well/well-relationships.json");
        drillingReportRefTreeFromFile = getRefTreeFromFile("/entities/drillingreport/latest-drillingreport-reftree.json");
        activityProgramRefTreeFromFile = getRefTreeFromFile("/entities/activityprogram/activityprogram-reftree.json");
        activityProgramEntity =
            getJdbcEntityFromFile("/entities/activityprogram/activityprogram.json", "/entities/activityprogram/activityprogram-relationships.json");
        activityPlanEntity = getJdbcEntityFromFile("/entities/activityplan/activityplan.json", "/entities/activityplan/activityplan-relationships.json");
        bharunEntity = getJdbcEntityFromFile("/entities/bharun/bharun.json", "/entities/bharun/bhrarun-relationships.json");
        wellboreEntityDto = getEntityDtoFromFile("/entities/wellbore/wellbore.json");
        bharunEntityDtoReturn = getEntityDtoReturnFromFile("/entities/bharun/bharun.json");
    }

    @Test
    public void getBHARunsBySegmentShouldReturnValidEntityDtoReturnWhenPresent() {
        when(jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityId(BHA_RUN, EXISTENCE_KIND_PLANNED, HOLE_SECTION, ID))
            .thenReturn(Collections.singletonList(bharunEntity));
        List<EntityDtoReturn> bhaRunsBySegment = queryClient.getBHARunsBySection(EXISTENCE_KIND_PLANNED, ID);
        assertEquals(bharunEntityDtoReturn, bhaRunsBySegment.get(0));
    }

    @Test
    public void getBHARunsBySegmentShouldReturnNotValidEntityDtoReturnWhenPresent() {
        when(jdbcEntityRepository.getEntitiesByExistenceKindAndRelationshipEntityId(BHA_RUN, EXISTENCE_KIND_PLANNED, HOLE_SECTION, ID))
            .thenReturn(Collections.singletonList(bharunEntity));
        List<EntityDtoReturn> bhaRunsBySegment = queryClient.getBHARunsBySection(EXISTENCE_KIND_PLANNED, ID);
        assertEquals(bharunEntityDtoReturn, bhaRunsBySegment.get(0));
    }

    @Test
    public void testGetLatestDrillingReportRefTree() {
        when(jdbcEntityRepository.getLatestDrillingReport(any(), any())).thenReturn(Optional.of(drillingReportEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELLBORE, WELLBORE_ID)).thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELL, WELL_ID)).thenReturn(Optional.of(wellEntity));

        Object latestDrillingReportRefTree = queryClient.getLatestDrillingReportRefTree(any(), any());
        assertEquals(drillingReportRefTreeFromFile, latestDrillingReportRefTree);
    }

    @Test
    public void testGetLatestWellActivityProgramRefTreeByWell() {
        when(jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELLBORE, EXISTENCE_KIND_PLANNED, WELL, WELL_ID))
            .thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestActivityProgramByExistenceKindAndWellId(EXISTENCE_KIND_PLANNED, WELLBORE_ID))
            .thenReturn(Optional.of(activityProgramEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELLBORE, WELLBORE_ID)).thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELL, WELL_ID)).thenReturn(Optional.of(wellEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(ACTIVITY_PLAN.toLowerCase(), ACTIVITY_PLAN_ID)).thenReturn(Optional.of(activityPlanEntity));

        Object latestWellActivityProgramRefTreeByWell = queryClient.getLatestWellActivityProgramRefTreeByWell(EXISTENCE_KIND_PLANNED, WELL_ID);
        assertEquals(activityProgramRefTreeFromFile, latestWellActivityProgramRefTreeByWell);
    }

    @Test
    public void testGetSpecificWellActivityProgramRefTreeByWell() {
        when(jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELLBORE, EXISTENCE_KIND_PLANNED, WELL, WELL_ID))
            .thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository
            .getSpecificEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, EXISTENCE_KIND_PLANNED, 1L, WELLBORE, WELLBORE_ID))
            .thenReturn(Optional.of(activityProgramEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELLBORE, WELLBORE_ID)).thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELL, WELL_ID)).thenReturn(Optional.of(wellEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(ACTIVITY_PLAN.toLowerCase(), ACTIVITY_PLAN_ID)).thenReturn(Optional.of(activityPlanEntity));
        Object specificWellActivityProgramRefTreeByWell = queryClient.getSpecificWellActivityProgramRefTreeByWell(EXISTENCE_KIND_PLANNED, WELL_ID, 1L);
        assertEquals(activityProgramRefTreeFromFile, specificWellActivityProgramRefTreeByWell);
    }

    @Test
    public void testGetLatestWellActivityProgramChildrenListByWell() throws IOException {
        when(jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELLBORE, EXISTENCE_KIND_PLANNED, WELL, WELL_ID))
            .thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestEntityByExistenceKindAndRelationshipEntityId(WELL_ACTIVITY_PROGRAM, EXISTENCE_KIND_PLANNED, WELLBORE, WELLBORE_ID))
            .thenReturn(Optional.of(activityProgramEntity));

        when(jdbcEntityRepository.getLatestByIdAndType(WELLBORE, WELLBORE_ID)).thenReturn(Optional.of(wellboreEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(WELL, WELL_ID)).thenReturn(Optional.of(wellEntity));
        when(jdbcEntityRepository.getLatestByIdAndType(ACTIVITY_PLAN.toLowerCase(), ACTIVITY_PLAN_ID)).thenReturn(Optional.of(activityPlanEntity));

        ArrayList<EntityDtoReturn> entityDtoReturns = new ArrayList<>();
        entityDtoReturns.add(getEntityDtoReturnFromFile("/entities/wellbore/wellbore.json"));
        entityDtoReturns.add(getEntityDtoReturnFromFile("/entities/activityprogram/activityprogram.json"));
        entityDtoReturns.add(getEntityDtoReturnFromFile("/entities/well/well.json"));
        entityDtoReturns.add(getEntityDtoReturnFromFile("/entities/activityplan/activityplan.json"));

        List<EntityDtoReturn> latestWellActivityProgramChildrenListByWell =
            queryClient.getLatestWellActivityProgramChildrenListByWell(EXISTENCE_KIND_PLANNED, WELL_ID);

        assertTrue(
            entityDtoReturns.size() == latestWellActivityProgramChildrenListByWell.size()
                && entityDtoReturns.containsAll(latestWellActivityProgramChildrenListByWell)
                && latestWellActivityProgramChildrenListByWell.containsAll(entityDtoReturns)
        );
    }


    @Test(expected = AppException.class)
    public void getBHARunsByWellsShouldThrowExceptionWhenNoBhaRunsByWellbore() {
        queryClient.getBHARunsByWells(EXISTENCE_KIND_PLANNED, Collections.singletonList(ID));
    }

    private JdbcEntity getJdbcEntityFromFile(String entityJsonPath, String relationShipPath) throws IOException {
        InputStream entityDtoJsonStream = GoogleQueryClientTest.class.getResourceAsStream(entityJsonPath);
        CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Relationship.class);
        InputStream relationshipsJsonStream = GoogleQueryClientTest.class.getResourceAsStream(relationShipPath);
        List<Relationship> relationships = objectMapper.readValue(relationshipsJsonStream, type);

        EntityDto entityDto = objectMapper.readValue(entityDtoJsonStream, EntityDto.class);
        return new JdbcEntity(1L, entityDto, relationships);
    }

    private EntityDto getEntityDtoFromFile(String entityJsonPath) throws IOException {
        InputStream entityDtoJsonStream = GoogleQueryClientTest.class.getResourceAsStream(entityJsonPath);
        return objectMapper.readValue(entityDtoJsonStream, EntityDto.class);
    }

    private EntityDtoReturn getEntityDtoReturnFromFile(String entityJsonPath) throws IOException {
        InputStream entityDtoJsonStream = GoogleQueryClientTest.class.getResourceAsStream(entityJsonPath);
        EntityDto entityDto = objectMapper.readValue(entityDtoJsonStream, EntityDto.class);
        return new EntityDtoReturn(entityDto);
    }

    private JsonObject getRefTreeFromFile(String refTreePath) {
        InputStream refTreeStream = GoogleQueryClientTest.class.getResourceAsStream(refTreePath);
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(refTreeStream));
        return jsonElement.getAsJsonObject();
    }

}