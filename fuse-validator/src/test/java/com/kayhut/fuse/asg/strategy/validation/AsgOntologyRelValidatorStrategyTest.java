package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgOntologyRelValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, OntologyTestUtils.PERSON.type))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidParentEntityWithQuantUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(GENDER.type, 3, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(eProp(11,
                                                EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }
    @Test
    public void testNotValidDirectionParentEntityWithQuantUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(GENDER.type, 3, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.L).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(eProp(11,
                                                EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));
    }

    @Test
    public void testValidParentEntityWithQuantNonValidUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(GENDER.type, 3, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(eProp(11,
                                                EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testValidParentEntityWithQuantReverseQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, DRAGON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(NAME.type, 3, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.L).below(relProp(9)).
                                next(typed(10, DRAGON.type)
                                        .next(eProp(11, EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(NAME.type, 7, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testValidParentEntityWithQuantQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, DRAGON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(NAME.type, 3, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, DRAGON.type)
                                        .next(eProp(11, EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(NAME.type, 7, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testNotValidNoParentEntityWithQuantQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(NAME.type, 3, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, DRAGON.type).next(eProp(11, EProp.of(NAME.type, 11, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(NAME.type, 7, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyRelValidatorStrategy.ERROR_1));
    }

    @Test
    public void testValidTypeQuery() {
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testValidUntypedOneSideQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testValidUntypedTowSidesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(unTyped(3))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testNotValidRelWithOneSideMissingQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyRelValidatorStrategy.ERROR_1));

    }

    @Test
    public void testNotValidRelWithOneSideNotAllowedByRelTypesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OntologyTestUtils.FIRE.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
                .build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));

    }

    @Test
    public void testNotValidRelWithTwoSidesNotAllowedByRelTypesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OntologyTestUtils.FIRE.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
                .build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));

    }

}
