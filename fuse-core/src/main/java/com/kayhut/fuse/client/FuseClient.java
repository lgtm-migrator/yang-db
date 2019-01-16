package com.kayhut.fuse.client;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.cursor.CreateCursorRequestDeserializer;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.jooby.MediaType;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Created by Roman on 11/05/2017.
 */
public class FuseClient {
    //region Constructor
    public FuseClient(String fuseUrl) throws IOException {
        this.fuseUrl = fuseUrl;
        this.objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class,
                new CreateCursorRequestDeserializer(Stream.ofAll(this.getCursorBindings().entrySet())
                        .map(entry -> new CompositeCursorFactory.Binding(entry.getKey(), entry.getValue(), null))
                        .toJavaList()));
        this.objectMapper.registerModule(module);
    }
    //endregion

    //region Public Methods
    public FuseResourceInfo getFuseInfo() throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.fuseUrl)), FuseResourceInfo.class);
    }

    public Object getId(String name, int numIds) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.fuseUrl + "/idgen/" + name + "?numIds=" + numIds)), Map.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException {
        return postQuery(queryStoreUrl,query, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, String query,String ontology) throws IOException {
        return postQuery(queryStoreUrl,query,ontology, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setName(id);
        request.setQuery(query);
        request.setPlanTraceOptions(planTraceOptions);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException {
        CreateJsonQueryRequest request = new CreateJsonQueryRequest();
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setName(id);
        request.setQuery(query);
        request.setOntology(ontology);
        request.setPlanTraceOptions(planTraceOptions);
        final String response = postRequest(queryStoreUrl +"/" + CreateJsonQueryRequest.TYPE, request);
        return this.objectMapper.readValue(unwrap(response), QueryResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name, CreateCursorRequest createCursorRequest) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        request.setCreateCursorRequest(createCursorRequest);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    public String initIndices(String catalogStoreUrl,String ontology) {
        return getRequest(catalogStoreUrl+"/"+ontology + "/init");
    }

    public String dropIndices(String catalogStoreUrl,String ontology) {
        return getRequest(catalogStoreUrl+"/"+ontology + "/drop");
    }

    public Map<String, Class<? extends CreateCursorRequest>> getCursorBindings() throws IOException {
        Map<String, String> cursorBindingStrings = unwrap(getRequest(this.fuseUrl + "/internal/cursorBindings"), Map.class);

        return Stream.ofAll(cursorBindingStrings.entrySet())
                .toJavaMap(entry -> {
                    try {
                        return new Tuple2<>(entry.getKey(), (Class<? extends CreateCursorRequest>)Class.forName(entry.getValue()));
                    } catch (ClassNotFoundException e) {
                        return new Tuple2<>(entry.getKey(), null);
                    }
                });
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        return this.postCursor(cursorStoreUrl, new CreatePathsCursorRequest());
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException {
        return this.objectMapper.readValue(unwrap(postRequest(cursorStoreUrl, cursorRequest)), CursorResourceInfo.class);
    }

    public PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException {
        CreatePageRequest request = new CreatePageRequest();
        request.setPageSize(pageSize);

        return this.objectMapper.readValue(unwrap(postRequest(pageStoreUrl, request)), PageResourceInfo.class);
    }

    public PageResourceInfo getPage(String pageUrl,String pageId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageUrl+"/"+pageId)), PageResourceInfo.class);
    }

    public PageResourceInfo getPage(String pageUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageUrl)), PageResourceInfo.class);
    }

    public QueryResourceInfo getQuery(String queryUrl,String queryId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(queryUrl+"/"+queryId)), QueryResourceInfo.class);
    }

    public CursorResourceInfo getCursor(String cursorUrl,String cursorId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(cursorUrl+"/"+cursorId)), CursorResourceInfo.class);
    }

    public Ontology getOntology(String ontologyUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(ontologyUrl)), Ontology.class);
    }

    public QueryResultBase getPageData(String pageDataUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageDataUrl)), QueryResultBase.class);
    }

    public String getPageDataPlain(String pageDataUrl) throws IOException {
        return getRequest(pageDataUrl, MediaType.plain.name());
    }

    public String getPlan(String planUrl) throws IOException {
        return getRequest(planUrl);
    }

    public Plan getPlanObject(String planUrl) throws IOException {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = unwrapDouble(getRequest(planUrl));
        return planWithCost.getPlan();

    }

    public Long getFuseSnowflakeId() throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.fuseUrl+"/internal/snowflakeId")), Long.class);
    }

    public String getFuseUrl() {
        return fuseUrl;
    }

    public Query getQueryObject(String v1QueryUrl) throws IOException {
        return unwrapDouble(getRequest(v1QueryUrl));

    }
    //endregion

    //region Protected Methods
    public static String postRequest(String url, Object body) throws IOException {
        return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .print();
    }

    public static String getRequest(String url) {
        return getRequest(url, "application/json");
    }

    public static String getRequest(String url, String contentType) {
        return given().contentType(contentType)
                .get(url)
                .thenReturn()
                .print();
    }

    public String deleteQuery(QueryResourceInfo queryResourceInfo) {
        return given().contentType("application/json")
                .delete(queryResourceInfo.getResourceUrl())
                .thenReturn()
                .print();
    }

    public String unwrap(String response) throws IOException {
        Map<String, Object> responseMap = this.objectMapper.readValue(response, new TypeReference<Map<String, Object>>(){});
        return this.objectMapper.writeValueAsString(responseMap.get("data"));
    }

    public <T> T unwrap(String response, Class<T> klass) throws IOException {
        return this.objectMapper.readValue(unwrap(response), klass);
    }

    public  static <T> T unwrapDouble(String response) throws IOException {
        return ((ContentResponse<T>)JsonReader.jsonToJava((String)JsonReader.jsonToJava(response))).getData();
    }
    //endregion

    //region Fields
    private String fuseUrl;

    private ObjectMapper objectMapper;
    //endregion
}