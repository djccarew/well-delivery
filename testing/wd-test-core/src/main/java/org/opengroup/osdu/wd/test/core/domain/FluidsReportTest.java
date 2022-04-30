package org.opengroup.osdu.wd.test.core.domain;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.wd.test.core.util.*;

import java.util.Arrays;
import java.util.Map;

public abstract class FluidsReportTest extends TestBase {

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

    private static  String FR_TYPE = "FluidsReport";
    protected static final String FR_KIND = String.format("slb:well-delivery:%s:3.0.0", FR_TYPE.toLowerCase());
    protected static final String FR_ID = FR_TYPE.toLowerCase() + "-" + NOW;
    protected static long FR_VERSION;

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===DrillingReportTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);
        String fr_schema = SchemaUtil.buildFluidsReport();
        SchemaUtil.create(fr_schema, token);

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

        String dr_body = EntityUtil.buildFluidsReport(FR_ID, FR_KIND, LEGAL_TAG, existenceKind, wellboreid);
        FR_VERSION  = EntityUtil.create(FR_TYPE, FR_ID, dr_body, token);
        Assert.assertTrue(FR_VERSION > 0);

    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
        EntityUtil.purgeEntity(FR_TYPE, FR_ID, token);
    }

    @Test
    public void should_returnFluidsReports_whenQueryByWellbore() throws Exception {
        String path = String.format("fluidsReports/v1/by_wellbore/%s", WELLBORE_ID);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        EntityUtil.EnitityMock[] frs = getFluidsReports(response);
        String frid = String.format("namespace:master-data--%s:%s", FR_TYPE, FR_ID);
        Assert.assertTrue(Arrays.stream(frs).anyMatch(i -> i.id.equals(frid) && i.version == FR_VERSION));
    }

    private static EntityUtil.EnitityMock[] getFluidsReports(ClientResponse response) {
        Gson gson = new Gson();
        EntityUtil.EnitityMock[] arr = gson.fromJson(response.getEntity(String.class), EntityUtil.EnitityMock[].class);
        return arr;
    }
}
