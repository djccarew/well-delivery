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

package org.opengroup.osdu.wd.azure.keyvault;

import com.azure.identity.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class KeyvaultClient {

    private static final Logger LOGGER = Logger.getLogger(KeyvaultClient.class.getName());

    @Value("${azure.keyvault.url}")
    private String keyVaultURL;

    public String getSecretValue(String key) {
        try {
            SecretClient secretClient = buildSecretsClient();
            String value = KeyVaultFacade.getSecretWithValidation(secretClient, key);
            LOGGER.log(Level.INFO, String.format("Secret %s: %s***", key, value.substring(0, 3)));
            return value;
        }catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", String.format("Error getting Secret for key: %s", key), e);
        }
    }

    private SecretClient buildSecretsClient() {
        LOGGER.log(Level.INFO, String.format("keyVaultURL: %s", this.keyVaultURL));
        DefaultAzureCredential credential = KeyVaultFacade.azureCredential();
        SecretClient secretClient = KeyVaultFacade.keyVaultSecretClient(credential, this.keyVaultURL);
        return secretClient;
    }
}
