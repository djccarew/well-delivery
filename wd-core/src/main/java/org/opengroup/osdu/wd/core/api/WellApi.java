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
import org.opengroup.osdu.wd.core.services.WellQueryService;
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
@RequestMapping("wells/v1")
public class WellApi {
    @Autowired
    WellQueryService service;

    @GetMapping("/versions/by_name/{name}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getWellVersionNumbers_actual(
            @PathVariable("name") String name) {
        List<Long> res = this.service.getWellVersionNumbers(EXISTENCE_KIND.ACTUAL, name);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_name/{name}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellVersion_actual(
            @PathVariable("name") String name) {
        EntityDtoReturn res = this.service.getLatestWellVersion(EXISTENCE_KIND.ACTUAL, name);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_name/{name}/{version}:actual")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellboreVersion_actual(
            @PathVariable("name") String name,
            @PathVariable("version") long version) {
        EntityDtoReturn res = this.service.getSpecificWellVersion(EXISTENCE_KIND.ACTUAL, name, version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/versions/by_name/{name}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getWellVersionNumbers_planned(
            @PathVariable("name") String name) {
        List<Long> res = this.service.getWellVersionNumbers(EXISTENCE_KIND.PLANNED, name);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_name/{name}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestWellVersion_planned(
            @PathVariable("name") String name) {
        EntityDtoReturn res = this.service.getLatestWellVersion(EXISTENCE_KIND.PLANNED,name);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    @GetMapping("/by_name/{name}/{version}:planned")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificWellVersion_planned(
            @PathVariable("name") String name,
            @PathVariable("version") long version) {
        EntityDtoReturn res = this.service.getSpecificWellVersion(EXISTENCE_KIND.PLANNED,name, version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }
}
