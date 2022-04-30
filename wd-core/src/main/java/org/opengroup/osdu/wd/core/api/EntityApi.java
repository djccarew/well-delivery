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
import org.opengroup.osdu.wd.core.models.EntityDtoReturn;
import org.opengroup.osdu.wd.core.models.VersionNumbers;
import org.opengroup.osdu.wd.core.util.Common;
import org.opengroup.osdu.wd.core.services.EntityStorageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestScope
@RequestMapping("storage/v1")
@Validated
public class EntityApi {

    @Autowired
    private EntityStorageService service;

    /**
     * Create or update entity to system
     **/
    @PutMapping("/{type}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> createOrUpdateEntities(
            @PathVariable("type") String type,
            @RequestBody @Valid @NotNull Object entity) {
        EntityDtoReturn res = this.service.createOrUpdateEntities(type, entity);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.CREATED);
    }

    /**
     * Get the latest version of entity
     * Parameter type is entity type, id is entity ID
     **/
    @GetMapping("/{type}/{id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getLatestEntityVersion(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        EntityDtoReturn res = this.service.getLatestEntityVersion(type, id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Get a specific version of entity
     * Parameter type is entity type, id is entity ID, and version is the entity version number
     **/
    @GetMapping("/{type}/{id}/{version}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getSpecificEntityVersion(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @PathVariable("version") long version) {
        EntityDtoReturn res = this.service.getSpecificEntityVersion(type, id, version);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Get a list of version numbers for an entity
     * Parameter type is entity type, id is entity ID
     **/
    @GetMapping("/{type}/versions/{id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.VIEWER + "', '" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<String> getEntityVersionNumbers(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        VersionNumbers res = this.service.getEntityVersionNumbers(type, id);
        return new ResponseEntity<String>(Common.toPrettyString(res), HttpStatus.OK);
    }

    /**
     * Soft delete all versions for an entity
     * Parameter type is entity type, id is entity ID
     **/
    @DeleteMapping("/{type}/{id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<Void> deleteEntity(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        this.service.deleteEntity(type, id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    /**
     * Hard delete all versions for an entity
     * Parameter type is entity type, id is entity ID
     **/
    @DeleteMapping("/{type}/{id}:purge")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.ADMIN + "')")
    public ResponseEntity<Void> purgeEntity(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        this.service.purgeEntity(type, id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    /**
     * Soft delete a specific version of entity
     * Parameter type is entity type, id is entity ID, and version is the entity version number
     **/
    @DeleteMapping("/{type}/{id}/{version}")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.CREATOR + "', '" + EntityRole.ADMIN + "')")
    public ResponseEntity<Void> deleteEntityVersion(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @PathVariable("version") long version) {
        this.service.deleteEntityVersion(type, id, version);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    /**
     * Hard delete a specific version of entity
     * Parameter type is entity type, id is entity ID, and version is the entity version number
     **/
    @DeleteMapping("/{type}/{id}/{version}:purge")
    @PreAuthorize("@authorizationFilter.hasRole('" + EntityRole.ADMIN + "')")
    public ResponseEntity<Void> purgeEntityVersion(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @PathVariable("version") long version) {
        this.service.purgeEntityVersion(type, id, version);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
