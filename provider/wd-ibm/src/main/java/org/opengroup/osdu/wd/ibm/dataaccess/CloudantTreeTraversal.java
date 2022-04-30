/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.cloudant.client.api.Database;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component
@RequestScope
public class CloudantTreeTraversal { 
	@Autowired
    CloudantdbInit cloudantdbInit;

    private Set<String> idSet;

    public CloudantTreeTraversal()
    {
        this.idSet = new HashSet<>();
    }

    public JsonObject buildDocumentRef(CloudantEntity doc) {
        JsonObject res = new JsonObject();

        String entityId = doc.get_id();
        String entityType = doc.getEntityType();
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);

        res.addProperty("id", entityId);
        res.addProperty("version", doc.getVersion());
        res.addProperty("type", entityType);

        List<Relationship> relationships = doc.getRelationships();
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

            Database entityDb = cloudantdbInit.getEntityDB(type);
            if(entityDb != null) {
                CloudantEntity sub = getSpecificDocument(entityDb, id);
                if (sub != null ) {
                    JsonObject children = buildDocumentRef(sub);
                    arr.add(children);
                }
            }
        }
        if (arr.size() > 0)
            res.add("items", arr);

        return res;
    }

    public List<CloudantEntity> buildDocumentList(CloudantEntity sub2) {
        List<CloudantEntity> res = new ArrayList<>();

        String entityId = sub2.getEntityId();
        String entityType = sub2.getEntityType();
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);
        res.add(sub2);

        List<Relationship> relationships = sub2.getRelationships();
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

           Database subCollection = cloudantdbInit.getEntityDB(type);
            if(subCollection != null) {
            	CloudantEntity sub = getSpecificDocument(subCollection, id);
                if (sub != null) {
                    List<CloudantEntity> children = buildDocumentList(sub);
                    res.addAll(children);
                }
            }
        }
        return res;
    }

    private CloudantEntity getLatestDocument(Database entityDb, String id) {
        if (StringUtils.isBlank(id))
            return null;
        CloudantEntity doc = CloudantdbFacade.findLatestOne(entityDb, id);
        if (doc == null)
            return null;
        return  doc;
    }

    private CloudantEntity getSpecificDocument(Database entityDb, String id) {
        if (StringUtils.isBlank(id))
            return null;
        CloudantEntity doc = CloudantdbFacade.findSpecificOne(entityDb, id);
        if (doc == null)
            return null;
        return  doc;
    }
}
