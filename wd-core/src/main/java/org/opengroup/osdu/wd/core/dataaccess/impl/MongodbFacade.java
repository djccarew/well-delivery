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

import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.opengroup.osdu.core.client.model.http.AppException;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongodbFacade {

    private static final Logger LOGGER = Logger.getLogger(MongodbFacade.class.getName());


    /////////////////////////////////////////
    // General Entity
    ////////////////////////////////////////

    public static void upsert(MongoCollection<Document> collection, String _id, Document doc) {

        try {
            collection.replaceOne(Filters.eq("_id", _id),
                                    doc,
                                    new ReplaceOptions().upsert(true));
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to put item into MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document findLatestOne(MongoCollection<Document> collection, String entityId) {
        try {
            FindIterable<Document> docs = collection.find(
                       Filters.and(
                            Filters.eq("entityId", entityId),
                            Filters.eq("deleted", false)))
                    .sort(Sorts.descending("_id"))
                    .limit(1);
            return docs != null && docs.iterator().hasNext() ? docs.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document findSpecificOne(MongoCollection<Document> collection, String _id) {
        try {
            FindIterable<Document> docs = collection.find(
                    Filters.and(
                            Filters.eq("_id", _id),
                            Filters.eq("deleted", false)));
            return docs != null && docs.iterator().hasNext() ? docs.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<Long> findVersionNumbers(MongoCollection<Document> collection, String entityId) {
        try {
            MongoIterable<Long> res = collection.find(
                        Filters.and(
                            Filters.eq("entityId", entityId),
                            Filters.eq("deleted", false)))
                    .map(x -> x.getLong("version"));
            List<Long> list = new ArrayList<>();
            for (Long version : res) {
                list.add(version);
            }
            if (list.size() > 0)
                Collections.sort(list);
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find versions from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static long deleteEntity(MongoCollection<Document> collection, String entityId) {
        try {
            UpdateResult res = collection.updateMany(Filters.eq("entityId", entityId), Updates.set("deleted", true));
            return res.getMatchedCount();
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to delete entity from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static long purgeEntity(MongoCollection<Document> collection, String entityId) {
        try {
            DeleteResult res = collection.deleteMany(Filters.eq("entityId", entityId));
            return res.getDeletedCount();
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to purge entity from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static int deleteOne(MongoCollection<Document> collection, String _id) {
        try {
            Document doc = collection.findOneAndUpdate(Filters.eq("_id", _id), Updates.set("deleted", true));
            return doc == null ? 0 : 1;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to delete item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static int purgeOne(MongoCollection<Document> collection, String _id) {
        try {
            Document doc = collection.findOneAndDelete(Filters.eq("_id", _id));
            return doc == null ? 0 : 1;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to purge item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }


    /////////////////////////////////////////
    // Query Operation
    ////////////////////////////////////////

    public static Document getLatesEntity_ByName(MongoCollection<Document> collection, String existenceKind, String name) {
        try {
            FindIterable<Document> docs = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("data.FacilityName", name),
                            eq("deleted", false)
                    ))
                    .sort(Sorts.descending("_id"))
                    .limit(1);
            return docs != null && docs.iterator().hasNext() ? docs.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document getSpecificEntity_ByName(MongoCollection<Document> collection, String existenceKind, String name, long verison) {
        try {
            FindIterable<Document> docs = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("data.FacilityName", name),
                            eq("version", verison),
                            eq("deleted", false)));
            return docs != null && docs.iterator().hasNext() ? docs.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<Long> getEntity_VersionNumberList_ByName(MongoCollection<Document> collection, String existenceKind, String name) {
        try {
            MongoIterable<Long> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("data.FacilityName", name),
                            eq("deleted", false)
                    ))
                    .map(x -> x.getLong("version"));
            List<Long> list = new ArrayList<>();
            for (Long version : res) {
                list.add(version);
            }
            if (list.size() > 0)
                Collections.sort(list);
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find versions from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }


    public static List<String> getIdList_Relationships_ByEntityId(MongoCollection<Document> collection, String existenceKind, String entityId, String relationshipType) {
        try {
            MongoIterable<List<Document>> res = collection.find(
                        and(
                            eq("entityId", entityId),
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false)
                        ))
                    .sort(Sorts.descending("_id"))
                    .limit(1)
                    .map(x -> x.getList("relationships", Document.class));
            MongoCursor<List<Document>> it = res.iterator();
            if(!it.hasNext())
                return new ArrayList<>();
            List<Document> docs = it.next();
            if(docs == null)
                return new ArrayList<>();
            List<String> list = new ArrayList<>();
            for (Document doc : docs) {
                String id = doc.getString("id");
                String type = doc.getString("entityType");
                if(id != null && type != null && type.equalsIgnoreCase(relationshipType)){
                    list.add(id);
                }
            }
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find versions from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static String getId_Relationship_ByEntityId(MongoCollection<Document> collection, String existenceKind, String entityId, String relationshipType) {
        try {
            MongoIterable<List<Document>> res = collection.find(
                    and(
                            eq("entityId", entityId),
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false)
                    ))
                    .sort(Sorts.descending("_id"))
                    .limit(1)
                    .map(x -> x.getList("relationships", Document.class));
            MongoCursor<List<Document>> it = res.iterator();
            if (!it.hasNext())
                return null;
            List<Document> docs = it.next();
            if (docs == null)
                return null;
            for (Document doc : docs) {
                String id = doc.getString("id");
                String type = doc.getString("entityType");
                if (id != null && type != null && type.equalsIgnoreCase(relationshipType)) {
                    return id;
                }
            }
            return null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find versions from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document getLatestEntity_ByRelatedEntityId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedEntityId) {
        try {
            FindIterable<Document> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            regex("id", "^" + relatedEntityId + ":")
                                    )
                            )
                    ))
                    .sort(Sorts.descending("version"))
                    .limit(1);
            return res != null && res.iterator().hasNext() ? res.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document getLatestEntity_ByRelatedId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedId) {
        try {
            FindIterable<Document> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            eq("id", relatedId)
                                    )
                            )
                    ))
                    .sort(Sorts.descending("version"))
                    .limit(1);
            return res != null && res.iterator().hasNext() ? res.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static Document getSpecificEntity_ByRelatedEntityId(MongoCollection<Document> collection, String existenceKind, long verison, String relatedType, String relatedEntityId) {
        try {
            FindIterable<Document> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("version", verison),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            regex("id", "^" + relatedEntityId + ":")
                                    )
                            )
                    ))
                    .limit(1);
            return res != null && res.iterator().hasNext() ? res.first() : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static String getLatestId_ByRelatedEntityId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedEntityId) {
        try {
            FindIterable<Document> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            regex("id", "^" + relatedEntityId + ":")
                                    )
                            )
                    ))
                    .sort(Sorts.descending("version"))
                    .limit(1);

            return res != null && res.iterator().hasNext() ? res.first().getString("_id") : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static String getLatestId_ByRelatedId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedId) {
        try {
            FindIterable<Document> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            eq("id", relatedId)
                                    )
                            )
                    ))
                    .sort(Sorts.descending("version"))
                    .limit(1);

            return res != null && res.iterator().hasNext() ? res.first().getString("_id") : null;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<String> getIdList_LatestPerEntity_ByRelatedEntityId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedEntityId) {
        try {
            MongoIterable<Document> res = collection.aggregate(
                    Arrays.asList(
                            match(and(
                                    eq("existenceKind", existenceKind.toLowerCase()),
                                    eq("deleted", false),
                                    elemMatch("relationships",
                                            and(
                                                    eq("entityType", relatedType.toLowerCase()),
                                                    regex("id", "^" + relatedEntityId + ":")
                                            )
                                    )
                            )),
                            group("$entityId", max("max", "$_id"))
                    )
            );
            List<String> list = new ArrayList<>();
            for (Document doc : res) {
                String current = doc.getString("max");
                if (!StringUtils.isBlank(current)) {
                    list.add(current);
                }
            }
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<String> getIdList_LatestPerEntity_ByRelatedId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedId) {
        try {
            MongoIterable<Document> res = collection.aggregate(
                    Arrays.asList(
                            match(and(
                                    eq("existenceKind", existenceKind.toLowerCase()),
                                    eq("deleted", false),
                                    elemMatch("relationships",
                                            and(
                                                    eq("entityType", relatedType.toLowerCase()),
                                                    eq("id", relatedId)
                                            )
                                    )
                            )),
                            group("$entityId", max("max", "$_id"))
                    )
            );
            List<String> list = new ArrayList<>();
            for (Document doc : res) {
                String current = doc.getString("max");
                if (!StringUtils.isBlank(current)) {
                    list.add(current);
                }
            }
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<String> getIdList_LatestPerEntity_ByTimeRange(MongoCollection<Document> collection, String existenceKind, String startTime, String endTime) {
        try {
            MongoIterable<Document> res = collection.aggregate(
                    Arrays.asList(
                            match(and(
                                    eq("existenceKind", existenceKind.toLowerCase()),
                                    eq("deleted", false),
                                    lte("startTime", endTime),
                                    gte("endTime", startTime)
                            )),
                            group("$entityId", Accumulators.max("max", "$_id"))
                    )
            );
            List<String> list = new ArrayList<>();
            for (Document doc : res) {
                String current = doc.getString("max");
                if (!StringUtils.isBlank(current)) {
                    list.add(current);
                }
            }
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<Long> getEntityVersionNumberList_ByRelatedEntityId(MongoCollection<Document> collection, String existenceKind, String relatedType, String relatedEntityId) {
        try {
            MongoIterable<Long> res = collection.find(
                    and(
                            eq("existenceKind", existenceKind.toLowerCase()),
                            eq("deleted", false),
                            elemMatch("relationships",
                                    and(
                                            eq("entityType", relatedType.toLowerCase()),
                                            regex("id", "^" + relatedEntityId + ":")
                                    )
                            )
                    ))
                    .map(x -> x.getLong("version"));
            List<Long> list = new ArrayList<>();
            for (Long version : res) {
                list.add(version);
            }
            if (list.size() > 0)
                Collections.sort(list);
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find version numbers from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    public static List<Document> getEntityList_ByIdList(MongoCollection<Document> collection, List idLlist) {
        try {
            MongoIterable<Document> res = collection.find(in("_id", idLlist));
            List<Document> list = new ArrayList<>();
            for (Document doc : res) {
                list.add(doc);
            }
            return list;
        } catch (MongoException e) {
            String errorMessage = "Unexpectedly failed to find item from MongoDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
        }
    }

    /////////////////////////////////////////
    // mongodb operation
    ////////////////////////////////////////

    public static MongoClient createClient(String mongodbUri) {

        if (StringUtils.isBlank(mongodbUri)) {
            String errorMessage = "Mongodb url is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        MongoClientURI uri = new MongoClientURI(mongodbUri);
        return new MongoClient(uri);
    }

    public static MongoCollection<Document> getCollection(MongoClient client, String dbName, String collectionName) {
        validateParameters(client, dbName, collectionName);
        MongoDatabase db = getDatabase(client, dbName);
        if (!checkIfCollectionExisted(db, collectionName)) {
            String errorMessage = String.format("Collection %s is not existed.", collectionName);
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_BAD_REQUEST, errorMessage, errorMessage);
        }
        return db.getCollection(collectionName);
    }

    public static MongoCollection<Document> getCollectionOrNull(MongoClient client, String dbName, String collectionName) {
        validateParameters(client, dbName, collectionName);
        MongoDatabase db = getDatabase(client, dbName);
        if (checkIfCollectionExisted(db, collectionName)) {
            return db.getCollection(collectionName);
        } else {
            return null;
        }
    }

    public static MongoCollection<Document> createOrGetCollection(MongoClient client, String dbName, String collectionName, boolean isCosmosdbAPI) {
        validateParameters(client, dbName, collectionName);
        MongoDatabase db = getDatabase(client, dbName);
        if (!checkIfCollectionExisted(db, collectionName)) {
            db.createCollection(collectionName);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            collection.createIndex(Indexes.descending("version"));
            return collection;
        }
        return db.getCollection(collectionName);
    }

    private static MongoDatabase getDatabase(MongoClient client, String dbName) {
        CodecRegistry defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(defaultCodecRegistry, fromProviders(pojoCodecProvider));
        MongoDatabase db = client.getDatabase(dbName).withCodecRegistry(pojoCodecRegistry);
        return db;
    }

    private static void validateParameters(MongoClient client, String dbName, String collectionName){
        if (client == null) {
            String errorMessage = "Mongo client is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(dbName)) {
            String errorMessage = "Mongo DB Name is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(collectionName)) {
            String errorMessage = "Mongo collection name is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
    }

    private static boolean checkIfCollectionExisted(MongoDatabase db, String collectionName) {
        MongoIterable<String> names = db.listCollectionNames();
        for (String name : names) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }
}

