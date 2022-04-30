/*
 *   Copyright 2020-2021 Google LLC
 *   Copyright 2020-2021 EPAM Systems, Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.opengroup.osdu.wd.gcp.partition;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.client.partition.IPartitionFactory;
import org.opengroup.osdu.core.client.partition.PartitionAPIConfig;
import org.opengroup.osdu.core.client.partition.PartitionFactory;
import org.opengroup.osdu.wd.gcp.config.PropertiesConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartitionServiceFactory {

    private static final Logger LOGGER = Logger.getLogger(PartitionServiceFactory.class.getName());

    private final PropertiesConfiguration configuration;

    @Bean
    public IPartitionFactory partitionFactory() {
        LOGGER.log(Level.INFO, "PARTITON_API:" + configuration.getPartitionApi());
        PartitionAPIConfig apiConfig = PartitionAPIConfig.builder()
            .rootUrl(configuration.getPartitionApi())
            .build();
        return new PartitionFactory(apiConfig);
    }
}
