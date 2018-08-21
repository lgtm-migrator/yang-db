package com.kayhut.fuse.unipop.process.edge;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Profiling;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.javatuples.Pair;
import org.unipop.process.UniPredicatesStep;
import org.unipop.process.order.Orderable;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.search.DeferredVertexQuery;
import org.unipop.schema.reference.DeferredVertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.stream.Collectors;

public class UniGraphEdgeOtherVertexStep extends MapStep<Edge, Vertex> {
    //region Constructors
    public UniGraphEdgeOtherVertexStep(Traversal.Admin traversal) {
        super(traversal);
    }
    //endregion

    //region MapStep Implementation
    @Override
    protected Vertex map(Traverser.Admin<Edge> traverser) {
        UniEdge uniEdge = (UniEdge)traverser.get();

        if (uniEdge.otherVertex() != null) {
            return uniEdge.otherVertex();
        } else {
            final List<Object> objects = traverser.path().objects();
            if (objects.get(objects.size() - 2) instanceof Vertex) {
                return ElementHelper.areEqual((Vertex) objects.get(objects.size() - 2), traverser.get().outVertex()) ?
                        traverser.get().inVertex() :
                        traverser.get().outVertex();
            }
        }

        return null;
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.PATH);
    }
    //endregion
}