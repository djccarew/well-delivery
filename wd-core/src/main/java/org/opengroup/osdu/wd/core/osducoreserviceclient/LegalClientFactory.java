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



import org.opengroup.osdu.core.client.legal.ILegalFactory;
import org.opengroup.osdu.core.client.legal.LegalAPIConfig;
import org.opengroup.osdu.core.client.legal.LegalFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LegalClientFactory extends AbstractFactoryBean<ILegalFactory> {
    private static final Logger LOGGER = Logger.getLogger(LegalClientFactory.class.getName());

    @Value("${app.legal.api}")
    private String LEGALTAG_API;

    @Override
    public Class<?> getObjectType() {
        return ILegalFactory.class;
    }

    @Override
    protected ILegalFactory createInstance() throws Exception {
        LOGGER.log(Level.INFO, "LEGALTAG_API:" + LEGALTAG_API);
        return new LegalFactory(LegalAPIConfig
                .builder()
                .rootUrl(LEGALTAG_API)
                .build());
    }
}
