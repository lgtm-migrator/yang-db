package com.yangdb.fuse.executor.cursor;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorContext implements CursorFactory.Context {
    //region Constructor
    public TraversalCursorContext(
            Ontology ontology,
            QueryResource queryResource,
            CreateCursorRequest cursorRequest,
            Traversal<?, Path> traversal) {
        this.ontology = ontology;
        this.queryResource = queryResource;
        this.cursorRequest = cursorRequest;
        this.traversal = traversal;
    }
    //endregion

    //region CursorFactory.Context Implementation
    @Override
    public QueryResource getQueryResource() {
        return this.queryResource;
    }

    @Override
    public CreateCursorRequest getCursorRequest() {
        return this.cursorRequest;
    }
    //endregion

    //region Properties
    public Traversal<?, Path> getTraversal() {
        return this.traversal;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public void setQueryResource(QueryResource queryResource) {
        this.queryResource = queryResource;
    }

    public void setCursorRequest(CreateCursorRequest cursorRequest) {
        this.cursorRequest = cursorRequest;
    }

    public void setTraversal(Traversal<?, Path> traversal) {
        this.traversal = traversal;
    }

//endregion


    @Override
    public TraversalCursorContext clone()  {
        return new TraversalCursorContext(ontology,queryResource,cursorRequest,traversal);
    }

    //region Fields
    private Ontology ontology;
    private QueryResource queryResource;
    private CreateCursorRequest cursorRequest;
    private Traversal<?, Path> traversal;
    //endregion
}