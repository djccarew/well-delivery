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

import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import org.opengroup.osdu.wd.core.services.EntitlementsAndCacheService;
import org.opengroup.osdu.wd.core.services.LegelAndCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("authorizationFilter")
@RequestScope
public class AuthorizationFilter {
    @Autowired
    private EntitlementsAndCacheService entitlementsCache;

    @Autowired
    private RequestInfo requestInfo;

    public boolean hasRole(String... requiredRoles) {
        String user = this.entitlementsCache.authorize(requiredRoles);
        requestInfo.getDpsHeaders().put(DpsHeaders.USER_EMAIL, user);
        return true;
    }
}
