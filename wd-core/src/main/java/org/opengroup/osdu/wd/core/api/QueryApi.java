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

package org.opengroup.osdu.wd.core.api;

import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.auth.EntityRole;
import org.opengroup.osdu.wd.core.models.ENTITY_TYPE;
import org.opengroup.osdu.wd.core.models.EXISTENCE_KIND;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.services.BHARunQueryService;
import org.opengroup.osdu.wd.core.services.WellboreTrajectoryQueryService;
import org.opengroup.osdu.wd.core.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestScope
@RequestMapping("query/v1")
public class QueryApi {

    @Autowired
    WellboreTrajectoryQueryService trajectoryService;

    @Autowired
    BHARunQueryService bhaRunService;

    @PostMapping("/by_well/{type}:batch")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getEntitiesByWells_planned(
            @PathVariable("type") @NotNull String type,
            @RequestBody @Valid @NotNull List<String> well_ids) {

        if(ENTITY_TYPE.WELLBORE_TRAJECTORY.equalsIgnoreCase(type))
        {
            List<EntityDtoReturn> res = this.trajectoryService.getTrajectoriesByWells(EXISTENCE_KIND.PLANNED, well_ids);
            return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
        }
        else if(ENTITY_TYPE.BHA_RUN.equalsIgnoreCase(type))
        {
            List<EntityDtoReturn> res = this.bhaRunService.getBHARunsByWells(EXISTENCE_KIND.PLANNED, well_ids);
            return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
        }
        else {
            throw new AppException(org.apache.http.HttpStatus.SC_BAD_REQUEST, "Bad Request", "Invalid entity type: " + type);
        }
    }
}
