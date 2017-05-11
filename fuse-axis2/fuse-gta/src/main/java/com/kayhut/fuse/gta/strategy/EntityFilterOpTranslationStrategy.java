package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context) {
        if (!(planOp instanceof EntityFilterOp)) {
            return traversal;
        }

        EntityFilterOp entityFilterOp = (EntityFilterOp)planOp;
        Optional<PlanOpBase> previousPlanOp = PlanUtil.getAdjacentPrev(context.getPlan(), entityFilterOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }

        if (HasStep.class.isAssignableFrom(traversal.asAdmin().getEndStep().getClass())) {
            traversal.asAdmin().removeStep(traversal.asAdmin().getSteps().indexOf(traversal.asAdmin().getEndStep()));
        }

        EntityOp entityOp = (EntityOp)previousPlanOp.get();
        if (PlanUtil.isFirst(context.getPlan(), entityOp)) {
            traversal = appendEntityAndPropertyGroup(
                    traversal,
                    entityOp.getAsgEBase().geteBase(),
                    entityFilterOp.getAsgEBase().geteBase(),
                    context.getOntology());

        } else if (!entityFilterOp.getAsgEBase().geteBase().geteProps().isEmpty()) {

            traversal = appendPropertyGroup(
                    traversal,
                    entityFilterOp.getAsgEBase().geteBase(),
                    context.getOntology());
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendEntityAndPropertyGroup(
            GraphTraversal traversal,
            EEntityBase entity,
            EPropGroup ePropGroup,
            Ontology ontology) {

        if (entity instanceof EConcrete) {
            traversal.has(GlobalConstants.HasKeys.PROMISE, P.eq(Promise.as(((EConcrete) entity).geteID())));
        }
        else if (entity instanceof ETyped) {
            String eTypeName = OntologyUtil.getEntityTypeNameById(ontology,((ETyped) entity).geteType());
            Traversal constraintTraversal = __.has(T.label, P.eq(eTypeName));
            List<Traversal> epropTraversals =
                    Stream.ofAll(ePropGroup.geteProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ontology)).toJavaList();

            if (!epropTraversals.isEmpty()) {
                epropTraversals.add(0, constraintTraversal);
                constraintTraversal = __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));
            }

            traversal.has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal));
        }
        else if (entity instanceof EUntyped) {
            ;
        }

        return traversal;
    }

    private GraphTraversal appendPropertyGroup(
            GraphTraversal traversal,
            EPropGroup ePropGroup,
            Ontology ontology) {

        List<Traversal> epropTraversals =
                Stream.ofAll(ePropGroup.geteProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ontology)).toJavaList();

        Traversal constraintTraversal = epropTraversals.size() == 1 ?
                epropTraversals.get(0) :
                __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));

        return traversal.outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal));
    }

    private Traversal convertEPropToTraversal(EProp eProp, Ontology ontology) {
         Optional<Property> property = OntologyUtil.getProperty(ontology, Integer.parseInt(eProp.getpType()));
         if (!property.isPresent()) {
             return __.start();
         }

         return __.has(property.get().getName(), ConverstionUtil.convertConstraint(eProp.getCon()));
    }
    //endregion
}
