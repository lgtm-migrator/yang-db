package com.kayhut.fuse.unipop.controller.discrete;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.*;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.appender.DualEdgeDirectionSearchAppender;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.unipop.process.start.UniGraphStartCountStep;
import org.unipop.query.aggregation.ReduceEdgeQuery;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Roman on 3/14/2018.
 */
public class DiscreteElementReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public DiscreteElementReduceController(
            Client client,
            ElasticGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        SearchBuilder searchBuilder = new SearchBuilder();
        if(reduceQuery instanceof ReduceEdgeQuery){
            buildEdgeQuery((ReduceEdgeQuery) reduceQuery, searchBuilder);
        }else{
            buildVertexQuery(reduceQuery, searchBuilder);
        }

        SearchRequestBuilder searchRequest = searchBuilder.build(client, false);
        SearchResponse response = searchRequest.execute().actionGet();
        return response.getHits().getTotalHits();

    }

    private void buildEdgeQuery(ReduceEdgeQuery reduceQuery, SearchBuilder searchBuilder) {
        Iterable<String> edgeLabels = getRequestedEdgeLabels(reduceQuery.getPredicates().getPredicates());

        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }


        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        if (!Stream.ofAll(edgeLabels).isEmpty()) {
            constraint = constraint.isPresent() ?
                    Optional.of(Constraint.by(__.and(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList())), constraint.get().getTraversal()))) :
                    Optional.of(Constraint.by(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList()))));
        }

        List<HasContainer> vertexHasContainer = Stream.ofAll(reduceQuery.getVertexPredicates().getPredicates()).filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if(vertexHasContainer.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        TraversalConstraint vertexTraversalConstraint = (TraversalConstraint)vertexHasContainer.get(0).getValue();
        String vertexLabel = Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(vertexTraversalConstraint.getTraversal(), T.label.getAccessor())).get();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexControllerContext(
                        this.graph,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        1,
                        reduceQuery.getDirection(),
                        Collections.singleton(new DiscreteVertex(1,vertexLabel, graph, new HashMap<>()))));

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new EdgeSourceSearchAppender()),
                        wrap(new EdgeRoutingSearchAppender()),
                        wrap(new EdgeSourceRoutingSearchAppender()),
                        wrap(new EdgeIndexSearchAppender()),
                        wrap(new DualEdgeDirectionSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    private void buildVertexQuery(ReduceQuery reduceQuery, SearchBuilder searchBuilder) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                new DiscreteElementControllerContext(this.graph,
                        ElementType.vertex,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        0),
                null);

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new ElementIndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    protected Iterable<String> getRequestedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                        .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                        .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return requestedEdgeLabels;
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}