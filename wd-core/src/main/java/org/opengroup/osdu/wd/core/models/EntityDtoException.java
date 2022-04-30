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

package org.opengroup.osdu.wd.core.models;

import lombok.EqualsAndHashCode;
import org.opengroup.osdu.core.client.http.HttpResponse;
import org.opengroup.osdu.core.client.model.http.DpsException;


@EqualsAndHashCode(callSuper = false)
public class EntityDtoException extends DpsException {

    private static final long serialVersionUID = 2694789225576271097L;

    public EntityDtoException(String message, HttpResponse httpResponse) {
        super(message, httpResponse);
    }
}
