package com.kayhut.fuse.asg.strategy.type;

import com.kayhut.fuse.asg.validation.AsgQueryValidator;
import com.kayhut.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.RelPattern;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.Tagged.tagSeq;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * test Rel pattern asg strategy:
 * Expand given relation range pattern into an Or quant with all premutations of requested path length
 * <p>
 * (:E)-[:R | 1..3]->(:E) would be transformed into:
 * <p>
 * (:E)-Quant[OR]
 * -[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)-[:R]->(:E)
 */
public class RelationRangeAsgStrategyTest {
    static Ontology ontology;
    static AsgQueryValidator queryValidator;

    @BeforeClass
    public static void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyShort();
        queryValidator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        });
    }

    @Test
    public void testUntypedToTypedStrategyWithoutQuantsInPath() {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(relPattern(2, OntologyTestUtils.OWN.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(endPattern(new EUntyped(3,tagSeq("end"),0,-1)))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(3, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(7, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(11, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelProp.class).size());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).size());

        Assert.assertEquals(0, AsgQueryUtil.elements(query, EProp.class).size());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, EPropGroup.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));

    }

    @Test
    public void testUntypedToTypedWithPropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(endPattern(new ETyped(3,tagSeq("end"), OntologyTestUtils.PERSON.type,0,-1)))
                .next(eProp(4, OntologyTestUtils.FIRST_NAME.type, of(eq, "abc")))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());

        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(4, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(8, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(12, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelProp.class).size());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).size());

        Assert.assertEquals(3, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, EPropGroup.class).size());


        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testUntypedToTypedWithSomePropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type, "source"))
                .next(quant1(2, all))
                .in(
                        eProp(4, OntologyTestUtils.FIRST_NAME.type, of(eq, "abc")),
                        relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                                .next(
                                        endPattern(new ETyped(3,tagSeq("end"), OntologyTestUtils.PERSON.type,0))
                                                .addNext(eProp(4, OntologyTestUtils.LAST_NAME.type, of(eq, "abc")))))

                .build();
        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(3, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(9, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));


        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelProp.class).size());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelPropGroup.class).size());

        Assert.assertEquals(1, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getpType().equals(OntologyTestUtils.FIRST_NAME.type)).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getpType().equals(OntologyTestUtils.LAST_NAME.type)).count());

        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testUntypedToTypedWithQuantPropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(endPattern(new ETyped(3, tagSeq("end"),OntologyTestUtils.PERSON.type,0)))
                .next(quant1(4, all))
                .in(ePropGroup(5,
                        EProp.of(5, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(5, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(9, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(13, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());

        Assert.assertEquals(3, AsgQueryUtil.elements(query, EPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().size() == 1).count());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, EProp.class).size());

        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));

    }

    @Test
    public void testUntypedToTypedWithQuantPropsAfterRelStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1,"P",Collections.singletonList(OntologyTestUtils.PERSON.type)))
                .next(relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(quant1(3, all))
                .in(relPropGroup(11, all, RelProp.of(11, END_DATE.type, of(eq, new Date()))),
                        endPattern(new ETyped(4, tagSeq("end"),OntologyTestUtils.PERSON.type,0))
                                .next(quant1(5, all)
                                        .addNext(ePropGroup(12, EProp.of(12, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                                )
                )
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(7, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(12, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(17, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().get(0).getpType().equals(START_DATE.type)).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().get(0).getpType().equals(END_DATE.type)).count());

        Assert.assertEquals(3, AsgQueryUtil.elements(query, EPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().size() == 1).count());

        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testUntypedToTypedWithPropsStrategyWithQuantsInPath() {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1,"P", Collections.singletonList(OntologyTestUtils.PERSON.type)))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(3, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                .next(relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(endPattern(new ETyped(3,tagSeq("end"), OntologyTestUtils.PERSON.type,0)))
                .next(eProp(4, OntologyTestUtils.NAME.type, of(eq, "abc")))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==3).count());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e->e.getNext().size()==1).count());

        Assert.assertEquals(4, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(8, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(12, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).size());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelProp.class).size());

        Assert.assertEquals(3, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, EPropGroup.class).size());

        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
        System.out.println(AsgQueryDescriptor.print(query));

    }


}