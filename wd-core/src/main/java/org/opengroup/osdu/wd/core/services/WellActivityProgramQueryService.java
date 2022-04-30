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

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.dataaccess.interfaces.IQueryClient;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.HttpErrorStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class WellActivityProgramQueryService {
    @Autowired
    private IQueryClient queryClient;
    @Autowired
    private EntityValidateService validate;

    public EntityDtoReturn getLatestWellActivityProgramVersion(String existenceKind, String wellId) {
        EntityDtoReturn dto = queryClient.getLatestWellActivityProgramByWell(existenceKind, wellId);
        if (dto == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find related well activity program with well id: " + wellId);
        validate.ValidateEntityReturn(dto);
        return dto;
    }

    public EntityDtoReturn getSpecificWellActivityProgramVersion(String existenceKind, String wellId, long wapVersion) {
        EntityDtoReturn dto = queryClient.getSpecificWellActivityProgramByWell(existenceKind, wellId, wapVersion);
        if (dto == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find related well activity program with well id: " + wellId + " and specific version " + wapVersion);
        validate.ValidateEntityReturn(dto);
        return dto;
    }

    public Object getLatestWellActivityProgramRefTree(String existenceKind, String wellId) {
        Object res = this.queryClient.getLatestWellActivityProgramRefTreeByWell(existenceKind, wellId);
        if (res == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find related well activity program reference tree with well id: " + wellId);
        return res;
    }

    public Object getSpecificWellActivityProgramRefTree(String existenceKind, String wellId, long wapVersion) {
        Object res = this.queryClient.getSpecificWellActivityProgramRefTreeByWell(existenceKind, wellId, wapVersion);
        if (res == null)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find well activity program reference tree with well id " + wellId + " and specific version " + wapVersion);
        return res;
    }

    public List<Long> getWellActivityProgramVersionNumbers(String existenceKind, String wellId) {
        List<Long> list = this.queryClient.getWellActivityProgramVersionNumbersByWell(existenceKind, wellId);
        if (list.size() == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find related well activity program with well id: " + wellId);
        return list;
    }

    public List<EntityDtoReturn> getLatestWellActivityProgramChildrenList(String existenceKind, String wellId) {
        List<EntityDtoReturn> list = this.queryClient.getLatestWellActivityProgramChildrenListByWell(existenceKind, wellId);
        if (list.size() == 0)
            throw new AppException(HttpStatus.SC_NOT_FOUND, HttpErrorStrings.NOT_FOUND, "Could not find related well activity program reference tree with well id: " + wellId);
        return list;
    }
}
