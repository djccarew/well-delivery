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

import com.google.gson.JsonElement;
import com.google.gson.Gson;

import lombok.Data;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.opengroup.osdu.core.client.model.partition.PartitionException;
import org.opengroup.osdu.core.client.model.partition.PartitionInfo;
import org.opengroup.osdu.core.client.partition.IPartitionFactory;
import org.opengroup.osdu.core.client.partition.IPartitionProvider;
import org.opengroup.osdu.wd.azure.keyvault.KeyvaultClient;
import org.opengroup.osdu.wd.azure.util.AzureServicePrincipleTokenService;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.cache.MongoConnStringCache;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IMongodbConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class PartitionAndCacheService implements IMongodbConnection {

    private static final Logger logger = Logger.getLogger(PartitionAndCacheService.class.getName());

    @Autowired
    private IPartitionFactory partitionFactory;
    @Autowired
    KeyvaultClient keyvaultClient;
    @Autowired
    private AzureServicePrincipleTokenService tokenService;
    @Autowired
    private MongoConnStringCache connCache;
    @Autowired
    private RequestInfo requestInfo;

    private final Gson gson = new Gson();

    @Override
    public String get(String partitionId) {
        String connString = (String) this.connCache.get(partitionId);
        if (connString != null) {
            return connString;
        }
        connString = getReCosmosConnectionByPartitionId(partitionId);
        this.connCache.save(partitionId, connString);
        return connString;
    }

    private String getReCosmosConnectionByPartitionId(String partitionId) {
        try {
            IPartitionProvider serviceClient = getServiceClient();
            PartitionInfo partitionInfo = serviceClient.get(partitionId);
            PartitionInfoAzure partitionInfoAzure = convert(partitionInfo);
            String connString = getReCosmosConnection(partitionInfoAzure);
            return connString;
        } catch (PartitionException e) {
            throw new AppException(HttpStatus.SC_FORBIDDEN, "Service unavailable", String.format("Error getting partition info for data-partition: %s", partitionId), e);
        }
    }

    public CosmosConfig getCosmosConfig(String partitionId) {
        CosmosConfig config = new CosmosConfig();
        config.endpoint = (String) this.connCache.get(partitionId + "-endpoint");
        config.key = (String) this.connCache.get(partitionId + "-key");
        if (config.endpoint != null && config.key != null) {
            return config;
        }
        config = getCosmosConfigByPartitionId(partitionId);
        this.connCache.save(partitionId + "-endpoint", config.endpoint);
        this.connCache.save(partitionId + "-key", config.key);
        return config;
    }


    private CosmosConfig getCosmosConfigByPartitionId(String partitionId) {
        try {
            IPartitionProvider serviceClient = getServiceClient();
            PartitionInfo partitionInfo = serviceClient.get(partitionId);
            PartitionInfoAzure partitionInfoAzure = convert(partitionInfo);
            CosmosConfig config = getCosmosConnection(partitionInfoAzure);
            return config;
        } catch (PartitionException e) {
            throw new AppException(HttpStatus.SC_FORBIDDEN, "Service unavailable", String.format("Error getting partition info for data-partition: %s", partitionId), e);
        }
    }

    private PartitionInfoAzure convert(final PartitionInfo partitionInfo) {
        JsonElement jsonElement = gson.toJsonTree(partitionInfo.getProperties());
        PartitionInfoAzure infoAzure = gson.fromJson(jsonElement, PartitionInfoAzure.class);
        return infoAzure;
    }

    private String getReCosmosConnection(PartitionInfoAzure infoAzure) {
        if (infoAzure.getReCosmosConnectionConfig() == null) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "The value of re-cosmos-connection in Partition Service is null");
        }
        String connection = String.valueOf(infoAzure.getReCosmosConnectionConfig().getValue());
        if (infoAzure.getReCosmosConnectionConfig().isSensitive()) {
            return keyvaultClient.getSecretValue(connection);
        } else {
            return connection;
        }
    }

    private CosmosConfig getCosmosConnection(PartitionInfoAzure infoAzure) {
        if (infoAzure.getCosmosEndpointConfig() == null) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "The value of cosmos-endpoint in Partition Service is null");
        }
        if (infoAzure.getCosmosPrimaryKeyConfig() == null) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "The value of cosmos-key in Partition Service is null");
        }
        CosmosConfig config= new CosmosConfig();
        config.endpoint = String.valueOf(infoAzure.getCosmosEndpointConfig().getValue());
        config.key = String.valueOf(infoAzure.getCosmosPrimaryKeyConfig().getValue());
        if (infoAzure.getCosmosEndpointConfig().isSensitive()) {
            config.endpoint = keyvaultClient.getSecretValue(config.endpoint);
        }
        if (infoAzure.getCosmosPrimaryKeyConfig().isSensitive()) {
            config.key = keyvaultClient.getSecretValue(config.key);
        }
        return config;
    }

    private IPartitionProvider getServiceClient() {
        this.requestInfo.getDpsHeaders().put(DpsHeaders.AUTHORIZATION, "Bearer " + this.tokenService.getAuthorizationToken());
        return this.partitionFactory.create(this.requestInfo.getDpsHeaders());
    }

    @Data
    public class  CosmosConfig{
        String endpoint;
        String key;
    }
}
