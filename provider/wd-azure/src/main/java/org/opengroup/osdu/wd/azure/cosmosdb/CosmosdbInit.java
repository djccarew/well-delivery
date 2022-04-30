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

package org.opengroup.osdu.wd.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosContainerResponse;
import org.opengroup.osdu.wd.azure.partition.PartitionAndCacheService;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CosmosdbInit {

    @Autowired
    PartitionAndCacheService partitionService;
    @Autowired
    private RequestInfo requestInfo;

    @Value("${azure.cosmosdb.database}")
    private String cosmosDBName;

    private CosmosClient cosmosClient;
    private CosmosContainer entityContainer;

    public CosmosContainer getEntityContainer(String entityType) {
        if (cosmosClient == null)
            cosmosClient = getCosmosClient();
        String containerName = entityType + "Container";
        if (entityContainer == null || !entityContainer.getId().equalsIgnoreCase(containerName))
            entityContainer = CosmosdbFacade.getContainer(cosmosClient, this.cosmosDBName, containerName);
        return entityContainer;
    }

    public CosmosContainer getEntityContainerOrNull(String entityType) {
        if (cosmosClient == null)
            cosmosClient = getCosmosClient();
        String containerName = entityType + "Container";
        if (entityContainer == null || !entityContainer.getId().equalsIgnoreCase(containerName))
            entityContainer = CosmosdbFacade.getContainerOrNull(cosmosClient, this.cosmosDBName, containerName);
        return entityContainer;
    }

    public CosmosContainer createOrGetEntityContainer(String entityType) {
        if (cosmosClient == null)
            cosmosClient = getCosmosClient();
        String containerName = entityType + "Container";
        if (entityContainer == null || !entityContainer.getId().equalsIgnoreCase(containerName))
            entityContainer = CosmosdbFacade.createOrGetContainer(cosmosClient, this.cosmosDBName, containerName);
        return entityContainer;
    }

    private CosmosClient getCosmosClient() {
        String partitionId = requestInfo.getDpsHeaders().getPartitionId();
        PartitionAndCacheService.CosmosConfig config = partitionService.getCosmosConfig(partitionId);
        return CosmosdbFacade.getClient(config.getEndpoint(), config.getKey());
    }
}
