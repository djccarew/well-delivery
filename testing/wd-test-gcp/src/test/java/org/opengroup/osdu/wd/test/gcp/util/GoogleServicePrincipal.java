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

package org.opengroup.osdu.wd.test.gcp.util;

import java.io.IOException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.gcp.auth.GoogleServiceAccount;
import org.springframework.http.HttpStatus;

public class GoogleServicePrincipal {

    public static String getIdToken(String testerAccount, String audience) throws Exception {
        try {
            return new GoogleServiceAccount(testerAccount).getAuthToken(audience);
        } catch (IOException e) {
            throw new AppException(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "The user is unauthorized to perform this action");
        }
    }
}
