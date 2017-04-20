package com.kayhut.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import org.jooby.test.JoobyRule;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataTest {

    @ClassRule
    public static JoobyRule createApp() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getNextResults(anyInt())).thenReturn(QueryResult.QueryResultBuilder.aQueryResult().build());

        CursorFactory cursorFactory = mock(CursorFactory.class);
        when(cursorFactory.createCursor(any())).thenReturn(cursor);

        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.mockEngine.dev.conf")
                .injector((stage, module) -> {
                    return Guice.createInjector(stage, Modules.override(module).with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(CursorFactory.class).toInstance(cursorFactory);
                        }
                    }));
                }));
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void data() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cuyrsor resource
        AtomicReference<String> cursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreateCursorRequest();
        cursorRequest.setCursorType(CreateCursorRequest.CursorType.graph);
        given()
                .contentType("application/json")
                .body(cursorRequest)
                .post("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorType"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorType"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(302)
                .contentType("application/json;charset=UTF-8");


        //create cursor page resource
        CreatePageRequest pageRequest = new CreatePageRequest();
        pageRequest.setPageSize(100);
        AtomicReference<String> pageId = new AtomicReference<>();
        given()
                .contentType("application/json")
                .body(pageRequest)
                .post("/fuse/query/1/cursor/"+cursorId.get()+"/page")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        pageId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get page data resource by id
        given()
                .contentType("application/json")
                .get("/fuse/query/1/cursor/"+cursorId.get() +"/page/"+pageId.get() +"/data")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(302)
                .contentType("application/json;charset=UTF-8");

    }
}