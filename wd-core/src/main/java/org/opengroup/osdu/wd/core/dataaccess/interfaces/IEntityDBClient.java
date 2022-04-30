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

package org.opengroup.osdu.wd.core.dataaccess.interfaces;

import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Relationship;

import java.util.List;

public interface IEntityDBClient {
    EntityDtoReturn saveEntity(EntityDto dto, List<Relationship> relationships);

    EntityDtoReturn getLatestEntityVersion(String entityType, String entityId);

    EntityDtoReturn  getSpecificEntityVersion(String entityType, String entityId, long version);

    List<Long> getEntityVersionNumbers(String entityType, String entityId);

    long deleteEntity(String entityType, String entityId);

    long purgeEntity(String entityType, String entityId);

    long deleteEntityVersion(String entityType, String entityId, long version);

    long purgeEntityVersion(String entityType, String entityId, long version);
}