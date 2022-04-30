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

package org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@Slf4j
@ReadingConverter
public enum PostgresObjectToRelationshipsConverter implements Converter<PGobject, List<Relationship>> {
    INSTANCE;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Relationship> convert(PGobject json) {
        CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Relationship.class);
        List<Relationship> converted = null;
        String source = json.getValue();
        try {
            converted = objectMapper.readValue(source, type);
        } catch (JsonProcessingException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not convert object from JSON, the issue: {}", e.getMessage());
            }
        }
        return converted;
    }
}
