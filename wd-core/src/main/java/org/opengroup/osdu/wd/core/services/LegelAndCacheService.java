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

import org.opengroup.osdu.core.client.legal.ILegalClient;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.cache.CountryCodeCache;
import org.opengroup.osdu.wd.core.cache.LegalTagCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.logging.Logger;

@Service
public class LegelAndCacheService {
    private static final Logger logger = Logger.getLogger(LegelAndCacheService.class.getName());

    @Autowired
    private ILegalClient legalClient;
    @Autowired
    private LegalTagCache legalTagCache;
    @Autowired
    private CountryCodeCache countryCodeCache;
    @Autowired
    private RequestInfo requestInfo;

    public void validateLegalTag(Set<String> legalTags) {
        if(this.isLegalTagInCache(legalTags)){
            return;
        }
        this.legalClient.validateLegalTags(requestInfo.getDpsHeaders(), legalTags);
        for (String legalTag : legalTags) {
            this.legalTagCache.save(legalTag);
        }
    }

    private boolean isLegalTagInCache(Set<String> legalTags) {
        for (String legalTag : legalTags) {
            if (this.legalTagCache.get(legalTag) == null) {
                return false;
            }
        }
        return true;
    }

    public void validateCountryCode(Set<String>  countryCodes) {
        if(this.isCountryCodeInCache(countryCodes)){
            return;
        }
        this.legalClient.validateOtherRelevantDataCountries(requestInfo.getDpsHeaders(), countryCodes);
        for (String countryCode : countryCodes) {
            this.countryCodeCache.save(countryCode);
        }
    }

    private boolean isCountryCodeInCache(Set<String> countryCodes) {
        for (String countryCode : countryCodes) {
            if (this.countryCodeCache.get(countryCode) == null) {
                return false;
            }
        }
        return true;
    }
}
