package com.kayhut.fuse.assembly.knowledge.asg;

/*-
 * #%L
 * fuse-asg
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

import com.kayhut.fuse.asg.translator.cypher.CypherTranslator;
import com.kayhut.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.kayhut.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;

import java.io.IOException;

public class AsgClauseTransformer implements QueryTransformer<String, Query> {

    //region Constructors
    public AsgClauseTransformer(JsonQueryTranslator translator,BusinessTypesProvider typesProvider) {
        this.translator = translator;
        this.typesProvider = typesProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public Query transform(String query) {

        try {
            return translator.translate(query,typesProvider);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    //endregion

    //region Fields
    private JsonQueryTranslator translator;
    private BusinessTypesProvider typesProvider;
    //endregion
}
