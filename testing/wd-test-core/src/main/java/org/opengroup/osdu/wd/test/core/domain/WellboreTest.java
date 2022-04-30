package org.opengroup.osdu.wd.test.core.domain;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.wd.test.core.util.*;

import java.util.Arrays;
import java.util.Map;

public abstract class WellboreTest  extends TestBase {

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

    protected static String existenceKind = "actual";
    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===WellboreTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);
        String wellbore_schema = SchemaUtil.buildWellbore();
        SchemaUtil.create(wellbore_schema, token);

        LegalTagUtils.create(LEGAL_TAG,  token);

        String well_body = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind, WELL_NAME);
        WELL_VERSION  = EntityUtil.create(WELL_TYPE, WELL_ID, well_body, token);
        Assert.assertTrue(WELL_VERSION > 0);
        String wellid = String.format("namespace:master-data--%s:%s:%d", WELL_TYPE, WELL_ID, WELL_VERSION);

        String wellbore_body = EntityUtil.buildWellbore(WELLBORE_ID, WELLBORE_KIND, LEGAL_TAG, existenceKind, wellid);
        WELLBORE_VERSION  = EntityUtil.create(WELLBORE_TYPE, WELLBORE_ID, wellbore_body, token);
        Assert.assertTrue(WELLBORE_VERSION > 0);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
        EntityUtil.purgeEntity(WELLBORE_TYPE, WELLBORE_ID, token);
    }

    @Test
    public void shouldWellboreVersionNumbers_return_whenQueryVersionNumbers() throws Exception {
        String path = String.format("wellbores/v1/versions/by_well/%s:%s", WELL_ID, existenceKind);
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
        Assert.assertTrue(Arrays.stream(array).anyMatch(i -> i == WELLBORE_VERSION));
    }

    @Test
    public void should_returnLatestWellbore_whenQueryByWell() throws Exception {
        String path = String.format("wellbores/v1/by_well/%s:%s", WELL_ID, existenceKind);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WELLBORE_TYPE, WELLBORE_ID);
        Assert.assertEquals(WELLBORE_VERSION, version);
    }

    @Test
    public void should_returnWellbore_whenQueryByWellVersion() throws Exception {
        String path = String.format("wellbores/v1/by_well/%s/%s:%s", WELL_ID, WELLBORE_VERSION, existenceKind);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WELLBORE_TYPE, WELLBORE_ID);
        Assert.assertEquals(WELLBORE_VERSION, version);
    }

    private static long[] getVersionNumbers(ClientResponse response) {
        Gson gson = new Gson();
        long[] arr = gson.fromJson(response.getEntity(String.class), long[].class);
        return arr;
    }
}
