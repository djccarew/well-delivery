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

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.wd.test.core.util.*;

import java.util.Map;

public abstract class ActivityPlanTest  extends TestBase {

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

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===ActivityPlanTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);
        String plan_schema = SchemaUtil.buildActivityPlan();
        SchemaUtil.create(plan_schema, token);

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
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
        EntityUtil.purgeEntity(PLAN_TYPE, PLAN_ID, token);
    }

    @Test
    public void should_returnActivityPlan_whenQueryByWell() throws Exception {
        String path = String.format("activityPlans/v1/by_well/%s", WELL_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, PLAN_TYPE, PLAN_ID);
        Assert.assertEquals(PLAN_VERSION, version);
    }
}
