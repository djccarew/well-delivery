/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.partition;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.client.partition.IPartitionFactory;
import org.opengroup.osdu.core.client.partition.PartitionAPIConfig;
import org.opengroup.osdu.core.client.partition.PartitionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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

