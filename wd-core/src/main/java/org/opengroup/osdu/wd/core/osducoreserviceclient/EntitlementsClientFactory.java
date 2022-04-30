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

import org.opengroup.osdu.core.client.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.client.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.client.entitlements.IEntitlementsFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EntitlementsClientFactory extends AbstractFactoryBean<IEntitlementsFactory> {
    private static final Logger LOGGER = Logger.getLogger(EntitlementsClientFactory.class.getName());

    @Value("${app.entitlements.api}")
    private String AUTHORIZE_API;

    @Value("${app.entitlements.api.key}")
    private String AUTHORIZE_API_KEY;

    @Override
    protected IEntitlementsFactory createInstance() throws Exception {
        LOGGER.log(Level.INFO, "AUTHORIZE_API:" + AUTHORIZE_API);
        return new EntitlementsFactory(EntitlementsAPIConfig
                .builder()
                .rootUrl(AUTHORIZE_API)
                .apiKey(AUTHORIZE_API_KEY)
                .build());
    }

    @Override
    public Class<?> getObjectType() {
        return IEntitlementsFactory.class;
    }
}