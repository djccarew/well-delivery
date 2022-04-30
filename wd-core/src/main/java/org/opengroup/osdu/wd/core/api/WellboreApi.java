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

import org.opengroup.osdu.wd.core.auth.EntityRole;
import org.opengroup.osdu.wd.core.models.EXISTENCE_KIND;
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.services.WellboreQueryService;
import org.opengroup.osdu.wd.core.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@RestController
@RequestScope
@RequestMapping("wellbores/v1")
public class WellboreApi {

    @Autowired
    WellboreQueryService service;

    @GetMapping("/versions/by_well/{well_id}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getWellboreVersionNumbers_actual(
            @PathVariable("well_id") String well_id) {
        List<Long> res = this.service.getWellboreVersionNumbers(EXISTENCE_KIND.ACTUAL, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_well/{well_id}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellboreVersion_actual(
            @PathVariable("well_id") String well_id) {
        EntityDtoReturn res = this.service.getLatestWellboreVersion(EXISTENCE_KIND.ACTUAL, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_well/{well_id}/{wellbore_version}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellboreVersion_actual(
            @PathVariable("well_id") String well_id,
            @PathVariable("wellbore_version") long wellbore_version) {
        EntityDtoReturn res = this.service.getSpecificWellboreVersion(EXISTENCE_KIND.ACTUAL, well_id, wellbore_version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/versions/by_well/{well_id}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getWellboreVersionNumbers_planned(
            @PathVariable("well_id") String well_id) {
        List<Long> res = this.service.getWellboreVersionNumbers(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_well/{well_id}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellboreVersion_planned(
            @PathVariable("well_id") String well_id) {
        EntityDtoReturn res = this.service.getLatestWellboreVersion(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_well/{well_id}/{wellbore_version}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellboreVersion_planned(
            @PathVariable("well_id") String well_id,
            @PathVariable("wellbore_version") long wellbore_version) {
        EntityDtoReturn res = this.service.getSpecificWellboreVersion(EXISTENCE_KIND.PLANNED, well_id, wellbore_version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }
}
