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

package org.opengroup.osdu.wd.test.core.entity;

import org.junit.Assert;
import org.opengroup.osdu.wd.test.core.util.*;
import org.junit.Test;

public abstract class EntityTest extends TestBase{
    protected static final long NOW = System.currentTimeMillis();
    protected static final String LEGAL_TAG = LegalTagUtils.createRandomName();

    private static  String TYPE = "Well";
    private static  String NAME = "Well-Test";
    protected static final String KIND = String.format("slb:well-delivery:%s:3.0.0", TYPE.toLowerCase());

    protected static final String EXISTENCE_KIND = "planned";

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===EntityTest");
        token = theToken;
        TestUtils.disableSslVerification();
        String schema = SchemaUtil.buildWell();
        SchemaUtil.create(schema, token);

        LegalTagUtils.create(LEGAL_TAG,  token);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);
    }

    @Test
    public void should_returnEntity_create_whenValidEntity() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        //Create
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnEntity_whenQueryVaildEntity() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        //Create
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Get
        EntityUtil.getEntity(TYPE, ID, token);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnVersionNumbers_whenQueryVersionNumbers() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Get
        EntityUtil.getVersionNumbers(TYPE, ID, token);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnSuccess_whenDeleteEntity() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Delete
        EntityUtil.deleteEntity(TYPE, ID, token);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnSuccess_whenPurgeEntity() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnEntity_whenQueryEntityVersion() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Get
        EntityUtil.getEntityVersion(TYPE, ID, version, token);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnSuccess_whenDeleteEntityVersion() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Delete
        EntityUtil.deleteEntityVersion(TYPE, ID, version, token);
        //Purge
        EntityUtil.purgeEntity(TYPE, ID, token);
    }

    @Test
    public void should_returnSuccess_whenPurgeEntityVersion() throws Exception {
        String ID = TYPE.toLowerCase() + "-" + NOW;
        String body = EntityUtil.buildWell(ID, KIND, LEGAL_TAG, EXISTENCE_KIND, NAME);
        long version  = EntityUtil.create(TYPE, ID, body, token);
        Assert.assertTrue(version > 0);
        //Purge
        EntityUtil.purgeEntityVersion(TYPE, ID, version, token);
    }
}
