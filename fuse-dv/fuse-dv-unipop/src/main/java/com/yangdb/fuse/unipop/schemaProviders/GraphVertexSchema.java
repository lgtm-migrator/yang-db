package com.yangdb.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

/**
 * Created by roman on 1/16/2015.
 */
public interface GraphVertexSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Vertex.class;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphElementSchema.Impl implements GraphVertexSchema {
        //region Constructors
        public Impl(String label ) {
            super(label);
        }

        public Impl(String label, GraphElementRouting routing) {
            super(label, routing);
        }

        public Impl(String label, IndexPartitions indexPartitions) {
            super(label, indexPartitions);
        }

        public Impl(String label, GraphElementRouting routing, IndexPartitions indexPartitions) {
            super(label, routing, indexPartitions);
        }

        public Impl(String label, IndexPartitions indexPartitions, Iterable<GraphElementPropertySchema> properties) {
            super(label, indexPartitions, properties);
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            super(label, constraint, routing, indexPartitions, properties);
        }
        //endregion
    }
}
