package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.StepAncestorAdjacentStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.Direction.reverse;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAncestorAdjacentStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType(1);

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "A", 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, "B", 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped("C", 6))
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8))
                )
                .build();
    }

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 3)))), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2ReversefullPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));


        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2GotoPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new GoToEntityOp(getAsgEBaseByEnum(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 7);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 11)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtil.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
