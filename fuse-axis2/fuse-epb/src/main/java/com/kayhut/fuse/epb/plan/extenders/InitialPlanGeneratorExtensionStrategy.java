package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public InitialPlanGeneratorExtensionStrategy() {}

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        if(!plan.isPresent()) {
            List<AsgEBase<EEntityBase>> entitySeeds = AsgQueryUtil.nextDescendants(query.getStart(), e -> e.geteBase() instanceof EEntityBase, p -> true);
            entitySeeds.forEach(entity-> {
                Optional<AsgEBase<EPropGroup>> epropGroup = AsgQueryUtil.nextAdjacentDescendant(entity, EPropGroup.class, 2);
                EntityOp op = new EntityOp(entity);
                Plan newPlan = new Plan(Collections.singletonList(op));
                if(epropGroup.isPresent()) {
                    newPlan = Plan.compose(newPlan,new EntityFilterOp(epropGroup.get()));
                    newPlan.log("InitialPlanGeneratorExtensionStrategy:[empty->"+newPlan.getOps().size()+"]", Level.INFO);
                }
                plans.add(newPlan);
            });
        }

        return plans;
    }

}
