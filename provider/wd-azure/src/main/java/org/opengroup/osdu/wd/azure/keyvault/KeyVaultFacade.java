//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.wd.azure.keyvault;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyVaultFacade {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultFacade.class.getName());

    public static String getSecretWithValidation(SecretClient client, String secretName) {
        if (client == null) {
            String errorMessage = "Secret Client is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        KeyVaultSecret secret = client.getSecret(secretName);
        if (secret == null) {
            String errorMessage = "Secret is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        String secretValue = secret.getValue();
        if (StringUtils.isBlank(secretValue)) {
            String errorMessage = "Secret value is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        return secretValue;
    }

    public static SecretClient keyVaultSecretClient(DefaultAzureCredential credential, String keyVaultURL) {
        if (credential == null) {
            String errorMessage = "Credential is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        if (StringUtils.isBlank(keyVaultURL)) {
            String errorMessage = "KV URL is null";
            LOGGER.log(Level.WARNING, errorMessage);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, errorMessage);
        }
        return new SecretClientBuilder()
                .credential(credential)
                .vaultUrl(keyVaultURL)
                .buildClient();
    }

    public static DefaultAzureCredential azureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }
}
