/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;

import java.net.MalformedURLException;
import java.util.List;

import org.bson.Document;
import org.opengroup.osdu.wd.core.dataaccess.impl.MongoEntity;

import org.opengroup.osdu.wd.core.dataaccess.interfaces.IEntityDBClient;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.core.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;


@Service
@ConditionalOnProperty(name = "app.entity.source", havingValue = "cloudantdb", matchIfMissing = true)
public class CloudantEntityClient implements IEntityDBClient {

    @Autowired
    private CloudantdbInit cloudantInit;

	@Override
	public EntityDtoReturn saveEntity(EntityDto dto, List<Relationship> relationships) {
			
				Database entityDB=cloudantInit.getEntityDB(dto.getEntityType());
				CloudantEntity dbEntity=new CloudantEntity(dto,relationships);
				Response response=entityDB.save(dbEntity);
					
		return null;
	}

	 @Override
	    public EntityDtoReturn getLatestEntityVersion(String entityType, String entityId) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        CloudantEntity doc = CloudantdbFacade.findLatestOne(entityDb, entityId);
	        if (doc == null)
	            return null;	      
	        return doc.ToEntityDtoReturn();
	    }

	    @Override
	    public EntityDtoReturn getSpecificEntityVersion(String entityType, String entityId, long version) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        String _id = Common.buildId(entityId, version);
	        CloudantEntity doc = CloudantdbFacade.findSpecificOne(entityDb, _id);
	        if (doc == null )
	            return null;

	        
	        return doc.ToEntityDtoReturn();
	    }

	    @Override
	    public List<Long> getEntityVersionNumbers(String entityType, String entityId) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        List<Long> list = CloudantdbFacade.findVersionNumbers(entityDb, entityId);
	        return list;
	    }

	    @Override
	    public long deleteEntity(String entityType, String entityId) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        long cnt = CloudantdbFacade.deleteEntity(entityDb, entityId);
	        return cnt;
	    }

	    @Override
	    public long purgeEntity(String entityType, String entityId) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        long cnt = CloudantdbFacade.purgeEntity(entityDb, entityId);
	        return cnt;
	    }

	    @Override
	    public long deleteEntityVersion(String entityType, String entityId, long version) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        String _id = Common.buildId(entityId, version);
	        int cnt = CloudantdbFacade.deleteOne(entityDb, _id);
	        return cnt;
	    }

	    @Override
	    public long purgeEntityVersion(String entityType, String entityId, long version) {
	        Database entityDb = cloudantInit.getEntityDB(entityType);

	        String _id = Common.buildId(entityId, version);
	        int cnt = CloudantdbFacade.purgeOne(entityDb, _id);
	        return cnt;
	    }
	}

