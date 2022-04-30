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

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.client.model.http.AppException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;

@RunWith(MockitoJUnitRunner.class)
public class MongodbFacadeTest {


    @Mock
    MongoCollection<Document> mockCollection;

    @Mock
    FindIterable<Document> mockFindIterable;

    @Mock
    MongoCursor<Document> mockCursor;

    @Mock
    Document mockDocument;

    @Mock
    MongoIterable<Long> mockLongIterable;

    @Mock
    List<Long> mockLongList;

    @Mock
    UpdateResult mockUpdateResult;

    @Mock
    DeleteResult mockDeleteResult;

    @Before
    public void setup() {
    }

    /////////////////////////////////////////
    // General Entity
    ////////////////////////////////////////

    @Test
    public void should_returnSuccess_when_upsert_ObjectIsValid() {

        String id = "id_test";
        Document doc = Document.parse("{\"id\": \"id_test\"}");
        when(mockCollection.replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class))).thenReturn(null);
        MongodbFacade.upsert(mockCollection, id, doc);
    }

    @Test
    public void should_returnException_when_upsert_ObjectIsInvalid() {

        String id = "id_vaild";
        Document doc = Document.parse("{\"id\": \"id_test\"}");
        when(mockCollection.replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.upsert(mockCollection, id, doc);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_findLatestOne_ObjectIsValid() {

        String entityId = "entityId_test";
        when(mockCollection.find((any(Bson.class)))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("_id"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document doc = MongodbFacade.findLatestOne(mockCollection, entityId);
        assertEquals(doc, mockDocument);
    }

    @Test
    public void should_returnException_when_findLatestOne_ObjectIsInvalid() {

        String entityId = "entityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.findLatestOne(mockCollection, entityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_findSpecificOne_ObjectIsValid() {

        String id = "id_test";
        when(mockCollection.find((any(Bson.class)))).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document doc = MongodbFacade.findSpecificOne(mockCollection, id);
        assertEquals(doc, mockDocument);
    }

    @Test
    public void should_returnException_when_findSpecificOne_ObjectIsInvalid() {

        String id = "id_vaild";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.findSpecificOne(mockCollection, id);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_deleteEntity_ObjectIsValid() {

        String entityId = "entityId_test";
        Bson filter = Filters.eq("entityId", entityId);
        Bson update = Updates.set("deleted", true);
        long matchedCount = 5;
        when(mockCollection.updateMany(filter, update)).thenReturn(mockUpdateResult);
        when(mockUpdateResult.getMatchedCount()).thenReturn(matchedCount);

        long result = MongodbFacade.deleteEntity(mockCollection, entityId);
        assertEquals(matchedCount, result);
    }


    @Test
    public void should_returnException_when_deleteEntity_ObjectIsInvalid() {

        String entityId = "entityId_invalid";
        when(mockCollection.updateMany(any(Bson.class), any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.deleteEntity(mockCollection, entityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_purgeEntity_ObjectIsValid() {

        String entityId = "entityId_test";
        Bson filter = Filters.eq("entityId", entityId);
        long deletedCount = 5;
        when(mockCollection.deleteMany(filter)).thenReturn(mockDeleteResult);
        when(mockDeleteResult.getDeletedCount()).thenReturn(deletedCount);

        long result = MongodbFacade.purgeEntity(mockCollection, entityId);
        assertEquals(deletedCount, result);
    }


    @Test
    public void should_returnException_when_purgeEntity_ObjectIsInvalid() {

        String entityId = "entityId_invalid";
        when(mockCollection.deleteMany(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.purgeEntity(mockCollection, entityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_deleteOne_ObjectIsValid() {

        String id = "id_test";
        when(mockCollection.findOneAndUpdate(any(Bson.class), any(Bson.class))).thenReturn(mockDocument);

        long result = MongodbFacade.deleteOne(mockCollection, id);
        assertEquals(1, result);
    }


    @Test
    public void should_returnException_when_deleteOne_ObjectIsInvalid() {

        String id = "id_test";
        when(mockCollection.findOneAndUpdate(any(Bson.class), any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.deleteOne(mockCollection, id);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnSuccess_when_purgeOne_ObjectIsValid() {

        String id = "id_test";
        when(mockCollection.findOneAndDelete(any(Bson.class))).thenReturn(mockDocument);

        long result = MongodbFacade.purgeOne(mockCollection, id);
        assertEquals(1, result);
    }


    @Test
    public void should_returnException_when_purgeOne_ObjectIsInvalid() {

        String id = "id_test";
        when(mockCollection.findOneAndDelete(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.purgeOne(mockCollection, id);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    /////////////////////////////////////////
    // Query Operation
    ////////////////////////////////////////

    @Test
    public void should_returnDocument_when_InputValid_for_getLatesEntity_ByName() {

        String existenceKind = "existenceKind_test";
        String name = "name_test";

        when(mockCollection.find((any(Bson.class)))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("_id"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document result = MongodbFacade.getLatesEntity_ByName(mockCollection, existenceKind, name);
        assertEquals(result, mockDocument);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getLatesEntity_ByName() {

        String entityId = "entityId_invalid";
        String name = "name_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getLatesEntity_ByName(mockCollection, entityId, name);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnDocument_when_InputValid_for_getSpecificEntity_ByName() {

        String existenceKind = "existenceKind_test";
        String name = "name_test";
        long verison = 100;

        when(mockCollection.find((any(Bson.class)))).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document result = MongodbFacade.getSpecificEntity_ByName(mockCollection, existenceKind, name, verison);
        assertEquals(result, mockDocument);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getSpecificEntity_ByName() {

        String entityId = "entityId_invalid";
        String name = "name_invalid";
        long verison = 0;
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getSpecificEntity_ByName(mockCollection, entityId, name, verison);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnDocument_when_InputValid_for_getLatestEntity_ByRelatedEntityId() {

        String existenceKind = "existenceKind_test";
        String relatedType = "relatedType_test";
        String relatedEntityId = "relatedEntityId_test";

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("version"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document result = MongodbFacade.getLatestEntity_ByRelatedEntityId(mockCollection, existenceKind, relatedType, relatedEntityId);
        assertEquals(result, mockDocument);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getLatestEntity_ByRelatedEntityId() {

        String entityId = "entityId_invalid";
        String relatedType = "relatedType_invalid";
        String relatedEntityId = "relatedEntityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getLatestEntity_ByRelatedEntityId(mockCollection, entityId, relatedType, relatedEntityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnDocument_when_InputValid_for_getLatestEntity_ByRelatedId() {

        String existenceKind = "existenceKind_test";
        String relatedType = "relatedType_test";
        String relatedEntityId = "relatedEntityId_test";

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("version"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document result = MongodbFacade.getLatestEntity_ByRelatedId(mockCollection, existenceKind, relatedType, relatedEntityId);
        assertEquals(result, mockDocument);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getLatestEntity_ByRelatedId() {

        String entityId = "entityId_invalid";
        String relatedType = "relatedType_invalid";
        String relatedEntityId = "relatedEntityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getLatestEntity_ByRelatedId(mockCollection, entityId, relatedType, relatedEntityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    /*
    @Test
    public void should_returnDocument_when_InputValid_for_getSpecificEntity_ByRelatedEntityId() {

        String existenceKind = "existenceKind_test";
        long verison = 100;
        String relatedType = "relatedType_test";
        String relatedEntityId = "relatedEntityId_test";

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("version"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        Document result = MongodbFacade.getSpecificEntity_ByRelatedEntityId(mockCollection, existenceKind, verison, relatedType, relatedEntityId);
        assertEquals(result, mockDocument);
    }*/

    @Test
    public void should_returnException_when_InputInvalid_for_getSpecificEntity_ByRelatedEntityId() {

        String entityId = "entityId_invalid";
        long verison = 0;
        String relatedType = "relatedType_invalid";
        String relatedEntityId = "relatedEntityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getSpecificEntity_ByRelatedEntityId(mockCollection, entityId, verison, relatedType, relatedEntityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnDocument_when_InputValid_for_getLatestId_ByRelatedEntityId() {

        String existenceKind = "existenceKind_test";
        String relatedType = "relatedType_test";
        String relatedEntityId = "relatedEntityId_test";
        String idResult = "id_Result";

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("version"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);
        when(mockDocument.getString(any(String.class))).thenReturn(idResult);

        String result = MongodbFacade.getLatestId_ByRelatedEntityId(mockCollection, existenceKind, relatedType, relatedEntityId);
        assertEquals(result, idResult);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getLatestId_ByRelatedEntityId() {

        String entityId = "entityId_invalid";
        String relatedType = "relatedType_invalid";
        String relatedEntityId = "relatedEntityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getLatestId_ByRelatedEntityId(mockCollection, entityId, relatedType, relatedEntityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }

    @Test
    public void should_returnDocument_when_InputValid_for_getLatestId_ByRelatedId() {

        String existenceKind = "existenceKind_test";
        String relatedType = "relatedType_test";
        String relatedEntityId = "relatedEntityId_test";
        String idResult = "id_Result";

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(Sorts.descending("version"))).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(1)).thenReturn(mockFindIterable);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true);
        when(mockFindIterable.first()).thenReturn(mockDocument);
        when(mockDocument.getString(any(String.class))).thenReturn(idResult);

        String result = MongodbFacade.getLatestId_ByRelatedId(mockCollection, existenceKind, relatedType, relatedEntityId);
        assertEquals(result, idResult);
    }

    @Test
    public void should_returnException_when_InputInvalid_for_getLatestId_ByRelatedId() {

        String entityId = "entityId_invalid";
        String relatedType = "relatedType_invalid";
        String relatedEntityId = "relatedEntityId_invalid";
        when(mockCollection.find(any(Bson.class))).thenThrow(new MongoException("error"));
        try {
            MongodbFacade.getLatestId_ByRelatedId(mockCollection, entityId, relatedType, relatedEntityId);
            Assert.fail("Expected an AppException to be thrown");
        } catch (AppException ex) {
            assertEquals(500, ex.getError().getCode());
            assertEquals("error", ex.getError().getMessage());
        }
    }
}
