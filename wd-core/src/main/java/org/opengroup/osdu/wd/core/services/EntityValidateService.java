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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.client.model.entitlements.Groups;
import org.opengroup.osdu.core.client.model.http.AppException;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.wd.core.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class EntityValidateService {

    private static final Logger logger = Logger.getLogger(EntityValidateService.class.getName());

    @Autowired
    private LegelAndCacheService legelClientService;
    @Autowired
    private EntitlementsAndCacheService entitlementsCache;

    public void ValidateEntity(JsonNode node, EntityDto info) {
        if (node.at("/legal") == null || node.at("/legal").isEmpty())
            throw new ValidationException("Legal are empty");
        JsonNode legalTagsNode = node.at("/legal/legaltags");
        if (!hasValues(legalTagsNode))
            throw new ValidationException("Legal Tags are empty");
        JsonNode countriesNode = node.at("/legal/otherRelevantDataCountries");
        if (!hasValues(countriesNode))
            throw new ValidationException("Countries are empty");
        if (node.at("/acl") == null || node.at("/acl").isEmpty())
            throw new ValidationException("ACL are empty");
        JsonNode ownersNode = node.at("/acl/owners");
        if (!hasValues(ownersNode))
            throw new ValidationException("Owner Acls are empty");
        JsonNode viewersNode = node.at("/acl/viewers");
        if (!hasValues(viewersNode))
            throw new ValidationException("Viewer Acls are empty");

        Set<String> legalTags = getValues(legalTagsNode);
        Set<String> countries = getValues(countriesNode);
        Set<String> viewerAcls = getValues(viewersNode);
        Set<String> ownerAcls = getValues(ownersNode);

        legelClientService.validateLegalTag(legalTags);
        legelClientService.validateCountryCode(countries);
        validateAcls(ownerAcls, viewerAcls);

        info.setLegal(new Legal());
        info.getLegal().setLegaltags(legalTags);
        info.getLegal().setOtherRelevantDataCountries(countries);
        info.setAcl(new ACL());
        info.getAcl().setOwners(ownerAcls);
        info.getAcl().setViewers(viewerAcls);
    }

    public void ValidateEntityReturnList(List<EntityDtoReturn> list) {
        Groups groups = this.entitlementsCache.getGroups();
        boolean hasAllAccess = true;
        for (EntityDtoReturn dto : list) {
            ACL acl = dto.getAcl();
            if (!hasAccess(acl, groups)) {
                this.logger.severe("The user does not have access to the entity " + dto.getId());
                hasAllAccess = false;
            }
        }
        if (!hasAllAccess)
            throw new AppException(HttpStatus.SC_FORBIDDEN, HttpErrorStrings.FORBIDDEN,
                    "The user does not have access to the entities");
    }

    public void ValidateEntityReturn(EntityDtoReturn dto) {
        Groups groups = this.entitlementsCache.getGroups();
        ACL acl =  dto.getAcl();
        if(!hasAccess(acl, groups)){
            throw new AppException(HttpStatus.SC_FORBIDDEN, HttpErrorStrings.FORBIDDEN,
                    "The user does not have access to the entity");
        }
    }

    private boolean hasAccess(ACL acl, Groups groups) {
        Set<String> aclList = new HashSet<>();
        for (String viewer : acl.getViewers()) {
            aclList.add(viewer.split("@")[0]);
        }
        for (String owner : acl.getOwners()) {
            aclList.add(owner.split("@")[0]);
        }
        String[] acls = new String[aclList.size()];
        return groups.any(aclList.toArray(acls));
    }

    private boolean hasOwnerAccess(Set<String> ownerAcls) {
        Groups groups = this.entitlementsCache.getGroups();
        Set<String> aclList = new HashSet<>();
        for (String owner : ownerAcls) {
            aclList.add(owner.split("@")[0]);
        }
        String[] acls = new String[aclList.size()];
        return groups.any(aclList.toArray(acls));
    }

    private void validateAcls(Set<String> viewerAcls, Set<String> ownerAcls) {
        Set<String> acls = new HashSet<>();
        acls.addAll(viewerAcls);
        acls.addAll(ownerAcls);
        this.entitlementsCache.validAcls(acls);
    }

    private Set<String> getValues(JsonNode node) {
        ArrayNode array = (ArrayNode) node;
        Set<String> set = new HashSet<>();
        for (JsonNode item : array) {
            set.add(item.textValue());
        }
        return set;
    }

    private boolean hasValues(JsonNode node) {
        if(node == null || !node.isArray() || node.isEmpty())
            return false;
        ArrayNode array = (ArrayNode) node;
        for (JsonNode item : array) {
            if(StringUtils.isBlank(item.textValue()))
                return  false;
        }
        return true;
    }
}
