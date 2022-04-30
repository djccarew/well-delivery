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

import org.opengroup.osdu.core.client.storage.IStorageFactory;
import org.opengroup.osdu.core.client.storage.StorageAPIConfig;
import org.opengroup.osdu.core.client.storage.StorageFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class StorageClientFactory extends AbstractFactoryBean<IStorageFactory> {
    private static final Logger LOGGER = Logger.getLogger(StorageClientFactory.class.getName());

    @Value("${app.storage.api}")
    private String storageAPI;

    @Override
    public Class<?> getObjectType() {
        return IStorageFactory.class;
    }

    @Override
    protected IStorageFactory createInstance() throws Exception {
        LOGGER.log(Level.INFO, "STORAGE_API:" + storageAPI);
        return new StorageFactory(StorageAPIConfig
                .builder()
                .rootUrl(storageAPI)
                .build());
    }
}

