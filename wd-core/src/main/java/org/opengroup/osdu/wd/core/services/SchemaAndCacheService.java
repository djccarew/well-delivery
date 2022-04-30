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

package org.opengroup.osdu.wd.core.services;

import org.opengroup.osdu.core.client.schema.ISchemaClient;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.cache.SchemaCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaAndCacheService {

    @Autowired
    private ISchemaClient schemaClient;
    @Autowired
    private SchemaCache schemaCache;
    @Autowired
    private RequestInfo requestInfo;

    public Object getSchema(String id) {
        Object schema = schemaCache.get(id);
        if (schema != null) {
            return schema;
        }

        schema = schemaClient.getSchema(requestInfo.getDpsHeaders(), id);

        schemaCache.save(id, schema);
        return schema;
    }
}
