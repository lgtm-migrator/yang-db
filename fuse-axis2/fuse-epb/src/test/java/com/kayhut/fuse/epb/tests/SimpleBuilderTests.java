package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyPlanCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleBuilderTests {

    @Test
    public void TestInitialCreationSingleEntity(){
        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        InitialPlanGeneratorExtensionStrategy<CostCalculator.Cost> strategy = new InitialPlanGeneratorExtensionStrategy<CostCalculator.Cost>(new DummyCostEstimator(), new DummyPlanCostEstimator());
        Iterable<Plan<CostCalculator.Cost>> plans = strategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(1,plansList.size());
        Plan<CostCalculator.Cost> plan = plansList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0).getOpBase();
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), query.getRight().geteNum());
        Assert.assertEquals(query.getLeft().getStart().getNext().get(0), ((EntityOp)op).getEntity());
    }

    @Test
    public void TestInitialCreationMultipleEntities(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        InitialPlanGeneratorExtensionStrategy<CostCalculator.Cost> strategy = new InitialPlanGeneratorExtensionStrategy<CostCalculator.Cost>(new DummyCostEstimator(), new DummyPlanCostEstimator());
        Iterable<Plan<CostCalculator.Cost>> plans = strategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(2,plansList.size());

        AsgEBase<? extends EBase> untypedBaseAsg = query.getRight().getNext().get(0).getNext().get(0);

        boolean foundFirst = false, foundSecond = false;
        for(Plan<CostCalculator.Cost> plan : plans){
            Assert.assertEquals(1,plan.getOps().size());
            PlanOpBase op = plan.getOps().get(0).getOpBase();
            Assert.assertTrue(op instanceof EntityOp);
            if(((EntityOp)op).getEntity().geteBase() == query.getRight().geteBase()){
                foundFirst = true;
                Assert.assertEquals(query.getRight().geteBase().geteNum(), op.geteNum());
            }

            if(((EntityOp)op).getEntity().geteBase() == query.getRight().getNext().get(0).getNext().get(0).geteBase()) {
                foundSecond = true;
                Assert.assertEquals(untypedBaseAsg.geteNum(), op.geteNum());
            }
        }
        Assert.assertTrue(foundFirst && foundSecond);
    }

    @Test
    public void TestCompositePlanStrategyInit(){
        CompositePlanExtensionStrategy<Plan<CostCalculator.Cost>, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(new InitialPlanGeneratorExtensionStrategy<>(new DummyCostEstimator(), new DummyPlanCostEstimator()),
                                                                                                                            new AllDirectionsPlanExtensionStrategy<>(new DummyCostEstimator(), new DummyPlanCostEstimator()));

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        Iterable<Plan<CostCalculator.Cost>> plans = compositePlanExtensionStrategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan<CostCalculator.Cost>> planList = new LinkedList<>();
        plans.forEach(p -> planList.add(p));

        Assert.assertEquals(1, planList.size());
        Plan<CostCalculator.Cost> plan = planList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0).getOpBase();
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), query.getRight().geteNum());
        Assert.assertEquals(query.getLeft().getStart().getNext().get(0), ((EntityOp)op).getEntity());
    }


}
