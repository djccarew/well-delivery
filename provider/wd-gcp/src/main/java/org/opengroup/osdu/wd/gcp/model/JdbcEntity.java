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

package org.opengroup.osdu.wd.gcp.model;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.EntityDtoToPostgresObjectConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.PostgresObjectToEntityDtoConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.PostgresObjectToRelationshipsConverter;
import org.opengroup.osdu.wd.gcp.dataaccess.db.postgres.converter.RelationshipsToPostgresObjectConverter;
import org.postgresql.util.PGTimestamp;
import org.postgresql.util.PGobject;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class JdbcEntity {

    @Id
    private Long id;
    private PGobject data;
    private PGobject relationships;
    private PGTimestamp deletedAt;

    public JdbcEntity(Long id, EntityDto entityDto, List<Relationship> relationships) {
        this.id = id;
        this.data = EntityDtoToPostgresObjectConverter.INSTANCE.convert(entityDto);
        this.relationships = RelationshipsToPostgresObjectConverter.INSTANCE.convert(relationships);
    }

    public void setDataFromEntityDto(EntityDto entityDto) {
        data = EntityDtoToPostgresObjectConverter.INSTANCE.convert(entityDto);
    }

    public EntityDto getEntityDtoFromData() {
        EntityDto convertedEntityDto = PostgresObjectToEntityDtoConverter.INSTANCE.convert(data);
        return convertedEntityDto == null ? new EntityDto() : convertedEntityDto;
    }

    public void setRelationshipsFromDto(List<Relationship> relationships) {
        this.relationships = RelationshipsToPostgresObjectConverter.INSTANCE.convert(relationships);
    }

    public List<Relationship> getRelationshipsDto() {
        List<Relationship> convertedRelationships = PostgresObjectToRelationshipsConverter.INSTANCE.convert(this.relationships);
        return convertedRelationships == null ? Collections.emptyList() : convertedRelationships;
    }
}
