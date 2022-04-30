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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequestScope
public class MongoTreeTraversal  {

    @Autowired
    MongodbInit mongodbInit;

    private Set<String> idSet;

    public MongoTreeTraversal()
    {
        this.idSet = new HashSet<>();
    }

    public JsonObject buildDocumentRef(Document doc) {
        JsonObject res = new JsonObject();

        String entityId = doc.getString("entityId");
        String entityType = doc.getString("entityType");
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);

        res.addProperty("id", entityId);
        res.addProperty("version", doc.getLong("version"));
        res.addProperty("type", entityType);

        List<Document> relationships = doc.getList("relationships", Document.class);
        if (relationships == null || relationships.size() == 0)
            return res;

        JsonArray arr = new JsonArray();
        for (Document rs : relationships) {
            String id = rs.getString("id");
            String type = rs.getString("entityType");
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type) || !id.matches("^[^:]+:[0-9]+$"))
                continue;
            if (this.idSet.contains(type + id.split(":")[0]))
                continue;
            else
                this.idSet.add(type + id.split(":")[0]);

            MongoCollection<Document> subCollection = mongodbInit.getEntityCollectionOrNull(type);
            if(subCollection != null) {
                Document sub = getSpecificDocument(subCollection, id);
                if (sub != null && !sub.isEmpty()) {
                    JsonObject children = buildDocumentRef(sub);
                    arr.add(children);
                }
            }
        }
        if (arr.size() > 0)
            res.add("items", arr);

        return res;
    }

    public List<Document> buildDocumentList(Document doc) {
        List<Document> res = new ArrayList<>();

        String entityId = doc.getString("entityId");
        String entityType = doc.getString("entityType");
        if (!this.idSet.contains(entityType + entityId))
            this.idSet.add(entityType + entityId);
        res.add(doc);

        List<Document> relationships = doc.getList("relationships", Document.class);
        if (relationships == null || relationships.size() == 0)
            return res;

        for (Document rs : relationships) {
            String id = rs.getString("id");
            String type = rs.getString("entityType");
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type))
                continue;
            if (this.idSet.contains(type + id.split(":")[0]))
                continue;
            else
                this.idSet.add(type + id.split(":")[0]);

            MongoCollection<Document> subCollection = mongodbInit.getEntityCollectionOrNull(type);
            if(subCollection != null) {
                Document sub = getSpecificDocument(subCollection, id);
                if (sub != null && !sub.isEmpty()) {
                    List<Document> children = buildDocumentList(sub);
                    res.addAll(children);
                }
            }
        }
        return res;
    }

    private Document getLatestDocument(MongoCollection<Document> collection, String id) {
        if (StringUtils.isBlank(id))
            return null;
        Document doc = MongodbFacade.findLatestOne(collection, id);
        if (doc == null || doc.isEmpty())
            return null;
        return  doc;
    }

    private Document getSpecificDocument(MongoCollection<Document> collection, String id) {
        if (StringUtils.isBlank(id))
            return null;
        Document doc = MongodbFacade.findSpecificOne(collection, id);
        if (doc == null || doc.isEmpty())
            return null;
        return  doc;
    }
}
