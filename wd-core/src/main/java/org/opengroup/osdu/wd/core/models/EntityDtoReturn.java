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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityDtoReturn {

    private String id;
    private String kind;
    private long version;
    private Legal legal;
    private ACL acl;
    private boolean valid;
    private List<String> errors;
    private Object data;
    private Object meta;


    public EntityDtoReturn(EntityDto dto, List<String> errors) {
        this.id = dto.getId();
        this.kind = dto.getKind();
        this.version = dto.getVersion();
        this.acl = new ACL();
        this.getAcl().setOwners(dto.getAcl().getOwners());
        this.getAcl().setViewers(dto.getAcl().getViewers());
        this.legal = new Legal();
        this.getLegal().setOtherRelevantDataCountries(dto.getLegal().getOtherRelevantDataCountries());
        this.getLegal().setLegaltags(dto.getLegal().getLegaltags());
        this.setValid(dto.isValid());
        this.setErrors(errors);
        this.data = dto.getData();
        this.meta = dto.getMeta();
    }

    public EntityDtoReturn(EntityDto dto) {
        this.id = dto.getId();
        this.kind = dto.getKind();
        this.version = dto.getVersion();
        this.acl = new ACL();
        this.getAcl().setOwners(dto.getAcl().getOwners());
        this.getAcl().setViewers(dto.getAcl().getViewers());
        this.legal = new Legal();
        this.getLegal().setOtherRelevantDataCountries(dto.getLegal().getOtherRelevantDataCountries());
        this.getLegal().setLegaltags(dto.getLegal().getLegaltags());
        this.setValid(dto.isValid());
        this.data = dto.getData();
        this.meta = dto.getMeta();
    }
}
