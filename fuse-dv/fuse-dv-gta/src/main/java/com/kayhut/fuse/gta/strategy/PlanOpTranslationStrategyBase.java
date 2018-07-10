package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.Predicate;

/**
 * Created by Roman on 24/05/2017.
 */
public abstract class PlanOpTranslationStrategyBase implements PlanOpTranslationStrategy {
    //region Constructors
    @SafeVarargs
    public PlanOpTranslationStrategyBase(Class<? extends PlanOp>...klasses) {
        this.klasses = klasses;
    }

    public PlanOpTranslationStrategyBase(Predicate<PlanOp> planOpPredicate) {
        this.planOpPredicate = planOpPredicate;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        if (this.planOpPredicate != null) {
            if (!this.planOpPredicate.test(planOp)) {
                return traversal;
            }
        }
        else if (Stream.of(klasses).filter(klass -> klass.isAssignableFrom(planOp.getClass())).isEmpty()) {
            return traversal;
        }

        return translateImpl(traversal, plan, planOp, context);
    }
    //endregion

    //region Abstract Methods
    protected abstract GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context);
    //endregion

    //region Fields
    private Class<? extends PlanOp>[] klasses;
    private Predicate<PlanOp> planOpPredicate;
    //endregion
}