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

package org.opengroup.osdu.wd.core.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.entitlements.IEntitlementsClient;
import org.opengroup.osdu.core.client.model.entitlements.Groups;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.cache.GroupCache;
import org.opengroup.osdu.wd.core.util.Crc32c;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class EntitlementsAndCacheService {
    private static final Logger logger = Logger.getLogger(EntitlementsAndCacheService.class.getName());

    @Autowired
    private IEntitlementsClient entitlementsClient;
    @Autowired
    private GroupCache groupCache;
    @Autowired
    private RequestInfo requestInfo;

    public String authorize(String... roles) {
        if(StringUtils.isBlank(requestInfo.getDpsHeaders().getAuthorization()))
            throw new AppException(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect", "No credentials sent on request.");

        Groups groups = this.getGroups();
        if (groups.any(roles)) {
            return groups.getDesId();
        } else {
            //will throw appexception if not authorized so can always return true
            throw new AppException(HttpStatus.SC_FORBIDDEN, "Access denied", "The user is not authorized to perform this action");
        }
    }

    public void validAcls(Set<String> acls) {
        Groups groups = this.getGroups();
        List<String> GroupEmails = groups.getGroups().stream().map(x->x.getEmail()).collect(Collectors.toList());

        for (String acl : acls) {
            if (!GroupEmails.contains(acl)) {
                throw new AppException(HttpStatus.SC_BAD_REQUEST, "Invalid ACL", "Invalid group name: " + acl);
            }
        }
    }

    public Groups getGroups() {
        DpsHeaders headers = requestInfo.getDpsHeaders();
        String cacheKey = this.getGroupCacheKey(headers);
        Groups groups = (Groups) this.groupCache.get(cacheKey);
        if (groups != null) {
            return groups;
        }
        groups = entitlementsClient.getGroups(headers);
        this.groupCache.save(cacheKey, groups);
        this.logger.info("Entitlements cache miss");
        return groups;
    }

    protected static String getGroupCacheKey(DpsHeaders headers) {
        String key = String.format("entitlement-groups:%s:%s", headers.getPartitionIdWithFallbackToAccountId(),
                headers.getAuthorization());
        return Crc32c.hashToBase64EncodedString(key);
    }
}
