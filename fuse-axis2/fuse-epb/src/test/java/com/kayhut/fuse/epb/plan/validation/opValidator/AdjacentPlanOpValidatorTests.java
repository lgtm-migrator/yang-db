package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Roman on 26/04/2017.
 */
public class AdjacentPlanOpValidatorTests {
    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7_entity8() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7_filter11_entity8() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity8_rel7_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity8_rel7_filter11_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testInvalidPlan_entity1_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity3_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity6_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter10() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter9_filter10() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new AdjacentPlanOpValidator()));

    //endregion
}
