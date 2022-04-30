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


import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.HttpErrorStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityPlanQueryService {
    @Autowired
    private IQueryClient queryClient;
    @Autowired
    private EntityValidateService validate;

    public EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellId) {
        EntityDtoReturn dto = queryClient.getLatestActivityPlanByWell(existenceKind, wellId);
        if (dto == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Not found activity plan with well id: " + wellId);
        validate.ValidateEntityReturn(dto);
        return dto;
    }
}
