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

package org.opengroup.osdu.wd.core.auth;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.client.entitlements.EntitlementsClient;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.opengroup.osdu.wd.core.services.EntitlementsAndCacheService;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AuthorizationFilterTest {

    private static final String ROLE1 = "role1";
    private static final String ROLE2 = "role2";
    private static final String ROLE3 = "cron.job";

    @Mock
    private DpsHeaders headers;
    @Mock
    private RequestInfo requestInfo;
    @Mock
    private EntitlementsAndCacheService cache;
    @InjectMocks
    private AuthorizationFilter sut;

    @Before
    public void setup() {
        when(headers.getAuthorization()).thenReturn("Bearer 123456");
        when(requestInfo.getDpsHeaders()).thenReturn(headers);
    }

    @Test
    public void should_authenticateRequest_when_resourceIsRolesAllowedAnnotated() {
        final String USER_EMAIL = "test@slb.com";
        when(this.cache.authorize(eq(ROLE1), eq(ROLE2))).thenReturn(USER_EMAIL);

        assertTrue(this.sut.hasRole(ROLE1, ROLE2));
        verify(headers).put(DpsHeaders.USER_EMAIL, USER_EMAIL);
    }

    @Test(expected = AppException.class)
    public void should_throwAppError_when_noAuthzProvided() {
        when(headers.getAuthorization()).thenReturn("");
        when(this.cache.authorize(eq(ROLE1), eq(ROLE2))).thenThrow(new AppException(HttpStatus.SC_MOVED_TEMPORARILY, "", ""));
        final String USER_EMAIL = "test@slb.com";

        this.sut.hasRole(ROLE1, ROLE2);
        assertEquals(USER_EMAIL, this.headers.getUserEmail());
    }
}
