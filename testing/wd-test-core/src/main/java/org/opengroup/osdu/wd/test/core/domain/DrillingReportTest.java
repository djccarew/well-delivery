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

public abstract class DrillingReportTest extends TestBase {

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

    private static  String DR_TYPE = "DrillingReport";
    protected static final String DR_KIND = String.format("slb:well-delivery:%s:3.0.0", DR_TYPE.toLowerCase());
    protected static final String DR_ID = DR_TYPE.toLowerCase() + "-" + NOW;
    private static  String DR_START = "2010-05-09T10:10:30";
    private static  String DR_END = "2010-05-10T14:09:10";
    protected static long DR_VERSION;

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===DrillingReportTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);
        String dr_schema = SchemaUtil.buildDrillingReport();
        SchemaUtil.create(dr_schema, token);

        LegalTagUtils.create(LEGAL_TAG,  token);

        String existenceKind = "actual";

        String well_body = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind, WELL_NAME);
        WELL_VERSION  = EntityUtil.create(WELL_TYPE, WELL_ID, well_body, token);
        Assert.assertTrue(WELL_VERSION > 0);
        String wellid = String.format("namespace:master-data--%s:%s:%d", WELL_TYPE, WELL_ID, WELL_VERSION);

        String wellbore_body = EntityUtil.buildWellbore(WELLBORE_ID, WELLBORE_KIND, LEGAL_TAG, existenceKind, wellid);
        WELLBORE_VERSION  = EntityUtil.create(WELLBORE_TYPE, WELLBORE_ID, wellbore_body, token);
        Assert.assertTrue(WELLBORE_VERSION > 0);
        String wellboreid = String.format("namespace:master-data--%s:%s:%d", WELLBORE_TYPE, WELLBORE_ID, WELLBORE_VERSION);

        String dr_body = EntityUtil.buildDrillingReport(DR_ID, DR_KIND, LEGAL_TAG, existenceKind, wellboreid, DR_START, DR_END);
        DR_VERSION  = EntityUtil.create(DR_TYPE, DR_ID, dr_body, token);
        Assert.assertTrue(DR_VERSION > 0);

    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
        EntityUtil.purgeEntity(DR_TYPE, DR_ID, token);
    }

    @Test
    public void should_returnDrillingReports_whenQueryByWellbore() throws Exception {
        String path = String.format("drillingReports/v1/by_wellbore/%s", WELLBORE_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.EnitityMock[] drs = getDrillingReports(response);
        String bharunid = String.format("namespace:master-data--%s:%s", DR_TYPE, DR_ID);
        Assert.assertTrue(Arrays.stream(drs).anyMatch(i -> i.id.equals(bharunid) && i.version == DR_VERSION));
    }

    @Test
    public void should_returnLatestDrillingReport_whenQueryByWellbore() throws Exception {
        String path = String.format("drillingReports/v1/latest/by_wellbore/%s", WELLBORE_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, DR_TYPE, DR_ID);
        Assert.assertEquals(DR_VERSION, version);
    }

    @Test
    public void should_returnDrillingReports_whenQueryByTimeRange() throws Exception {
        String path = String.format("drillingReports/v1/by_timeRange/%s/%s", DR_START, DR_END);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.EnitityMock[] drs = getDrillingReports(response);
        String drid = String.format("namespace:master-data--%s:%s", DR_TYPE, DR_ID);
        Assert.assertTrue(Arrays.stream(drs).anyMatch(i -> i.id.equals(drid) && i.version == DR_VERSION));
    }

    @Test
    public void should_returnDRReferenceTree_whenQueryByDrillingReport() throws Exception {
        String path = String.format("drillingReports/v1/reference_tree/by_drillingReport/%s", DR_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.ReferenceTreeMock tree = getReferenceTree(response);
        Assert.assertEquals(DR_ID, tree.id);
        Assert.assertEquals(DR_TYPE.toLowerCase(), tree.type);
    }

    private static EntityUtil.EnitityMock[] getDrillingReports(ClientResponse response) {
        Gson gson = new Gson();
        EntityUtil.EnitityMock[] arr = gson.fromJson(response.getEntity(String.class), EntityUtil.EnitityMock[].class);
        return arr;
    }

    private static EntityUtil.ReferenceTreeMock getReferenceTree(ClientResponse response) {
        Gson gson = new Gson();
        EntityUtil.ReferenceTreeMock tree = gson.fromJson(response.getEntity(String.class), EntityUtil.ReferenceTreeMock.class);
        return tree;
    }
}
