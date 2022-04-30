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

package org.opengroup.osdu.wd.core.util;

import com.google.gson.Gson;
import org.opengroup.osdu.core.client.model.storage.Record;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RecordConversion {

    @Autowired
    private RequestInfo requestInfo;

    @Value("${app.ddmsid}")
    private String ddmsId;

    public Record toRecord(EntityDto dto) {
        String localId = dto.getEntityId();
        String globalId = generateGlobalId(localId);

        Record record = new Record(dto.getKind(), globalId);
        //assign record the metadata
        record.setVersion(dto.getVersion());
        record.getAcl().setOwners(dto.getAcl().getOwners().stream().toArray(String[]::new));
        record.getAcl().setViewers(dto.getAcl().getViewers().stream().toArray(String[]::new));
        record.getLegal().setOtherRelevantDataCountries(dto.getLegal().getOtherRelevantDataCountries());
        record.getLegal().setLegaltags(dto.getLegal().getLegaltags());

        //assign record the data
        record.getData().addProperty("localid", localId);
        record.getData().addProperty("entityType", dto.getEntityType());
        record.getData().addProperty("ddmsid", this.ddmsId);
        record.getData().add("data", new Gson().toJsonTree(dto.getData()));
        if(dto.getMeta() != null)
            record.getData().add("meta", new Gson().toJsonTree(dto.getMeta()));

        return record;
    }

    private String generateGlobalId(String localId) {return String.format("%s:wd-ddms:%s", getPartitionId(), localId); }
    private String getPartitionId(){
        return requestInfo.getDpsHeaders().getPartitionId();
    }
}
