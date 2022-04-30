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

package org.opengroup.osdu.wd.test.core.domain;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.wd.test.core.util.*;

import java.util.Arrays;
import java.util.Map;

public abstract class WellActivityProgramTest extends TestBase {
    protected static final long NOW = System.currentTimeMillis();
    protected static final String LEGAL_TAG = LegalTagUtils.createRandomName();

    private static  String WELL_TYPE = "Well";
    private static  String WELL_NAME = "Well-Name";
    protected static final String WELL_KIND = String.format("slb:well-delivery:%s:3.0.0", WELL_TYPE.toLowerCase());
    protected static final String WELL_ID = WELL_TYPE.toLowerCase() + "-" + NOW;
    protected static long WELL_VERSION;

    private static  String WELLBORE_TYPE = "Wellbore";
    protected static final String WELLBORE_KIND = String.format("slb:well-delivery:%s:3.0.0", WELLBORE_TYPE.toLowerCase());
    protected static final String WELLBORE_ID = WELLBORE_TYPE.toLowerCase() + "-" + NOW;
    protected static long WELLBORE_VERSION;

    private static  String PLAN_TYPE = "ActivityPlan";
    protected static final String PLAN_KIND = String.format("slb:well-delivery:%s:3.0.0", PLAN_TYPE.toLowerCase());
    protected static final String PLAN_ID = PLAN_TYPE.toLowerCase() + "-" + NOW;
    protected static long PLAN_VERSION;

    private static  String WAP_TYPE = "WellActivityProgram";
    protected static final String WAP_KIND = String.format("slb:well-delivery:%s:3.0.0", WAP_TYPE.toLowerCase());
    protected static final String WAP_ID = WAP_TYPE.toLowerCase() + "-" + NOW;
    protected static long WAP_VERSION;

    protected static String token;


    public static void classSetup(String theToken) throws Exception {
        System.out.println("===WellActivityProgramTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);
        String plan_schema = SchemaUtil.buildActivityPlan();
        SchemaUtil.create(plan_schema, token);
        String wap_schema = SchemaUtil.buildWellActivityProgram();
        SchemaUtil.create(wap_schema, token);

        LegalTagUtils.create(LEGAL_TAG,  token);

        String existenceKind = "planned";

        String well_body = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind, WELL_NAME);
        WELL_VERSION  = EntityUtil.create(WELL_TYPE, WELL_ID, well_body, token);
        Assert.assertTrue(WELL_VERSION > 0);
        String wellid = String.format("namespace:master-data--%s:%s:%d", WELL_TYPE, WELL_ID, WELL_VERSION);

        String wellbore_body = EntityUtil.buildWellbore(WELLBORE_ID, WELLBORE_KIND, LEGAL_TAG, existenceKind, wellid);
        WELLBORE_VERSION  = EntityUtil.create(WELLBORE_TYPE, WELLBORE_ID, wellbore_body, token);
        Assert.assertTrue(WELLBORE_VERSION > 0);
        String wellboreid = String.format("namespace:master-data--%s:%s:%d", WELLBORE_TYPE, WELLBORE_ID, WELLBORE_VERSION);

        String plan_body = EntityUtil.buildActivityPlan(PLAN_ID, PLAN_KIND, LEGAL_TAG, existenceKind, wellboreid);
        PLAN_VERSION  = EntityUtil.create(PLAN_TYPE, PLAN_ID, plan_body, token);
        Assert.assertTrue(PLAN_VERSION > 0);
        String planid = String.format("namespace:master-data--%s:%s:%d",PLAN_TYPE, PLAN_ID, PLAN_VERSION);

        String wap_body = EntityUtil.buildWellActivityProgram(WAP_ID, WAP_KIND, LEGAL_TAG, existenceKind, wellboreid, planid);
        WAP_VERSION  = EntityUtil.create(WAP_TYPE, WAP_ID, wap_body, token);
        Assert.assertTrue(WAP_VERSION > 0);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
        EntityUtil.purgeEntity(PLAN_TYPE, PLAN_ID, token);
        EntityUtil.purgeEntity(WAP_TYPE, WAP_ID, token);
    }

    @Test
    public void should_returnWAPEntity_whenQueryByWell() throws Exception {
        String path = String.format("wellActivityPrograms/v1/by_well/%s", WELL_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WAP_TYPE, WAP_ID);
        Assert.assertEquals(WAP_VERSION, version);
    }

    @Test
    public void should_returnWAPEntity_whenQueryByWellVersion() throws Exception {
        String path = String.format("wellActivityPrograms/v1/by_well/%s/%s", WELL_ID, WAP_VERSION);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WAP_TYPE, WAP_ID);
        Assert.assertEquals(WAP_VERSION, version);
    }

    @Test
    public void should_returnWAPReferenceTree_whenQueryByWell() throws Exception {
        String path = String.format("wellActivityPrograms/v1/reference_tree/by_well/%s", WELL_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.ReferenceTreeMock tree = getReferenceTree(response);
        Assert.assertEquals(WAP_ID, tree.id);
        Assert.assertEquals(WAP_TYPE.toLowerCase(), tree.type);
    }

    @Test
    public void should_returnWAPReferenceTree_whenQueryByWellVersion() throws Exception {
        String path = String.format("wellActivityPrograms/v1/reference_tree/by_well/%s/%s", WELL_ID, WAP_VERSION);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.ReferenceTreeMock tree = getReferenceTree(response);
        Assert.assertEquals(WAP_ID, tree.id);
        Assert.assertEquals(WAP_VERSION, tree.version);
        Assert.assertEquals(WAP_TYPE.toLowerCase(), tree.type);
    }

    @Test
    public void should_returnWAPEntityList_whenQueryContentByWell() throws Exception {
        String path = String.format("wellActivityPrograms/v1/full_content/by_well/%s", WELL_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    public void should_returnWAPVersionNumbers_whenQueryVersionNumbers() throws Exception {
        String path = String.format("wellActivityPrograms/v1/versions/by_well/%s", WELL_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long[] array = getVersionNumbers(response);
        Assert.assertTrue(array != null);
        Assert.assertTrue(array.length > 0);
        Assert.assertTrue(Arrays.stream(array).anyMatch(i -> i == WAP_VERSION));
    }

    private static EntityUtil.ReferenceTreeMock getReferenceTree(ClientResponse response) {
        Gson gson = new Gson();
        EntityUtil.ReferenceTreeMock tree = gson.fromJson(response.getEntity(String.class), EntityUtil.ReferenceTreeMock.class);
        return tree;
    }

    private static long[] getVersionNumbers(ClientResponse response) {
        Gson gson = new Gson();
        long[] arr = gson.fromJson(response.getEntity(String.class), long[].class);
        return arr;
    }
}
