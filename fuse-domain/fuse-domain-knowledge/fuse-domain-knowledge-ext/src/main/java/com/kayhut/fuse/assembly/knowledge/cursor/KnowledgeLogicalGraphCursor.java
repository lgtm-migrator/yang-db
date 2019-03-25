package com.kayhut.fuse.assembly.knowledge.cursor;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.logical.LogicalEdge;
import com.kayhut.fuse.model.logical.LogicalNode;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.LogicalTypes.*;
import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaFields.*;

public class KnowledgeLogicalGraphCursor extends KnowledgeGraphHierarchyTraversalCursor {

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new KnowledgeLogicalGraphCursor(
                    (TraversalCursorContext) context,
                    ((CreateGraphHierarchyCursorRequest) context.getCursorRequest()).getCountTags());
        }
        //endregion
    }

    public KnowledgeLogicalGraphCursor(TraversalCursorContext context, Iterable<String> countTags) {
        super(context, countTags);
    }


    @Override
    protected Assignment compose(Assignment.Builder builder) {
        Assignment<LogicalNode, LogicalEdge> newAssignment = new Assignment<>();
        Assignment<Entity, Relationship> assignment = builder.build();

        Map<String, LogicalEdge> edgeMap = assignment.getRelationships().stream()
                .filter(r -> r.getrType().equals(RELATED_ENTITY))
                .map(r ->
                        new LogicalEdge(r.getrID(), r.getProperty(CATEGORY).get().getValue().toString(),
                                r.geteID1(), r.geteID2(), r.isDirectional())
                                .withMetadata(r.getProperties())
                )
                .collect(Collectors.toMap(LogicalEdge::getId, p -> p));

        Map<String, LogicalNode> entityMap = assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(ENTITY))
                .map(e ->
                        new LogicalNode(
                                e.getProperty(LOGICAL_ID).orElse(new Property(LOGICAL_ID, e.geteID())).getValue().toString(),
                                e.getProperty(CATEGORY).orElse(new Property(CATEGORY, CATEGORY)).getValue().toString())
                                .withMetadata(e.getProperties())
                )
                .collect(Collectors.toMap(LogicalNode::getId, p -> p));

        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(EVALUE))
                .forEach(p -> {
                    if (p.getProperty(LOGICAL_ID).isPresent() &&
                            entityMap.containsKey(p.getProperty(LOGICAL_ID).get().getValue().toString())) {
                        //populate properties
                        entityMap.get(p.getProperty(LOGICAL_ID).get().getValue().toString())
                                .withProperty(p.getProperty(FIELD_ID).get().getValue().toString(), value(p));

                    }
                });
        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(RVALUE))
                .forEach(p -> {
                    if (p.getProperty(RELATION_ID).isPresent() &&
                            edgeMap.containsKey(p.getProperty(RELATION_ID).get().getValue().toString())) {
                        //populate properties
                        edgeMap.get(p.getProperty(RELATION_ID).get().getValue().toString())
                                .withProperty(p.getProperty(RELATION_ID).get().getValue().toString(), value(p));

                    }
                });


        newAssignment.setEntities(new ArrayList<>(entityMap.values()));
        newAssignment.setRelationships(new ArrayList<>(edgeMap.values()));
        return newAssignment;
    }

    private Object value(Entity entity) {
        if ((entity.getProperty(STRING_VALUE).isPresent() && entity.getProperty(STRING_VALUE).get().getValue() != null))
            return entity.getProperty(STRING_VALUE).get().getValue();
        if ((entity.getProperty(INT_VALUE).isPresent() && entity.getProperty(INT_VALUE).get().getValue() != null))
            return entity.getProperty(INT_VALUE).get().getValue();
        if ((entity.getProperty(DATE_VALUE).isPresent() && entity.getProperty(DATE_VALUE).get().getValue() != null))
            return entity.getProperty(DATE_VALUE).get().getValue();
        if ((entity.getProperty(LONG_VALUE).isPresent() && entity.getProperty(LONG_VALUE).get().getValue() != null))
            return entity.getProperty(LONG_VALUE).get().getValue();
        if ((entity.getProperty(FLOAT_VALUE).isPresent() && entity.getProperty(FLOAT_VALUE).get().getValue() != null))
            return entity.getProperty(FLOAT_VALUE).get().getValue();
        if ((entity.getProperty(GEO_VALUE).isPresent() && entity.getProperty(GEO_VALUE).get().getValue() != null))
            return entity.getProperty(GEO_VALUE).get().getValue();
        return null;
    }
}