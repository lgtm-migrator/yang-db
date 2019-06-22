package com.kayhut.fuse.services;

/*-
 * #%L
 * fuse-domain-knowledge-poc
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.logging.Route;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateJsonQueryRequest;
import com.kayhut.fuse.model.transport.ExecutionScope;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.kayhut.fuse.services.controllers.StandardQueryController;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Result;
import org.jooby.Results;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

public class KnowledgeExtensionRegistrar extends AppControllerRegistrarBase<KnowledgeExtensionQueryController> {

    //region Constructors
    public KnowledgeExtensionRegistrar() {
        super(KnowledgeExtensionQueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("ext", () -> Results.redirect("/public/assets/swagger/swagger-ext.json"));

        /** create a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause",req -> postClause(app,req,this));
        /** run a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause/run",req -> runClause(app,req,this));

    }

    public static Result postClause(Jooby app, final Request req, KnowledgeExtensionRegistrar registrar  ) throws Exception {
        Route.of("postClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject((Map) createQueryRequest.get("query")).toString();
        CreateJsonQueryRequest request = new CreateJsonQueryRequest(
                createQueryRequest.get("id").toString(),
                createQueryRequest.get("name").toString(),
                createQueryRequest.get("queryType").toString(),
                query,
                createQueryRequest.get("ontology").toString());

        req.set(CreateJsonQueryRequest.class, request);
        req.set(PlanTraceOptions.class, request.getPlanTraceOptions());
        final long maxExecTime = request.getCreateCursorRequest() != null
                ? request.getCreateCursorRequest().getMaxExecutionTime() : 0;
        req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));

        ContentResponse<QueryResourceInfo> response = request.getCreateCursorRequest() == null ?
                registrar.getController(app).create(request) :
                registrar.getController(app).createAndFetch(request);

        return Results.with(response, response.status());

    }

    public static Result runClause(Jooby app, final Request req, KnowledgeExtensionRegistrar registrar  ) throws Exception {
        Route.of("runClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject(Collections.singletonMap("clause",createQueryRequest.get("clause"))).toString();
        String ontology = req.param("ontology").value();
        req.set(ExecutionScope.class, new ExecutionScope(1000 * 60 * 10));

        ContentResponse<Object> response = registrar.getController(app).run(query,ontology);

        return Results.with(response, response.status());
    }
    //endregion
}