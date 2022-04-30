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

package org.opengroup.osdu.wd.azure.cosmosdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.opengroup.osdu.wd.core.models.*;
import org.opengroup.osdu.wd.core.util.Common;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CosmosEntity {

    @NonNull
    private String id;
    private String origId;
    @NonNull
    private String entityId;
    @NonNull
    private String entityType;
    private String kind;
    private long version;
    private boolean deleted;
    private ACL acl;
    private Legal legal;
    private List<Relationship> relationships;
    private String existenceKind;
    private  boolean valid;
    private String timeStamp;
    private String startTime;
    private String endTime;
    @NonNull
    private Object data;
    private Object meta;

    public CosmosEntity(EntityDto dto, List<Relationship> relationships) {
        this.id = Common.buildId(dto.getEntityId(), dto.getVersion());
        this.origId = dto.getId();
        this.entityId = dto.getEntityId();
        this.entityType = dto.getEntityType();
        this.kind = dto.getKind();
        this.version = dto.getVersion();
        this.deleted = false;
        this.acl = new ACL();
        this.acl.setOwners(dto.getAcl().getOwners());
        this.acl.setViewers(dto.getAcl().getOwners());
        this.legal = new Legal();
        this.legal.setLegaltags(dto.getLegal().getLegaltags());
        this.legal.setOtherRelevantDataCountries(dto.getLegal().getOtherRelevantDataCountries());
        this.relationships = relationships;
        this.existenceKind = dto.getExistenceKind();
        this.timeStamp = dto.getTimeStamp();
        this.startTime = dto.getStartTime();
        this.endTime = dto.getEndTime();
        this.valid = dto.isValid();
        this.data = dto.getData();
        this.meta = dto.getMeta();
    }

    public EntityDtoReturn ToEntityDtoReturn() {

        EntityDtoReturn dto = new EntityDtoReturn(
                this.origId,
                this.kind,
                this.version,
                new Legal(),
                new ACL(),
                this.valid,
                null,
                this.data,
                this.meta);
        dto.getAcl().setOwners(this.acl.getOwners());
        dto.getAcl().setViewers(this.acl.getOwners());
        dto.getLegal().setLegaltags(this.legal.getLegaltags());
        dto.getLegal().setOtherRelevantDataCountries(this.legal.getOtherRelevantDataCountries());
        return dto;
    }
}
