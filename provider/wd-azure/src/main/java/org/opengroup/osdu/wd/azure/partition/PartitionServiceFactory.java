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

package org.opengroup.osdu.wd.azure.partition;

import org.opengroup.osdu.core.client.partition.IPartitionFactory;
import org.opengroup.osdu.core.client.partition.PartitionAPIConfig;
import org.opengroup.osdu.core.client.partition.PartitionFactory;
import org.opengroup.osdu.wd.core.osducoreserviceclient.SchemaClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class PartitionServiceFactory {
    private static final Logger LOGGER = Logger.getLogger(PartitionServiceFactory.class.getName());

    @Value("${app.partition.api}")
    private String PARTITON_API;

    @Bean
    public IPartitionFactory partitionFactory() {
        LOGGER.log(Level.INFO, "PARTITON_API:" + PARTITON_API);
        PartitionAPIConfig apiConfig = PartitionAPIConfig.builder()
                .rootUrl(this.PARTITON_API)
                .build();
        return new PartitionFactory(apiConfig);
    }
}
