/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.opengroup.osdu.wd.core.models.*;
import org.opengroup.osdu.wd.core.util.Common;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudantEntity {

 @NonNull
 private String _id;
 private String _rev;

 @NonNull
 private String origId;
 @NonNull
 private String entityId;
 @NonNull
 private String entityType;
 @NonNull
 private String kind;
 @NonNull
 private String schemaId;
 private long version;
 private boolean deleted;
 @NonNull
 private ACL acl;
 @NonNull
 private Legal legal;
 private List<Relationship> relationships;
 private String existenceKind;
 private boolean valid;
 private String timeStamp;
 private String startTime;
 private String endTime;
 @NonNull
 private Object data;
 private Object meta;

 public CloudantEntity(EntityDto dto, List<Relationship> relationships) {
     this._id = Common.buildId(dto.getEntityId(), dto.getVersion());
     this.origId = dto.getId();
     this.entityId = dto.getEntityId();
     this.entityType = dto.getEntityType();
     this.kind = dto.getKind();
     this.version = dto.getVersion();
     this.deleted = false;
     this.acl = new ACL();
     this.acl.setOwners(dto.getAcl().getOwners());
     this.acl.setViewers(dto.getAcl().getViewers());
     this.legal = new Legal();
     this.legal.setLegaltags(dto.getLegal().getLegaltags());
     this.legal.setOtherRelevantDataCountries(dto.getLegal().getOtherRelevantDataCountries());
     this.relationships = relationships;
     this.existenceKind = dto.getExistenceKind();
     this.valid = dto.isValid();
     this.timeStamp = dto.getTimeStamp();
     this.startTime = dto.getStartTime();
     this.endTime = dto.getEndTime();
     this.data = dto.getData();
     this.meta = dto.getMeta();
 }

 public EntityDtoReturn ToEntityDtoReturn() {
     EntityDtoReturn dto =  new EntityDtoReturn(
             this.origId,
             this.kind,
             this.version,
             new Legal(),
             new ACL(),
             this.valid,
             null,
             this.data,
             this.meta);
     dto.getAcl().setOwners(this.acl.getOwners());
     dto.getAcl().setViewers(this.acl.getOwners());
     dto.getLegal().setLegaltags(this.legal.getLegaltags());
     dto.getLegal().setOtherRelevantDataCountries(this.legal.getOtherRelevantDataCountries());
     return dto;
 }

 public Document ToDocument() {
     Gson gson = new Gson();
     String json = gson.toJson(this);
     Document doc = Document.parse(json);
     return doc;
 }

 public static CloudantEntity ToMongoEntity(Document doc) {
     Gson gson = new Gson();
     JsonWriterSettings relaxed = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
     String json = doc.toJson(relaxed);
     CloudantEntity entity = gson.fromJson(json, CloudantEntity.class);
     return entity;
 }
}
