/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;

import static com.cloudant.client.api.query.Expression.gte;
import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Operation.and;
import static com.cloudant.client.api.query.Expression.regex;
import static com.cloudant.client.api.query.Expression.in;
import static com.cloudant.client.api.query.PredicatedOperation.elemMatch;


import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import org.apache.http.HttpStatus;

import org.opengroup.osdu.core.client.model.http.AppException;
import org.opengroup.osdu.wd.core.models.Relationship;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.query.PredicateExpression;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Sort;


public class CloudantdbFacade {

	private static final Logger LOGGER = Logger.getLogger(CloudantdbFacade.class.getName());

	public static CloudantEntity findLatestOne(Database entityDb, String entityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(
					new QueryBuilder(eq("entityId", entityId)).sort(Sort.desc("_id")).limit(1).build(),
					CloudantEntity.class);
			if (results.getDocs() != null)
				return results.getDocs().get(0);
			return null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from cloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}

	}

	public static CloudantEntity findSpecificOne(Database entityDb, String _id) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(eq("_id", _id)).build(),
					CloudantEntity.class);

			if (results.getDocs() != null)
				return results.getDocs().get(0);
			return null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from cloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<Long> findVersionNumbers(Database entityDb, String entityId) {
		List<CloudantEntity> list = getEntites(entityDb, entityId);
		return list.stream().map(x -> x.getVersion()).sorted().collect(Collectors.toList());
	}

	public static long deleteEntity(Database entityDb, String entityId) {
		try {
			List<CloudantEntity> docList = getEntites(entityDb, entityId);

			for (CloudantEntity doc : docList) {
				doc.setDeleted(true);
				entityDb.update(doc);
			}
			return docList.size();
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to delete entity from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static long purgeEntity(Database entityDb, String entityId) {
		try {
			List<CloudantEntity> docList = getEntites(entityDb, entityId);

			for (CloudantEntity doc : docList) {
				entityDb.remove(doc.get_id(), doc.get_rev());
			}
			return docList.size();
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to purge entity from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static int deleteOne(Database entityDb, String _id) {
		try {
			CloudantEntity doc = entityDb.find(CloudantEntity.class, _id);
			doc.setDeleted(true);
			Response res = entityDb.update(doc);
			return res == null ? 0 : 1;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to delete item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static int purgeOne(Database entityDb, String _id) {
		try {
			Response doc = entityDb.remove(entityDb.find(CloudantEntity.class, _id));
			return doc == null ? 0 : 1;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to purge item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	private static List<CloudantEntity> getEntites(Database entityDb, String entityId) {
		QueryResult<CloudantEntity> results = entityDb
				.query(new QueryBuilder(eq("entityId", entityId)).sort(Sort.desc("_id")).build(), CloudantEntity.class);
		List<CloudantEntity> docList = new ArrayList<>();
		for (CloudantEntity cloudantEntity : results.getDocs())
			docList.add(cloudantEntity);
		return docList;
	}
/////////////////////////////////////////
// Query Operation
////////////////////////////////////////

	public static CloudantEntity getLatesEntity_ByName(Database entityDb, String existenceKind,
			String name) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(
                    	eq("existenceKind",existenceKind.toLowerCase()),
                    	eq("data.FacilityName", name),
                    	eq("deleted", false)))
						.sort(Sort.desc("_id")).
						limit(1).build(), CloudantEntity.class);
			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0) : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static CloudantEntity getSpecificEntity_ByName(Database entityDb, String existenceKind,
			String name, long verison) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(eq("existenceKind", existenceKind.toLowerCase()),
					eq("data.FacilityName", name), eq("version", verison),eq("deleted", false))).build(), CloudantEntity.class);
			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0) : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<Long> getEntity_VersionNumberList_ByName(Database entityDb,
			String existenceKind, String name) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(eq("existenceKind", existenceKind.toLowerCase()),
					eq("data.FacilityName", name),eq("deleted", false))).build(), CloudantEntity.class);
			List<Long> res=results.getDocs().stream().map(x -> x.getVersion()).sorted().collect(Collectors.toList());
			List<Long> list = new ArrayList<>();
			for (Long version : res) {
				list.add(version);
			}
			if (list.size() > 0)
				Collections.sort(list);
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find versions from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<String> getIdList_Relationships_ByEntityId(Database entityDb,
			String existenceKind, String entityId, String relationshipType) {
				QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(	
							eq("entityId", entityId),
							eq("existenceKind", existenceKind.toLowerCase()),
							eq("deleted", false)))
							.sort(Sort.desc(("_id"))).limit(1)
							.build(), CloudantEntity.class);
		
			if(results.getDocs().size() < 1 || results.getDocs().get(0) == null)
	            return new ArrayList<>();
			CloudantEntity entity = results.getDocs().get(0);
	        List<Relationship> relationships =  entity.getRelationships();
	        if(relationships == null)
	            return new ArrayList<>();
	        List<String> list = new ArrayList<>();
	        for (Relationship relationship : relationships) {
	            if(relationship.getId() != null && relationship.getEntityType() != null
	                    && relationship.getEntityType().equalsIgnoreCase(relationshipType)){
	                list.add(relationship.getId());
	            }
	        }
	        return list;
	}

	public static String getId_Relationship_ByEntityId(Database entityDb, String existenceKind,
			String entityId, String relationshipType) {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
				and(	
						eq("entityId", entityId),
						eq("existenceKind", existenceKind.toLowerCase()),
						eq("deleted", false)))
						.sort(Sort.desc(("_id"))).limit(1)
						.build(), CloudantEntity.class);
	
		if(results.getDocs().size() < 1 || results.getDocs().get(0) == null)
			return null;
		CloudantEntity entity = results.getDocs().get(0);
        List<Relationship> relationships =  entity.getRelationships();
        if(relationships == null)
        	return null;
     
        for (Relationship relationship : relationships) {
            if(relationship.getId() != null && relationship.getEntityType() != null
                    && relationship.getEntityType().equalsIgnoreCase(relationshipType)){
            	 return relationship.getId();
            }
        }
        return null;
	}

	public static CloudantEntity getLatestEntity_ByRelatedEntityId(Database entityDb, String existenceKind,
			String relatedType, String relatedEntityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(	
							eq("existenceKind", existenceKind.toLowerCase()),
							eq("deleted", false),
							elemMatch("relationships",PredicateExpression.eq(
									and(
											eq("entityType", relatedType.toLowerCase()),
											regex("id", "^" + relatedEntityId + ":")
											)))))
							.sort(Sort.desc("version")).limit(1)
							.build(), CloudantEntity.class);
			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0) : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static CloudantEntity getLatestEntity_ByRelatedId(Database entityDb, String existenceKind,
			String relatedType, String relatedId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					 and(
	                            eq("existenceKind", existenceKind.toLowerCase()),
	                            eq("deleted", false),
	                            elemMatch("relationships",PredicateExpression.eq(
	                                    and(
	                                    		eq("entityType", relatedType.toLowerCase()),
	                                            eq("id", relatedId)
	                                    )
	                            ))
	                    ))
											.sort(Sort.desc("version")).limit(1).build(), CloudantEntity.class);
			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0) : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static CloudantEntity getSpecificEntity_ByRelatedEntityId(Database entityDb,
			String existenceKind, long verison, String relatedType, String relatedEntityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(
							eq("existenceKind", existenceKind.toLowerCase()),
							eq("version", verison),
							eq("deleted", false),
							elemMatch("relationships",PredicateExpression.eq(
									and(eq("entityType", relatedType.toLowerCase()),
									regex("id", "^" + relatedEntityId + ":"))))))
							.limit(1).build(), CloudantEntity.class);
			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0) : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static String getLatestId_ByRelatedEntityId(Database entityDb, String existenceKind,
			String relatedType, String relatedEntityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(and(eq("existenceKind", existenceKind.toLowerCase()), eq("deleted", false),
							elemMatch("relationships",PredicateExpression.eq(
									and(
											eq("entityType", relatedType.toLowerCase()),
											regex("id", "^" + relatedEntityId + ":"))))))
					.sort(Sort.desc("version")).limit(1).build(), CloudantEntity.class);

			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0).get_id() : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from cloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static String getLatestId_ByRelatedId(Database entityDb, String existenceKind,
			String relatedType, String relatedId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(
							eq("existenceKind", existenceKind.toLowerCase()),
							eq("deleted", false),
							elemMatch("relationships",PredicateExpression.eq(
									and(
											eq("entityType", relatedType.toLowerCase()),
											eq("id", relatedId))))))
					.sort(Sort.desc("version")).limit(1).build(), CloudantEntity.class);

			return results != null && (results.getDocs().size()> 0 ) ? results.getDocs().get(0).get_id() : null;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CLoudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<String> getIdList_LatestPerEntity_ByRelatedEntityId(Database entityDb,
			String existenceKind, String relatedType, String relatedEntityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(
							eq("existenceKind", existenceKind.toLowerCase()), 
							eq("deleted", false),
							elemMatch("relationships",PredicateExpression.eq(
									and(
											eq("entityType", relatedType.toLowerCase()),
											regex("id", "^" + relatedEntityId + ":")))))
					//group("$entityId", max("max", "$_id"))
					).build(), CloudantEntity.class);
			List<String> list = new ArrayList<>();
			/*for (CloudantEntity doc : results) {
				String current = doc.getString("max");
				if (!StringUtils.isBlank(current)) {
					list.add(current);
				}
			}*/
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<String> getIdList_LatestPerEntity_ByRelatedId(Database entityDb,
			String existenceKind, String relatedType, String relatedId) {
		try {
			/*MongoIterable<Document> res = collection
					.aggregate(
							Arrays.asList(
									match(and(eq("existenceKind", existenceKind.toLowerCase()), eq("deleted", false),
											elemMatch("relationships",PredicateExpression.eq(
													and(eq("entityType", relatedType.toLowerCase()),
															eq("id", relatedId))))))
								//	group("$entityId", max("max", "$_id"))
									));*/
			List<String> list = new ArrayList<>();
		/*	for (Document doc : res) {
				String current = doc.getString("max");
				if (!StringUtils.isBlank(current)) {
					list.add(current);
				}
			}*/
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<String> getIdList_LatestPerEntity_ByTimeRange(Database entityDb,
			String existenceKind, String startTime, String endTime) {
		try {
		/*	MongoIterable<Document> res = collection.aggregate(Arrays.asList(
					match(and(eq("existenceKind", existenceKind.toLowerCase()), eq("deleted", false),
							lte("startTime", endTime), gte("endTime", startTime))),
					group("$entityId", Accumulators.max("max", "$_id"))));*/
			List<String> list = new ArrayList<>();
		/*	for (Document doc : res) {
				String current = doc.getString("max");
				if (!StringUtils.isBlank(current)) {
					list.add(current);
				}
			}*/
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from MongoDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<Long> getEntityVersionNumberList_ByRelatedEntityId(Database entityDb,
			String existenceKind, String relatedType, String relatedEntityId) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
					and(
							eq("existenceKind", existenceKind.toLowerCase()),
							eq("deleted", false), 
							elemMatch("relationships",PredicateExpression.eq(
									and(
											eq("entityType", relatedType.toLowerCase()),
											regex("id", "^" + relatedEntityId + ":"))))))
					.build(), CloudantEntity.class);
			List<Long> res=results.getDocs().stream().map(x -> x.getVersion()).sorted().collect(Collectors.toList());
					
			List<Long> list = new ArrayList<>();
			for (Long version : res) {
				list.add(version);
			}
			if (list.size() > 0)
				Collections.sort(list);
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find version numbers from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

	public static List<CloudantEntity> getEntityList_ByIdList(Database entityDb, List idLlist) {
		try {
			QueryResult<CloudantEntity> results = entityDb.query(new QueryBuilder(
													in("_id", idLlist))
													.build(), CloudantEntity.class);
			List<CloudantEntity> list = new ArrayList<>();
			for (CloudantEntity doc : results.getDocs()) {
				list.add(doc);
			}
			return list;
		} catch (Exception e) {
			String errorMessage = "Unexpectedly failed to find item from CloudantDB";
			LOGGER.log(Level.WARNING, errorMessage, e);
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getMessage(), e);
		}
	}

}