/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import org.opengroup.osdu.core.client.model.http.AppException;


import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;
import org.opengroup.osdu.wd.core.models.ENTITY_TYPE;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.cloudant.client.api.Database;

import com.google.gson.JsonObject;


@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "cloudantdb", matchIfMissing = true)
public class CloudantQueryClient implements IQueryClient {

	 @Autowired
	 CloudantdbInit cloudantInit;
	 @Autowired
	 CloudantTreeTraversal treeTraversal;

    //BHARun

    @Override
    public List<EntityDtoReturn> getBHARunsBySection(String existenceKind, String segmentEntityId) {
       Database entityDb = cloudantInit.getEntityDB(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(entityDb, existenceKind, ENTITY_TYPE.HOLE_SECTION, segmentEntityId);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(entityDb, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

       Database entityDb = cloudantInit.getEntityDB(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> runIdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedId(entityDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids.size() == 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find BHA run with wellbore id: " + wellboreEntityId);
            }
            runIdList.addAll(ids);
        }
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(entityDb, runIdList);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public List<EntityDtoReturn> getBHARunsByWellbore(String existenceKind, String wellboreEntityId) {
       Database entityDb = cloudantInit.getEntityDB(ENTITY_TYPE.BHA_RUN.toLowerCase());
        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(entityDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(entityDb, ids);
        return toEntityDtoReturnList(docs);
    }

    //WellboreSegment

    @Override
    public List<EntityDtoReturn> getHoleSectionsByWellbore(String existenceKind, String wellboreEntityId) {
        Database segmentDB = cloudantInit.getEntityDB(ENTITY_TYPE.HOLE_SECTION.toLowerCase());
        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(segmentDB, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(segmentDB, ids);
        return toEntityDtoReturnList(docs);
    }

    //WellboreTrajectory

    @Override
    public List<EntityDtoReturn> getWellboreTrajectoriesByWells(String existenceKind, List<String> wellEntityIds) {
        List<String> wellboreIds = getWellboreIdsByWells(existenceKind, wellEntityIds);

        Database trajectoryDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE_TRAJECTORY.toLowerCase());
        List<String> IdList = new ArrayList<>();
        for (String wellboreId : wellboreIds) {
            List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedId(trajectoryDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreId);
            if (ids.size() == 0) {
                String wellboreEntityId = wellboreId.split(":")[0];
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore trajectory with wellbore id: " + wellboreEntityId);
            }
            IdList.addAll(ids);
        }
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(trajectoryDb, IdList);
        return toEntityDtoReturnList(docs);
    }


    //ActivityPlan

    @Override
    public EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database activityPlanDb = cloudantInit.getEntityDB(ENTITY_TYPE.ACTIVITY_PLAN.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(activityPlanDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null )
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest activity plan with well id: " + wellEntityId);

          return entity.ToEntityDtoReturn();
    }

    //WellActivityProgram

    @Override
    public EntityDtoReturn getLatestWellActivityProgramByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(dpDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

         return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellActivityProgramByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getSpecificEntity_ByRelatedEntityId(dpDb, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        return entity.ToEntityDtoReturn();
    }

    @Override
    public Object getLatestWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(dpDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        JsonObject tree = treeTraversal.buildDocumentRef(entity);
        return tree;
    }

    @Override
    public Object getSpecificWellActivityProgramRefTreeByWell(String existenceKind, String wellEntityId, long dpVersion) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getSpecificEntity_ByRelatedEntityId(dpDb, existenceKind, dpVersion, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well activity program with well id: " + wellEntityId + " well activity program version : " + dpVersion);

        JsonObject tree = treeTraversal.buildDocumentRef(entity);
        return tree;
    }

    @Override
    public List<Long> getWellActivityProgramVersionNumbersByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        List<Long> list = CloudantdbFacade.getEntityVersionNumberList_ByRelatedEntityId(dpCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        return list;
    }

    @Override
    public List<EntityDtoReturn> getLatestWellActivityProgramChildrenListByWell(String existenceKind, String wellEntityId) {
        String wellboreEntityId = getWellboreEntityIdByWell(existenceKind, wellEntityId);

        Database dpDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELL_ACTIVITY_PROGRAM.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(dpDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well activity program with well id: " + wellEntityId);

        List<CloudantEntity> tree = treeTraversal.buildDocumentList(entity);
        List<EntityDtoReturn> res = new ArrayList<>();
        for (CloudantEntity item : tree) {         
            EntityDtoReturn ret = item.ToEntityDtoReturn();
            res.add(ret);
        }
        return res;
    }

    //Wellbore

    @Override
    public List<Long> getWellboreVersionNumbersByWell(String existenceKind, String wellEntityId) {
        Database wellboreCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<Long> list = CloudantdbFacade.getEntityVersionNumberList_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellboreVersionByWell(String existenceKind, String wellEntityId) {
        Database wellboreCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(wellboreCollection, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (entity == null )
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest wellbore with well id: " + wellEntityId);

        
        return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellboreVersionByWell(String existenceKind, String wellId, long wellboreVersion) {
        Database wellboreCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getSpecificEntity_ByRelatedEntityId(wellboreCollection, existenceKind, wellboreVersion, ENTITY_TYPE.WELL, wellId);
        if (entity == null )
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific wellbore with well id: " + wellId + " wellbore version : " + wellboreVersion);

        return entity.ToEntityDtoReturn();
    }

    //Well

    @Override
    public List<Long> getWellVersionNumbers(String existenceKind, String name) {
        Database wellboreCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELL.toLowerCase());
        List<Long> list = CloudantdbFacade.getEntity_VersionNumberList_ByName(wellboreCollection, existenceKind, name);
        return list;
    }

    @Override
    public EntityDtoReturn getLatestWellVersion(String existenceKind, String name) {
        Database wellCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELL.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatesEntity_ByName(wellCollection, existenceKind, name);
        if (entity == null )
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest well with name: " + name);

         return entity.ToEntityDtoReturn();
    }

    @Override
    public EntityDtoReturn getSpecificWellVersion(String existenceKind, String name, long version) {
        Database wellCollection = cloudantInit.getEntityDB(ENTITY_TYPE.WELL.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getSpecificEntity_ByName(wellCollection, existenceKind, name, version);
        if (entity == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find specific well with name: " + name + " well version : " + version);

        
        return entity.ToEntityDtoReturn();
    }

    //DrillingReport

    @Override
    public List<EntityDtoReturn> getDrillingReportsByWellbore(String existenceKind, String wellboreEntityId) {
        Database drCollection = cloudantInit.getEntityDB(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public EntityDtoReturn getLatestDrillingReportByWellbore(String existenceKind, String wellboreEntityId) {
        Database drCollection = cloudantInit.getEntityDB(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        CloudantEntity entity = CloudantdbFacade.getLatestEntity_ByRelatedEntityId(drCollection, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        if (entity == null )
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find latest drill report with wellbore id: " + wellboreEntityId);

      
        return entity.ToEntityDtoReturn();
    }

    @Override
    public List<EntityDtoReturn> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString) {
        Database drCollection = cloudantInit.getEntityDB(ENTITY_TYPE.DRILL_REPORT.toLowerCase());

        try {
            LocalDateTime startTime = DateTimeUtil.parseWithException(startTimeString);
            LocalDateTime endTime = DateTimeUtil.parseWithException(endTimeString);
            startTimeString = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endTimeString = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad request", " Invalid date format");
        }

        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByTimeRange(drCollection, existenceKind, startTimeString, endTimeString);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(drCollection, ids);
        return toEntityDtoReturnList(docs);
    }

    @Override
    public Object getLatestDrillingReportRefTree(String existenceKind, String drEntityId) {
        Database drEntityDb = cloudantInit.getEntityDB(ENTITY_TYPE.DRILL_REPORT.toLowerCase());
        CloudantEntity doc = CloudantdbFacade.findLatestOne(drEntityDb, drEntityId);
        if (doc == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find drill report with id: " + drEntityId);

        JsonObject tree = treeTraversal.buildDocumentRef(doc);
        return tree;
    }

    //FluidsReports

    @Override
    public List<EntityDtoReturn> getFluidsReportsByWellbore(String existenceKind, String wellboreEntityId) {
        Database entityDb = cloudantInit.getEntityDB(ENTITY_TYPE.FLUIDS_REPORT.toLowerCase());
        List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(entityDb, existenceKind, ENTITY_TYPE.WELLBORE, wellboreEntityId);
        List<CloudantEntity> docs = CloudantdbFacade.getEntityList_ByIdList(entityDb, ids);
        return toEntityDtoReturnList(docs);
    }

    private String getWellboreEntityIdByWell(String existenceKind, String wellEntityId) {
        Database wellboreDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE.toLowerCase());
        String wellboreId = CloudantdbFacade.getLatestId_ByRelatedEntityId(wellboreDb, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
        if (wellboreId == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
        return wellboreId.split(":")[0];
    }

    private List<String> getWellboreIdsByWells(String existenceKind, List<String> wellEntityIds) {
        Database wellboreDb = cloudantInit.getEntityDB(ENTITY_TYPE.WELLBORE.toLowerCase());
        List<String> wellboreIds = new ArrayList<>();
        for (String wellEntityId : wellEntityIds) {
            List<String> ids = CloudantdbFacade.getIdList_LatestPerEntity_ByRelatedEntityId(wellboreDb, existenceKind, ENTITY_TYPE.WELL, wellEntityId);
            if (ids.size() == 0)
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Not found", "Could not find wellbore with well id: " + wellEntityId);
            wellboreIds.addAll(ids);
        }
        return wellboreIds;
    }

    private List<EntityDtoReturn> toEntityDtoReturnList(List<CloudantEntity> docs) {
        List<EntityDtoReturn> res = new ArrayList<>();
        for (CloudantEntity doc : docs) {
            if (doc != null) {
                //MongoEntity entity = MongoEntity.ToMongoEntity(doc);
                res.add(doc.ToEntityDtoReturn());
            }
        }
        return res;
    }

    private CloudantEntity getLatestEntity(Database entitDb, String entityId) {
        CloudantEntity entity = CloudantdbFacade.findLatestOne(entitDb, entityId);
        if (entity == null )
            return null;
       
        return entity;
    }

}
