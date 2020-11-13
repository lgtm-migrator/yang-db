package com.yangdb.fuse.executor.sql;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import com.amazon.opendistroforelasticsearch.sql.sql.SQLService;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONObject;

import javax.inject.Inject;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;

public class FuseSqlService {
    public static final String FUSE_QUERY_ROUTE = "/fuse/query/sql";
    private SQLService service;

    @Inject
    public FuseSqlService(SQLService service) {
        this.service = service;
    }

    /**
     * Parse query and convert parse tree (CST) to abstract syntax tree (AST).
     */
    public UnresolvedPlan parse(String query) {
        return service.parse(query);
    }

    public void explain(String ontology, String query) {
        PhysicalPlan plan = plan(query);
        service.explain(plan, createExplainResponseListener());
    }

    public PhysicalPlan plan(String sql) {
        LogicalPlan analyze = service.analyze(parse(sql));
        return service.plan(analyze);
    }

    public LogicalPlan analyze(String ontology, String query) {
        return service.analyze(parse(query));
    }

    public void query(String ontology, String query, String format) {
        service.execute(new SQLQueryRequest(
                new JSONObject().put("query", query),
                query,
                FUSE_QUERY_ROUTE,
                format), createQueryResponseListener());
    }

    private ResponseListener<ExecutionEngine.QueryResponse> createQueryResponseListener() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
        return new ResponseListener<ExecutionEngine.QueryResponse>() {
            @Override
            public void onResponse(ExecutionEngine.QueryResponse response) {
                writeResponse(RestStatus.OK, formatter.format(new QueryResult(response.getSchema(), response.getResults())));
            }

            @Override
            public void onFailure(Exception e) {
//                LOG.error("Error happened during query handling", e);
                writeResponse(INTERNAL_SERVER_ERROR, formatter.format(e));
            }
        };
    }

    private void writeResponse(RestStatus status, String format) {
        //todo write response to channel / memory / index ???
    }

    private ResponseListener<ExecutionEngine.ExplainResponse> createExplainResponseListener() {
        return new ResponseListener<ExecutionEngine.ExplainResponse>() {
            @Override
            public void onResponse(ExecutionEngine.ExplainResponse response) {
                writeResponse(RestStatus.OK, new JsonResponseFormatter<ExecutionEngine.ExplainResponse>(PRETTY) {
                    @Override
                    protected Object buildJsonObject(ExecutionEngine.ExplainResponse response) {
                        return response;
                    }
                }.format(response));
            }

            @Override
            public void onFailure(Exception e) {
//                LOG.error("Error happened during explain", e);
                writeResponse(INTERNAL_SERVER_ERROR,
                        "Failed to explain the query due to error: " + e.getMessage());
            }
        };
    }

}
