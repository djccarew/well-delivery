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

public abstract class BHARunTest extends TestBase  {
    protected static final long NOW = System.currentTimeMillis();
    protected static final String LEGAL_TAG = LegalTagUtils.createRandomName();

    private static  String WELL_TYPE = "Well";
    private static  String WELL_NAME = "Well-Name";
    protected static final String WELL_KIND = String.format("slb:well-delivery:%s:3.0.0", WELL_TYPE.toLowerCase());
    protected static final String WELL_ID = WELL_TYPE.toLowerCase() + "-" + NOW;
    protected static long WELL_VERSION_planned;
    protected static long WELL_VERSION_actual;

    private static  String WELLBORE_TYPE = "Wellbore";
    protected static final String WELLBORE_KIND = String.format("slb:well-delivery:%s:3.0.0", WELLBORE_TYPE.toLowerCase());
    protected static final String WELLBORE_ID = WELLBORE_TYPE.toLowerCase() + "-" + NOW;
    protected static long WELLBORE_VERSION_planned;
    protected static long WELLBORE_VERSION_actual;

    private static  String SECTION_TYPE = "HoleSection";
    protected static final String SECTION_KIND = String.format("slb:well-delivery:%s:3.0.0", SECTION_TYPE.toLowerCase());
    protected static final String SECTION_ID = SECTION_TYPE.toLowerCase() + "-" + NOW;
    protected static long SECTION_VERSION_planned;
    protected static long SECTION_VERSION_actual;

    private static  String BHARUN_TYPE = "BHARun";
    protected static final String BHARUN_KIND = String.format("slb:well-delivery:%s:3.0.0", BHARUN_TYPE.toLowerCase());
    protected static final String BHARUN_ID = BHARUN_TYPE.toLowerCase() + "-" + NOW;
    protected static long BHARUN_VERSION_planned;
    protected static long BHARUN_VERSION_actual;

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===BHARunTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);
        String segement_schema = SchemaUtil.buildSection();
        SchemaUtil.create(segement_schema, token);
        String bharun_schema = SchemaUtil.buildBHARun();
        SchemaUtil.create(bharun_schema, token);

        LegalTagUtils.create(LEGAL_TAG, token);

        //planned
        String existenceKind_planned = "planned";

        String well_body_planned = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind_planned, WELL_NAME);
        WELL_VERSION_planned = EntityUtil.create(WELL_TYPE, WELL_ID, well_body_planned, token);
        Assert.assertTrue(WELL_VERSION_planned > 0);
        String wellid_planned = String.format("namespace:master-data--%s:%s:%d", WELL_TYPE, WELL_ID, WELL_VERSION_planned);

        String wellbore_body_planned = EntityUtil.buildWellbore(WELLBORE_ID, WELLBORE_KIND, LEGAL_TAG, existenceKind_planned, wellid_planned);
        WELLBORE_VERSION_planned = EntityUtil.create(WELLBORE_TYPE, WELLBORE_ID, wellbore_body_planned, token);
        Assert.assertTrue(WELLBORE_VERSION_planned > 0);
        String wellboreid_planned = String.format("namespace:master-data--%s:%s:%d", WELLBORE_TYPE, WELLBORE_ID, WELLBORE_VERSION_planned);

        String section_body_planned = EntityUtil.buildSection(SECTION_ID, SECTION_KIND, LEGAL_TAG, existenceKind_planned, wellboreid_planned);
        SECTION_VERSION_planned = EntityUtil.create(SECTION_TYPE, SECTION_ID, section_body_planned, token);
        Assert.assertTrue(SECTION_VERSION_planned > 0);
        String sectionid_planned = String.format("namespace:master-data--%s:%s:%d", SECTION_TYPE, SECTION_ID, SECTION_VERSION_planned);

        String bharun_body_planned = EntityUtil.buildBHARun(BHARUN_ID, BHARUN_KIND, LEGAL_TAG, existenceKind_planned, wellboreid_planned, sectionid_planned);
        BHARUN_VERSION_planned = EntityUtil.create(BHARUN_TYPE, BHARUN_ID, bharun_body_planned, token);
        Assert.assertTrue(BHARUN_VERSION_planned > 0);

        //actual
        String existenceKind_actual = "actual";

        String well_body_actual = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind_actual, WELL_NAME);
        WELL_VERSION_actual = EntityUtil.create(WELL_TYPE, WELL_ID, well_body_actual, token);
        Assert.assertTrue(WELL_VERSION_actual > 0);
        String wellid_actual = String.format("namespace:master-data--%s:%s:%d", WELL_TYPE, WELL_ID, WELL_VERSION_actual);

        String wellbore_body_actual = EntityUtil.buildWellbore(WELLBORE_ID, WELLBORE_KIND, LEGAL_TAG, existenceKind_actual, wellid_actual);
        WELLBORE_VERSION_actual = EntityUtil.create(WELLBORE_TYPE, WELLBORE_ID, wellbore_body_actual, token);
        Assert.assertTrue(WELLBORE_VERSION_actual > 0);
        String wellboreid_actual = String.format("namespace:master-data--%s:%s:%d", WELLBORE_TYPE, WELLBORE_ID, WELLBORE_VERSION_actual);

        String segment_body_actual = EntityUtil.buildSection(SECTION_ID, SECTION_KIND, LEGAL_TAG, existenceKind_actual, wellboreid_actual);
        SECTION_VERSION_actual = EntityUtil.create(SECTION_TYPE, SECTION_ID, segment_body_actual, token);
        Assert.assertTrue(SECTION_VERSION_actual > 0);
        String segmentid_actual = String.format("namespace:master-data--%s:%s:%d", SECTION_TYPE, SECTION_ID, SECTION_VERSION_actual);

        String bharun_body_actual = EntityUtil.buildBHARun(BHARUN_ID, BHARUN_KIND, LEGAL_TAG, existenceKind_actual, wellboreid_actual, segmentid_actual);
        BHARUN_VERSION_actual = EntityUtil.create(BHARUN_TYPE, BHARUN_ID, bharun_body_actual, token);
        Assert.assertTrue(BHARUN_VERSION_actual > 0);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
        EntityUtil.purgeEntity(SECTION_TYPE, SECTION_ID, token);
        EntityUtil.purgeEntity(BHARUN_TYPE, BHARUN_ID, token);
    }

    @Test
    public void should_returnBHARuns_whenQueryBySection() throws Exception {
        String path = String.format("bhaRuns/v1/by_holeSection/%s", SECTION_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.EnitityMock[] bharuns = getBHARuns(response);
        String bharunid = String.format("namespace:master-data--%s:%s", BHARUN_TYPE, BHARUN_ID);
        Assert.assertTrue(Arrays.stream(bharuns).anyMatch(i -> i.id.equals(bharunid) && i.version == BHARUN_VERSION_planned));
    }

    @Test
    public void should_returnBHARuns_whenQueryByWellbore_actual() throws Exception {
        String path = String.format("bhaRuns/v1/by_wellbore/%s:actual", WELLBORE_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.EnitityMock[] bharuns = getBHARuns(response);
        String bharunid = String.format("namespace:master-data--%s:%s", BHARUN_TYPE, BHARUN_ID);
        Assert.assertTrue(Arrays.stream(bharuns).anyMatch(i -> i.id.equals(bharunid) && i.version == BHARUN_VERSION_actual));
    }

    private static EntityUtil.EnitityMock[] getBHARuns(ClientResponse response) {
        Gson gson = new Gson();
        EntityUtil.EnitityMock[] arr = gson.fromJson(response.getEntity(String.class), EntityUtil.EnitityMock[].class);
        return arr;
    }
}
