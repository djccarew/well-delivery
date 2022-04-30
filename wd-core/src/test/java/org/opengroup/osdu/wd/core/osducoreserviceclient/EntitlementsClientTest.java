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

package org.opengroup.osdu.wd.core.osducoreserviceclient;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.opengroup.osdu.core.client.entitlements.EntitlementsClient;
import org.opengroup.osdu.core.client.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.client.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.client.http.HttpResponse;
import org.opengroup.osdu.core.client.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.client.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.client.model.entitlements.Groups;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.powermock.modules.junit4.PowerMockRunner;

import org.opengroup.osdu.wd.core.logging.ILogger;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class EntitlementsClientTest {

    private static final String MEMBER_EMAIL = "tester@gmail.com";
    private static final String HEADER_ACCOUNT_ID = "anyTenant";
    private static final String HEADER_AUTHORIZATION = "anyCrazyToken";

    @Mock
    IEntitlementsFactory factory;
    @Mock
    IEntitlementsService service;
    @Mock
    ILogger log;
    @Mock
    private DpsHeaders dpsHeaders;
    @InjectMocks
    EntitlementsClient sut;

    private DpsHeaders headers;

    @Before
    public void setup(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(DpsHeaders.ACCOUNT_ID, HEADER_ACCOUNT_ID);
        headerMap.put(DpsHeaders.AUTHORIZATION, HEADER_AUTHORIZATION);
        this.headers = DpsHeaders.createFromMap(headerMap);

        Mockito.when(factory.create(this.headers)).thenReturn(this.service);
    }

    @Test
    public void should_returnMemberEmail_when_authorizationIsSuccessfull() throws Exception {

        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@gmail.com");
        g1.setName("role1");

        GroupInfo g2 = new GroupInfo();
        g2.setEmail("role2@gmail.com");
        g2.setName("role2");

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setDesId(MEMBER_EMAIL);

        when(this.service.getGroups()).thenReturn(groups);

        assertEquals(MEMBER_EMAIL, this.sut.authorize(this.headers, "role2"));
    }

    @Test
    public void should_returnHttp403_when_userDoesNotBelongToRoleGroup() throws EntitlementsException {

        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@gmail.com");
        g1.setName("role1");

        GroupInfo g2 = new GroupInfo();
        g2.setEmail("role2@gmail.com");
        g2.setName("role2");

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setDesId(MEMBER_EMAIL);

        when(this.service.getGroups()).thenReturn(groups);

        try {
            this.sut.authorize(this.headers, "role3");

            fail("Should not succeed");
        } catch (AppException e) {
            assertEquals(HttpStatus.SC_FORBIDDEN, e.getError().getCode());
            assertEquals("Access denied", e.getError().getReason());
            assertEquals("The user is not authorized to perform this action", e.getError().getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void should_throwAppException_when_entitlementExceptionHappens() throws EntitlementsException {

        final String ERROR_MSG = "FATAL ERROR";

        HttpResponse response = mock(HttpResponse.class);
        when(response.isServerErrorCode()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        EntitlementsException expectedException = new EntitlementsException(ERROR_MSG, response);

        when(this.service.getGroups()).thenThrow(expectedException);

        try {
            this.sut.authorize(this.headers, "role3");

            fail("Should not succeed");
        } catch (AppException e) {
            assertEquals(HttpStatus.SC_FORBIDDEN, e.getError().getCode());
            assertEquals("Access denied", e.getError().getReason());
            assertEquals("The user is not authorized to perform this action", e.getError().getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    /*@Test
    public void should_returnTrue_when_AclIsValid() throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@tenant.gmail.com");
        g1.setName("role1");
        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");
        acls.add("valid2@tenant.gmail.com");

        assertEquals(true, this.sut.isValidAcl(this.headers, acls));
    }*/

    /*
    @Test
    public void should_returnFalse_when_AclIsNotValid() throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@tenant.gmail.com");
        g1.setName("role1");
        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");
        acls.add("invalid@test.whatever.com");

        assertEquals(false, this.sut.isValidAcl(this.headers, acls));
    }*/

    /*
    @Test(expected = AppException.class)
    public void should_throwAppException_when_NoGroupGotFromCacheOrEntitlements() throws EntitlementsException {
        List<GroupInfo> groupsInfo = new ArrayList<>();
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");

        this.sut.isValidAcl(this.headers, acls);
    }*/

    /*
    @Test(expected = AppException.class)
    public void should_throwAppException_when_EmailOfGroupNotMatchingValidRegex_NoAtSymbol()
            throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("test.tenant.gmail.com");
        g1.setName("role1");
        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");

        this.sut.isValidAcl(this.headers, acls);
    }*/

    /*
    @Test(expected = AppException.class)
    public void should_throwAppException_when_EmailOfGroupNotMatchingValidRegex_NoGroupName()
            throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("@tenant.gmail.com");
        g1.setName("role1");
        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");

        this.sut.isValidAcl(this.headers, acls);
    }*/

    /*
    @Test(expected = AppException.class)
    public void should_throwAppException_when_EmailOfGroupNotMatchingValidRegex_DomainTooSimple()
            throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("test@tenantgmailcom");
        g1.setName("role1");
        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        when(this.service.getGroups()).thenReturn(groups);

        Set<String> acls = new HashSet<>();
        acls.add("valid@tenant.gmail.com");

        this.sut.isValidAcl(this.headers, acls);
    }*/

    /*
    @Test
    public void should_returnTrue_when_aclContainedInGroups() throws EntitlementsException {
        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@slb.com");
        g1.setName("role1");

        GroupInfo g2 = new GroupInfo();
        g2.setEmail("role2@slb.com");
        g2.setName("role2");

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setDesId(MEMBER_EMAIL);

        when(this.service.getGroups()).thenReturn(groups);

        String[] viewers = new String[]{"role1@slb.com"};
        String[] owners = new String[]{"role2@slb.com"};
        Acl storageAcl = new Acl();
        storageAcl.setOwners(owners);
        storageAcl.setViewers(viewers);

        RecordMetadata recordMetadata = new RecordMetadata();
        recordMetadata.setAcl(storageAcl);
        recordMetadata.setId("acl-check-1");

        List<RecordMetadata> input = new ArrayList<>();
        input.add(recordMetadata);

        List<RecordMetadata> result = this.sut.hasValidAccess(input, this.headers);
        assertEquals(1, result.size());
        assertEquals("acl-check-1", result.get(0).getId());
    }
*/
    /*
    @Test
    public void should_returnTrue_when_aclNotContainedInGroups() throws EntitlementsException {

        GroupInfo g1 = new GroupInfo();
        g1.setEmail("role1@slb.com");
        g1.setName("role1");

        GroupInfo g2 = new GroupInfo();
        g2.setEmail("role2@slb.com");
        g2.setName("role2");

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setDesId(MEMBER_EMAIL);

        when(this.service.getGroups()).thenReturn(groups);

        String[] viewers = new String[]{"role3@slb.com"};
        String[] owners = new String[]{"role4@slb.com"};
        Acl storageAcl = new Acl();
        storageAcl.setOwners(owners);
        storageAcl.setViewers(viewers);

        RecordMetadata recordMetadata = new RecordMetadata();
        recordMetadata.setAcl(storageAcl);
        recordMetadata.setId("acl-check-2");

        List<RecordMetadata> input = new ArrayList<>();
        input.add(recordMetadata);

        List<RecordMetadata> result = this.sut.hasValidAccess(input, this.headers);
        assertEquals(0, result.size());
    }*/
}

