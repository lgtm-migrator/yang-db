package com.kayhut.fuse.gta.strategy.promise;

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    private static class EntityOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityOpStrategies() {
            super(
                    new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                    new SelectionTranslationStrategy(EntityOp.class)
            );
        }
    }

    private static class EntityFilterOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityFilterOpStrategies() {
            super(
                    new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                    new SelectionTranslationStrategy(EntityFilterOp.class)
            );
        }
    }

    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(new EntityOpStrategies(),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpStrategies(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}