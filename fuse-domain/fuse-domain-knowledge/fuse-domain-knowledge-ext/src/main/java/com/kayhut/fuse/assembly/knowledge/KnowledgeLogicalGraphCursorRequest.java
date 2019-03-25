package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;

/**
 * Created by Roman on 7/7/2018.
 */
public class KnowledgeLogicalGraphCursorRequest extends CreateGraphHierarchyCursorRequest {
    public static final String CursorType = "knowledgeLogicalGraphCursorRequest";

    //region Constructors
    public KnowledgeLogicalGraphCursorRequest() {
        super();
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Iterable<String> countTags) {
        super(countTags);
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(countTags, createPageRequest);
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(include, countTags, createPageRequest);
        this.setCursorType(CursorType);
    }
    //endregion
}