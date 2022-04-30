/*
 *   Copyright 2020-2021 Google LLC
 *   Copyright 2020-2021 EPAM Systems, Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.opengroup.osdu.wd.gcp.util;

import java.util.Collections;
import java.util.List;

public class StatementBuilder {

    private final StringBuilder statement = new StringBuilder();

    public StatementBuilder select() {
        statement.append("SELECT ");
        return this;
    }

    public StatementBuilder delete() {
        statement.append("DELETE ");
        return this;
    }

    public StatementBuilder update() {
        statement.append("UPDATE ");
        return this;
    }

    public StatementBuilder all() {
        statement.append("* ");
        return this;
    }

    public StatementBuilder jdbcEntity() {
        statement.append("jdbc_entity ");
        return this;
    }

    public StatementBuilder setDeletedAt() {
        statement.append("SET deleted_at = ? ");
        return this;
    }

    public StatementBuilder entityNotDeleted(){
        statement.append("deleted_at IS null ");
        return this;
    }

    public StatementBuilder fromJdbcEntity() {
        statement.append("FROM jdbc_entity ");
        return this;
    }

    public StatementBuilder selectRelationshipsAsOneToMany() {
        statement.append("jsonb_array_elements(relationships) ");
        return this;
    }

    public StatementBuilder value() {
        statement.append("value ");
        return this;
    }

    public StatementBuilder selectVersion() {
        statement.append("data->>'version' ");
        return this;
    }

    public StatementBuilder comma() {
        statement.append(", ");
        return this;
    }

    public StatementBuilder where() {
        statement.append("WHERE ");
        return this;
    }

    public StatementBuilder and() {
        statement.append("AND ");
        return this;
    }

    public StatementBuilder filterByJsonB() {
        statement.append("data @> ?::jsonb ");
        return this;
    }

    public StatementBuilder filterByRelationship() {
        statement.append("relationships @> ?::jsonb ");
        return this;
    }

    public StatementBuilder filterByRelationshipIdLike() {
        statement.append("value->>'id' LIKE ? ");
        return this;
    }

    public StatementBuilder filterByRelationshipMultipleIdsLike(List<String> values) {
        String inSql = String.join(",", Collections.nCopies(values.size(), "?"));
        statement.append(String.format("value->>'id' LIKE ANY (array[%s])", inSql));
        return this;
    }

    public StatementBuilder orderBy() {
        statement.append("ORDER BY ");
        return this;
    }

    public StatementBuilder descending() {
        statement.append("DESC ");
        return this;
    }

    public StatementBuilder ascending() {
        statement.append("ASC ");
        return this;
    }

    public StatementBuilder id() {
        statement.append("id ");
        return this;
    }

    public StatementBuilder entityTypeIsDrillingReport() {
        statement.append("data @> '{\"entityType\":\"drillingreport\"}'");
        return this;
    }

    public StatementBuilder entityTypeIsFluidReport() {
        statement.append("data @> '{\"entityType\":\"fluidsreport\"}'");
        return this;
    }

    public StatementBuilder entityTypeIsActivityProgram() {
        statement.append("data @> '{\"entityType\":\"wellactivityprogram\"}' ");
        return this;
    }

    public StatementBuilder entityTypeIsWell() {
        statement.append("data @> '{\"entityType\":\"well\"}' ");
        return this;
    }

    public String build() {
        return statement.toString();
    }
}
