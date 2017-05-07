package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Roman on 03/05/2017.
 */
public class ReverseRelationOpValidatorTests {
    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()))
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Invalid Plan
    @Test
    public void testInvalidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()))
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Private Methods
    private AsgEBase<Rel> reverseRelation(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new ReverseRelationOpValidator()));

    //endregion
}
