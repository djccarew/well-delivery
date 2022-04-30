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

import java.util.List;

@Service
public class HoleSectionQueryService {
    @Autowired
    private IQueryClient queryClient;
    @Autowired
    private EntityValidateService validate;

    public List<EntityDtoReturn> getSectionsByWellbore(String existenceKind, String wellboreId) {
        List<EntityDtoReturn> res = queryClient.getHoleSectionsByWellbore(existenceKind, wellboreId);
        if (res.isEmpty())
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Not found wellbore segments with wellbore id :" + wellboreId);
        validate.ValidateEntityReturnList(res);
        return res;
    }
}
