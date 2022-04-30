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

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.models.ENTITY_TYPE;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;
import org.opengroup.osdu.wd.core.util.RecordConversion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "mongodb", matchIfMissing = true)
public class MongoQueryClient implements IQueryClient {

    @Autowired
    MongodbInit mongodbInit;
    @Autowired
    RecordConversion conversion;
    @Autowired
    MongoTreeTraversal treeTraversal;

    //BHARun

    @Override
    public List<EntityDtoReturn> getBHARunsBySection(String existenceKind, String segmentEntityId) {
        MongoCollection<Document> runCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(runCollection, existenceKind, ENTITY_TYPE.HOLE_SECTION, segmentEntityId);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(runCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

        MongoCollection<Document> runCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> runIdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedId(runCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids.size() == 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find BHA run with wellbore id: " + wellboreEntityId);
            }
            runIdList.addAll(ids);
        }
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(runCollection, runIdList);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWellbore(String existenceKind, String wellboreEntityId) {
        MongoCollection<Document> runCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(runCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(runCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    //WellboreSegment

    @Override
    public List<EntityDtoReturn> getHoleSectionsByWellbore(String existenceKind, String wellboreEntityId) {
        MongoCollection<Document> segmentCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.HOLE_SECTION.toLowerCase());
        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(segmentCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(segmentCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    //WellboreTrajectory

    @Override
    public List<EntityDtoReturn> getWellboreTrajectoriesByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

        MongoCollection<Document> trajectoryCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE_TRAJECTORY.toLowerCase());
        List<String> IdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedId(trajectoryCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids.size() == 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore trajectory with wellbore id: " + wellboreEntityId);
            }
            IdList.addAll(ids);
        }
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(trajectoryCollection, IdList);
        return toEntityDtoReturnList(docs);
    }


    //ActivityPlan

    @Override
    public EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> planCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.ACTIVITY_PLAN.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(planCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest activity plan with well id: " + wellEntityId);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    //WellActivityProgram

    @Override
    public EntityDtoReturn getLatestWellActivityProgramByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellActivityProgramByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        Document doc = MongodbFacade.getSpecificEntity_ByRelatedEntityId(dpCollection, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public Object getLatestWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        JsonObject tree = treeTraversal.buildDocumentRef(doc);
        return tree;
    }

    @Override
    public Object getSpecificWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        Document doc = MongodbFacade.getSpecificEntity_ByRelatedEntityId(dpCollection, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        JsonObject tree = treeTraversal.buildDocumentRef(doc);
        return tree;
    }

    @Override
    public List<Long> getWellActivityProgramVersionNumbersByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        List<Long> list = MongodbFacade.getEntityVersionNumberList_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        return list;
    }

    @Override
    public List<EntityDtoReturn> getLatestWellActivityProgramChildrenListByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        MongoCollection<Document> dpCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        List<Document> tree = treeTraversal.buildDocumentList(doc);
        List<EntityDtoReturn> res = new ArrayList<>();
        for (Document item : tree) {
            MongoEntity entity = MongoEntity.ToMongoEntity(item);
            EntityDtoReturn ret = entity.ToEntityDtoReturn();
            res.add(ret);
        }
        return res;
    }

    //Wellbore

    @Override
    public List<Long> getWellboreVersionNumbersByWell(String existenceKind, String wellEntityId) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<Long> list = MongodbFacade.getEntityVersionNumberList_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellboreVersionByWell(String existenceKind, String wellEntityId) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest wellbore with well id: " + wellEntityId);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellboreVersionByWell(String existenceKind, String wellId, long wellboreVersion) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE.toLowerCase());
        Document doc = MongodbFacade.getSpecificEntity_ByRelatedEntityId(wellboreCollection, existenceKind, wellboreVersion, ENTITY_TYPE.WELL, wellId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific wellbore with well id: " + wellId + " wellbore version : " + wellboreVersion);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    //Well

    @Override
    public List<Long> getWellVersionNumbers(String existenceKind, String name) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL.toLowerCase());
        List<Long> list = MongodbFacade.getEntity_VersionNumberList_ByName(wellboreCollection, existenceKind, name);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellVersion(String existenceKind, String name) {
        MongoCollection<Document> wellCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL.toLowerCase());
        Document doc = MongodbFacade.getLatesEntity_ByName(wellCollection, existenceKind, name);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well with name: " + name);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellVersion(String existenceKind, String name, long version) {
        MongoCollection<Document> wellCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELL.toLowerCase());
        Document doc = MongodbFacade.getSpecificEntity_ByName(wellCollection, existenceKind, name, version);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well with name: " + name + " well version : " + version);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    //DrillingReport

    @Override
    public List<EntityDtoReturn> getDrillingReportsByWellbore(String existenceKind, String wellboreEntityId) {
        MongoCollection<Document> drCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public EntityDtoReturn getLatestDrillingReportByWellbore(String existenceKind, String wellboreEntityId) {
        MongoCollection<Document> drCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        Document doc = MongodbFacade.getLatestEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest drill report with wellbore id: " + wellboreEntityId);

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity.ToEntityDtoReturn();
    }

    @Override
    public List<EntityDtoReturn> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString) {
        MongoCollection<Document> drCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.DRILL_REPORT.toLowerCase());

        try {
            LocalDateTime startTime = DateTimeUtil.parseWithException(startTimeString);
            LocalDateTime endTime = DateTimeUtil.parseWithException(endTimeString);
            startTimeString = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endTimeString = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", " Invalid date format");
        }

        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByTimeRange(drCollection, existenceKind, startTimeString, endTimeString);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public Object getLatestDrillingReportRefTree(String existenceKind, String drEntityId) {
        MongoCollection<Document> drCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        Document doc = MongodbFacade.findLatestOne(drCollection, drEntityId);
        if (doc == null || doc.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find drill report with id: " + drEntityId);

        JsonObject tree = treeTraversal.buildDocumentRef(doc);
        return tree;
    }

    //FluidsReports

    @Override
    public List<EntityDtoReturn> getFluidsReportsByWellbore(String existenceKind, String wellboreEntityId) {
        MongoCollection<Document> dfCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.FLUIDS_REPORT.toLowerCase());
        List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(dfCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<Document> docs = MongodbFacade.getEntityList_ByIdList(dfCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    private String getWellboreEntityIdByWell(String existenceKind, String wellEntityId) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE.toLowerCase());
        String wellboreId = MongodbFacade.getLatestId_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (wellboreId == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
        return wellboreId.split(":")[0];
    }

    private List<String> getWellboreIdsByWells(String existenceKind, List<String> wellEntityIds) {
        MongoCollection<Document> wellboreCollection = mongodbInit.getEntityCollection(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<String> wellboreIds = new ArrayList<>();
        for (String wellEntityId : wellEntityIds) {
            List<String> ids = MongodbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
            if (ids.size() == 0)
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
            wellboreIds.addAll(ids);
        }
        return wellboreIds;
    }

    private List<EntityDtoReturn> toEntityDtoReturnList(List<Document> docs) {
        List<EntityDtoReturn> res = new ArrayList<>();
        for (Document doc : docs) {
            if (doc != null && !doc.isEmpty()) {
                MongoEntity entity = MongoEntity.ToMongoEntity(doc);
                res.add(entity.ToEntityDtoReturn());
            }
        }
        return res;
    }

    private MongoEntity getLatestEntity(MongoCollection<Document> collection, String entityId) {
        Document doc = MongodbFacade.findLatestOne(collection, entityId);
        if (doc == null || doc.isEmpty())
            return null;

        MongoEntity entity = MongoEntity.ToMongoEntity(doc);
        return entity;
    }
}
