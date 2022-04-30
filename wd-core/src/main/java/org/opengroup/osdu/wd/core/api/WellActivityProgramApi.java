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
import org.opengroup.osdu.wd.core.services.WellActivityProgramQueryService;
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
@RequestMapping("wellActivityPrograms/v1")
public class WellActivityProgramApi {

    @Autowired
    WellActivityProgramQueryService service;

    /**
     * Get the latest version of a well activity program object for a well
     * The parameter id is the well ID
     **/
    @GetMapping("/by_well/{well_id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellActivityProgramVersion(
            @PathVariable("well_id") String well_id) {
        EntityDtoReturn res = this.service.getLatestWellActivityProgramVersion(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Get a specific version  of a well activity program object for a well
     * The parameter id is the well ID, and version is the version of well activity program object
     **/
    @GetMapping("/by_well/{well_id}/{wap_version}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellActivityProgramVersion(
            @PathVariable("well_id") String well_id,
            @PathVariable("wap_version") long wap_version) {
        EntityDtoReturn res = this.service.getSpecificWellActivityProgramVersion(EXISTENCE_KIND.PLANNED, well_id, wap_version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Retrieve the latest version of a well activity program with a lists of URI references for a well
     * The parameter id is the well ID
     **/
    @GetMapping("reference_tree/by_well/{well_id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellActivityProgramRefTree(
            @PathVariable("well_id") String well_id) {
        Object res = this.service.getLatestWellActivityProgramRefTree(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Retrieve a specific version  of a well activity program with a lists of URI references for a well
     * The parameter id is the well ID, and version is the version of well activity program object
     **/
    @GetMapping("reference_tree/by_well/{well_id}/{wap_version}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellActivityProgramRefTree(
            @PathVariable("well_id") String well_id,
            @PathVariable("wap_version") long wap_version) {
        Object res = this.service.getSpecificWellActivityProgramRefTree(EXISTENCE_KIND.PLANNED, well_id, wap_version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/versions/by_well/{well_id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getWellActivityProgramVersionNumbers(
            @PathVariable("well_id") String well_id) {
        List<Long> res = this.service.getWellActivityProgramVersionNumbers(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("full_content/by_well/{well_id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellActivityProgramChildrenList(
            @PathVariable("well_id") String well_id) {
        List<EntityDtoReturn> res = this.service.getLatestWellActivityProgramChildrenList(EXISTENCE_KIND.PLANNED, well_id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }
}
