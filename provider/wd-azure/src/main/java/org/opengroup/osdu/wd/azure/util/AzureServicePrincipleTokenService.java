// Copyright © Microsoft Corporation
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

package org.opengroup.osdu.wd.azure.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.azure.keyvault.KeyVaultFacade;
import org.opengroup.osdu.wd.azure.keyvault.KeyvaultClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Azure Service Principle token service.
 */

@Component
public class AzureServicePrincipleTokenService {

    @Autowired
    KeyvaultClient client;

    @Value("${azure.activedirectory.app-resource-id}")
    private String aadClientId;

    private final String clientIDKey = "app-dev-sp-username";
    private final String clientSecretKey = "app-dev-sp-password";
    private final String tenantIdKey = "app-dev-sp-tenant-id";

    private final AzureServicePrincipal azureServicePrincipal = new AzureServicePrincipal();

    private Map<String, Object> tokenCache = new HashMap<>();

    public String getAuthorizationToken() {
        String accessToken = "";
        try {
            IdToken cachedToken = (IdToken) this.tokenCache.get("token");
            if (!IdToken.refreshToken(cachedToken)) {
                return cachedToken.getTokenValue();
            }
            String clientID = client.getSecretValue(clientIDKey);
            String clientSecret = client.getSecretValue(clientSecretKey);
            String tenantId =  client.getSecretValue(tenantIdKey);
            accessToken = this.azureServicePrincipal.getIdToken(clientID, clientSecret, tenantId, aadClientId);
            IdToken idToken = IdToken.builder()
                    .tokenValue(accessToken)
                    .expirationTimeMillis(JWT.decode(accessToken).getExpiresAt().getTime())
                    .build();
            this.tokenCache.put("token", idToken);
        } catch (JWTDecodeException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Persistence error", "Invalid token, error decoding", e);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Persistence error", "Error generating token", e);
        }

        return accessToken;
    }
}
