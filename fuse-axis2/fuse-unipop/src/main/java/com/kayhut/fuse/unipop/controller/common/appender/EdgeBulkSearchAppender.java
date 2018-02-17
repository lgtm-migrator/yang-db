package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeBulkSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region VertexControllerContext Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End endSchema = edgeSchema.getDirectionSchema().isPresent() ?
                edgeSchema.getEndA().get() :
                context.getDirection().equals(Direction.OUT) ?
                        edgeSchema.getEndA().get() :
                        edgeSchema.getEndB().get();

        // currently, taking the first id field for query
        // TODO: add support for querying multiple id fields
        String idField = Stream.ofAll(endSchema.getIdFields()).get(0);

        queryBuilder.seekRoot().query().filtered().filter().bool().must()
                .terms(idField,
                        idField,
                        Stream.ofAll(context.getBulkVertices()).map(vertex -> vertex.id().toString()).toJavaList());

        return true;
    }
    //endregion
}
