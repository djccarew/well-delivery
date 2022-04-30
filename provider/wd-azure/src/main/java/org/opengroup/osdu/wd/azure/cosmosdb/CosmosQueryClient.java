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
import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.ENTITY_TYPE;
import org.opengroup.osdu.wd.core.util.DateTimeUtil;
import org.opengroup.osdu.wd.core.util.RecordConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "cosmosdb", matchIfMissing = true)
public class CosmosQueryClient implements IQueryClient {

    @Autowired
    CosmosdbInit cosmosdbInit;
    @Autowired
    RecordConversion conversion;
    @Autowired
    CosmosTreeTraversal treeTraversal;

    //BHARun

    @Override
    public List<EntityDtoReturn> getBHARunsBySection(String existenceKind, String segmentEntityId) {
        CosmosContainer runCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(runCollection, existenceKind, ENTITY_TYPE.HOLE_SECTION, segmentEntityId);
        List<CosmosEntity> entityList = CosmosdbFacade.getEntityList_ByIdList(runCollection, ids);
        return toEntityDtoReturnList(entityList);
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

        CosmosContainer runContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> runIdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedId(runContainer, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids == null || ids.size() <= 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find BHA run with wellbore id: " + wellboreEntityId);
            }
            runIdList.addAll(ids);
        }
        List<CosmosEntity> entityList = CosmosdbFacade.getEntityList_ByIdList(runContainer, runIdList);
        return toEntityDtoReturnList(entityList);
    }


    @Override
    public List<EntityDtoReturn> getBHARunsByWellbore(String existenceKind, String wellboreEntityId) {
        CosmosContainer runContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(runContainer, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CosmosEntity> entityList = CosmosdbFacade.getEntityList_ByIdList(runContainer, ids);
        return toEntityDtoReturnList(entityList);
    }

    //WellboreSegment

    @Override
    public List<EntityDtoReturn> getHoleSectionsByWellbore(String existenceKind, String wellboreEntityId) {
        CosmosContainer segmentContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.HOLE_SECTION.toLowerCase());
        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(segmentContainer, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CosmosEntity> entityList = CosmosdbFacade.getEntityList_ByIdList(segmentContainer, ids);
        return toEntityDtoReturnList(entityList);
    }

    //WellboreTrajectory

    @Override
    public List<EntityDtoReturn> getWellboreTrajectoriesByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

        CosmosContainer trajectoryContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE_TRAJECTORY.toLowerCase());
        List<String> IdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedId(trajectoryContainer, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids == null || ids.size() <= 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore trajectory with wellbore id: " + wellboreEntityId);
            }
            IdList.addAll(ids);
        }
        List<CosmosEntity> entityList = CosmosdbFacade.getEntityList_ByIdList(trajectoryContainer, IdList);
        return toEntityDtoReturnList(entityList);
    }

    //ActivityPlan

    @Override
    public EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer planContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.ACTIVITY_PLAN.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(planContainer, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest activity plan with well id: " + wellEntityId);
        return entity.ToEntityDtoReturn();
    }

    //WellActivityProgram

    @Override
    public EntityDtoReturn getLatestWellActivityProgramByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellActivityProgramByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getSpecificEntity_ByRelatedEntityId(dpCollection, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public Object getLatestWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        JsonObject tree = treeTraversal.buildEntityRef(entity);
        return tree;
    }

    @Override
    public Object getSpecificWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getSpecificEntity_ByRelatedEntityId(dpCollection, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        JsonObject tree = treeTraversal.buildEntityRef(entity);
        return tree;
    }

    @Override
    public List<Long> getWellActivityProgramVersionNumbersByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        List<Long> list = CosmosdbFacade.getEntityVersionNumberList_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        return list;
    }

    @Override
    public List<EntityDtoReturn> getLatestWellActivityProgramChildrenListByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        CosmosContainer dpCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        List<CosmosEntity> tree = treeTraversal.buildEntityList(entity);
        List<EntityDtoReturn> res = new ArrayList<>();
        for (CosmosEntity item : tree) {
            EntityDtoReturn ret = entity.ToEntityDtoReturn();
            res.add(ret);
        }
        return res;
    }

    //Wellbore

    @Override
    public List<Long> getWellboreVersionNumbersByWell(String existenceKind, String wellEntityId) {
        CosmosContainer wellboreCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<Long> list = CosmosdbFacade.getEntityVersionNumberList_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellboreVersionByWell(String existenceKind, String wellEntityId) {
        CosmosContainer wellboreCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest wellbore with well id: " + wellEntityId);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellboreVersionByWell(String existenceKind, String wellId, long wellboreVersion) {
        CosmosContainer wellboreCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getSpecificEntity_ByRelatedEntityId(wellboreCollection, existenceKind, wellboreVersion, ENTITY_TYPE.WELL, wellId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific wellbore with well id: " + wellId + " wellbore version : " + wellboreVersion);

        return entity.ToEntityDtoReturn();
    }

    //Well

    @Override
    public List<Long> getWellVersionNumbers(String existenceKind, String name) {
        CosmosContainer wellboreContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL.toLowerCase());
        List<Long> list = CosmosdbFacade.getEntityVersionNumberList_ByName(wellboreContainer, existenceKind, name);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellVersion(String existenceKind, String name) {
        CosmosContainer wellContainer = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByName(wellContainer, existenceKind, name);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well with name: " + name);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellVersion(String existenceKind, String name, long version) {
        CosmosContainer wellCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELL.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getSpecificEntity_ByName(wellCollection, existenceKind, name, version);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well with name: " + name + " well version : " + version);

        return entity.ToEntityDtoReturn();
    }

    //DrillingReport

    @Override
    public List<EntityDtoReturn> getDrillingReportsByWellbore(String existenceKind, String wellboreEntityId) {
        CosmosContainer drCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CosmosEntity> docs = CosmosdbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public EntityDtoReturn getLatestDrillingReportByWellbore(String existenceKind, String wellboreEntityId) {
        CosmosContainer drCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.getLatestEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest drill report with wellbore id: " + wellboreEntityId);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public List<EntityDtoReturn> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString) {
        CosmosContainer drCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.DRILL_REPORT.toLowerCase());

        try {
            LocalDateTime startTime = DateTimeUtil.parseWithException(startTimeString);
            LocalDateTime endTime = DateTimeUtil.parseWithException(endTimeString);
            startTimeString = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endTimeString = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", " Invalid date format");
        }

        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByTimeRange(drCollection, existenceKind, startTimeString, endTimeString);
        List<CosmosEntity> docs = CosmosdbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public Object getLatestDrillingReportRefTree(String existenceKind, String drEntityId) {
        CosmosContainer drCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        CosmosEntity entity = CosmosdbFacade.findLatestItem(drCollection, drEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find drill report with id: " + drEntityId);

        JsonObject tree = treeTraversal.buildEntityRef(entity);
        return tree;
    }

    //FluidsReports

    @Override
    public List<EntityDtoReturn> getFluidsReportsByWellbore(String existenceKind, String wellboreEntityId) {
        CosmosContainer dfCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.FLUIDS_REPORT.toLowerCase());
        List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(dfCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CosmosEntity> docs = CosmosdbFacade.getEntityList_ByIdList(dfCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    private String getWellboreEntityIdByWell(String existenceKind, String wellEntityId) {
        CosmosContainer wellboreCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE.toLowerCase());
        String wellboreId = CosmosdbFacade.getLatestId_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (wellboreId == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
        return wellboreId.split(":")[0];
    }

    private List<String> getWellboreIdsByWells(String existenceKind, List<String> wellEntityIds) {
        CosmosContainer wellboreCollection = cosmosdbInit.getEntityContainer(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<String> wellboreIds = new ArrayList<>();
        for (String wellEntityId : wellEntityIds) {
            List<String> ids = CosmosdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
            if (ids == null || ids.size() <= 0)
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
            wellboreIds.addAll(ids);
        }
        return wellboreIds;
    }

    private List<EntityDtoReturn> toEntityDtoReturnList(List<CosmosEntity> entityList) {
        List<EntityDtoReturn> res = new ArrayList<>();
        for (CosmosEntity entity : entityList) {
            res.add(entity.ToEntityDtoReturn());
        }
        return res;
    }

    private CosmosEntity getLatestEntity(CosmosContainer container, String entityId) {
        CosmosEntity entity = CosmosdbFacade.findLatestItem(container, entityId);
        if (entity == null)
            return null;
        return entity;
    }
}
