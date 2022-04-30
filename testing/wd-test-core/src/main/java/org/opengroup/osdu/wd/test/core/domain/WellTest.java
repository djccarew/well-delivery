package org.opengroup.osdu.wd.test.core.domain;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.wd.test.core.util.*;

import java.util.Arrays;
import java.util.Map;

public abstract class WellTest extends TestBase {

    protected static final long NOW = System.currentTimeMillis();
    protected static final String LEGAL_TAG = LegalTagUtils.createRandomName();

    private static  String WELL_TYPE = "Well";
    private static  String WELL_NAME = "Well-Name";
    protected static final String WELL_KIND = String.format("slb:well-delivery:%s:3.0.0", WELL_TYPE.toLowerCase());
    protected static final String WELL_ID = WELL_TYPE.toLowerCase() + "-" + NOW;
    protected static long WELL_VERSION;

    protected static String existenceKind = "actual";

    protected static String token;

    public static void classSetup(String theToken) throws Exception {
        System.out.println("===DrillingReportTest");
        token = theToken;
        TestUtils.disableSslVerification();

        String well_schema = SchemaUtil.buildWell();
        SchemaUtil.create(well_schema, token);

        LegalTagUtils.create(LEGAL_TAG,  token);

        String well_body = EntityUtil.buildWell(WELL_ID, WELL_KIND, LEGAL_TAG, existenceKind, WELL_NAME);
        WELL_VERSION  = EntityUtil.create(WELL_TYPE, WELL_ID, well_body, token);
        Assert.assertTrue(WELL_VERSION > 0);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(LEGAL_TAG,  token);

        EntityUtil.purgeEntity(WELL_TYPE, WELL_ID, token);
    }

    @Test
    public void shouldWellVersionNumbers_return_whenQueryVersionNumbers() throws Exception {
        String path = String.format("wells/v1/versions/by_name/%s:%s", WELL_NAME, existenceKind);
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
        Assert.assertTrue(Arrays.stream(array).anyMatch(i -> i == WELL_VERSION));
    }

    @Test
    public void should_returnLatestWell_whenQueryByWell() throws Exception {
        String path = String.format("wells/v1/by_name/%s:%s", WELL_NAME, existenceKind);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WELL_TYPE, WELL_ID);
        Assert.assertEquals(WELL_VERSION, version);
    }

    @Test
    public void should_returnWell_whenQueryByWellVersion() throws Exception {
        String path = String.format("wells/v1/by_name/%s/%s:%s", WELL_NAME, WELL_VERSION, existenceKind);
        Map<String, String> headers =  HeaderUtils.getHeaders(TenantUtils.getTenantName(), token);
        ClientResponse response = TestUtils.send(EntityUtil.getEntityUrl(), path, "GET", headers, "","");
        System.out.println(String.format("response status: %d %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        if(response.getStatus() != HttpStatus.SC_OK) {
            String message = EntityUtil.getMessage(response);
            System.out.println(message);
        }
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        long version = EntityUtil.getVersion(response, WELL_TYPE, WELL_ID);
        Assert.assertEquals(WELL_VERSION, version);
    }

    private static long[] getVersionNumbers(ClientResponse response) {
        Gson gson = new Gson();
        long[] arr = gson.fromJson(response.getEntity(String.class), long[].class);
        return arr;
    }
}
