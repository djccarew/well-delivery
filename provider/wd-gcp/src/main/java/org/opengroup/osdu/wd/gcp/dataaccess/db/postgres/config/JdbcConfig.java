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

package org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.EntityDtoToPostgresObjectConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.PostgresObjectToEntityDtoConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.PostgresObjectToRelationshipsConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.RelationshipsToPostgresObjectConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        final List<Converter<?, ?>> converters = new ArrayList<>();

        converters.add(EntityDtoToPostgresObjectConverter.INSTANCE);
        converters.add(PostgresObjectToEntityDtoConverter.INSTANCE);
        converters.add(RelationshipsToPostgresObjectConverter.INSTANCE);
        converters.add(PostgresObjectToRelationshipsConverter.INSTANCE);

        return new JdbcCustomConversions(converters);
    }
}
