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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.client.model.partition.Property;


/**
 * Azure data partition variables.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartitionInfoAzure {

    @SerializedName("id")
    private Property idConfig;

    @SerializedName("name")
    private Property nameConfig;

    @SerializedName("cosmos-connection")
    private Property cosmosConnectionConfig;

    @SerializedName("cosmos-endpoint")
    private Property cosmosEndpointConfig;

    @SerializedName("cosmos-primary-key")
    private Property cosmosPrimaryKeyConfig;

    @SerializedName("re-cosmos-connection")
    private Property reCosmosConnectionConfig;
}
