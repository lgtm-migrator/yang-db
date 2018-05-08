package com.kayhut.fuse.assembly.knowlegde.asg;

import com.kayhut.fuse.asg.AsgQueryTransformer;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.assembly.knowlegde.KnowledgeM2AsgStrategyRegistrar;
import com.kayhut.fuse.assembly.knowlegde.KnowledgeRuleBoostProvider;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.ScoreEProp;
import com.kayhut.fuse.model.query.properties.ScoreEPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;

public class KnowledgeM2AsgStrategyTest {
    //region Setup
    @BeforeClass
    public static void setup() {
        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withEntityTypes(Collections.singletonList(
                        EntityType.Builder.get().withEType("Person").withName("Person").withProperties(
                                Collections.singletonList("stringValue")).build()))
                .withProperties(Collections.singletonList(
                        Property.Builder.get().withPType("stringValue").withName("stringValue").withType("string").build()))
                .build();

        RuleBoostProvider boostProvider = new KnowledgeRuleBoostProvider();

        OntologyProvider ontologyProvider = new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        };

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Collections.singletonList(
                        new GraphVertexSchema.Impl(
                                "Person",
                                new GraphElementConstraint.Impl(__.start()),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.singletonList(
                                        new GraphElementPropertySchema.Impl("stringValue", "string", Arrays.asList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("stringValue.keyword")
                                                //new GraphElementPropertySchema.NgramsIndexingSchema.Impl("stringValue.ngrams", 10)
                                        ))
                                )
                        )
                ),
                Collections.emptyList());

        GraphElementSchemaProviderFactory schemaProviderFactory = ontology1 -> schemaProvider;

        asgStrategy = new AsgQueryTransformer(new KnowledgeM2AsgStrategyRegistrar( ontologyProvider,schemaProviderFactory ,boostProvider),ontologyProvider);
    }
    //endregion

    //region Tests
    @Test
    public void testLikeWithoutAsterisks() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.like, "Sherley mustafa"))))
                .build();

        asgStrategy.transform(asgQuery);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(actual.getQuantType(),all);

        //1 root group for with 'some' operator {title/nickname}
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: nicknames
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //another wrapper group for the 4 conditions
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().size(), 1);
        //4 groups - one per ranking condition
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().size(), 4);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getQuantType(), some);

        //first condition (eq exact match = "Sherley mustafa")
        EPropGroup rule1Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(rule1Group.getQuantType(), all);
        Assert.assertEquals(rule1Group.getProps().size(), 1);

        //not expecting inner groups for eq ranking condition
        Assert.assertEquals(rule1Group.getGroups().size(), 0);
        Assert.assertEquals(rule1Group.getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(rule1Group.getProps().get(0).getCon().getOp(), eq);
        Assert.assertTrue(rule1Group.getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) rule1Group.getProps().get(0)).getBoost(), 1000000);

        //second condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule2Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(1);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule2Group.getQuantType(), all);
        Assert.assertEquals(rule2Group.getGroups().size(), 2);

        Assert.assertTrue(rule2Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule2Group.getGroups().get(1)).getBoost(), 10000);
        // expecting 3 stringValue filters on expression:
        // 1) * Sherley mustafa
        // 2) Sherley mustafa *
        // 2) * Sherley mustafa *
        Assert.assertEquals(rule2Group.getGroups().get(0).getProps().size(), 3);
        Assert.assertEquals(rule2Group.getGroups().get(0).getQuantType(), some);
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith(" *"));
        //expecting boosting group with terms
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule3Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(2);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule3Group.getQuantType(), all);
        Assert.assertEquals(rule3Group.getGroups().size(), 2);
        // expecting 3 stringValue filters on expression:
        // 1) *Sherley
        // 2) Sherley*
        // 2) *Sherley*
        Assert.assertEquals(rule3Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertEquals(rule3Group.getGroups().get(0).getQuantType(), some);

        Assert.assertTrue(rule3Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule3Group.getGroups().get(1)).getBoost(), 100);

        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));
        // 1) *mustafa
        // 2) mustafa*
        // 2) *mustafa*
        //expecting boosting group with terms
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule4Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(3);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule4Group.getQuantType(), all);
        Assert.assertEquals(rule4Group.getGroups().size(), 2);
        // expecting 3 stringValue filters on expression (:
        // 1) *Sherley
        // 2) Sherley*
        // 2) *Sherley*
        Assert.assertEquals(rule4Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertEquals(rule4Group.getGroups().get(0).getQuantType(), some);

        Assert.assertTrue(rule4Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule4Group.getGroups().get(1)).getBoost(), 1);

        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        // expecting 3 stringValue filters on expression:
        // 1) *mustafa
        // 2) mustafa*
        // 2) *mustafa*
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));

        //expecting boosting group with terms
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));
    }

    @Test
    public void testLikeWithAsterisksInMiddle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.like, "Sherley* mustafa"))))
                .build();

        asgStrategy.transform(asgQuery);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getQuantType(),all);
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        //1 root group for with 'some' operator
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: title
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //another wrapper group for the 4 conditions
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().size(), 1);
        //4 groups - one per ranking condition
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().size(), 4);

        //first condition (eq exact match = "Sherley mustafa")
        EPropGroup rule1Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(rule1Group.getQuantType(), all);
        Assert.assertEquals(rule1Group.getProps().size(), 1);

        //not expecting inner groups for eq ranking condition
        Assert.assertEquals(rule1Group.getGroups().size(), 0);
        Assert.assertEquals(rule1Group.getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(rule1Group.getProps().get(0).getCon().getOp(), eq);
        Assert.assertTrue(rule1Group.getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) rule1Group.getProps().get(0)).getBoost(), 1000000);

        //second condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule2Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(1);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule2Group.getQuantType(), all);
        Assert.assertEquals(rule2Group.getGroups().size(), 2);
        // expecting 6 stringValue filters on expression:
        // 1) * Sherley
        // 2) Sherley *
        // 2) * Sherley *
        // 1) *  mustafa
        // 2) mustafa *
        // 2) * mustafa *
        Assert.assertEquals(rule2Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule2Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule2Group.getGroups().get(1)).getBoost(), 10000);

        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith(" *"));
        //expecting boosting group with terms
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule3Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(2);
        Assert.assertEquals(rule3Group.getQuantType(), all);
        Assert.assertEquals(rule3Group.getGroups().size(), 2);

        Assert.assertTrue(rule3Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule3Group.getGroups().get(1)).getBoost(), 100);
        // expecting 3 stringValue filters on expression:
        // 1) *Sherley
        // 2) Sherley*
        // 3) *Sherley*
        // 4) *mustafa
        // 5) mustafa*
        // 6) *mustafa*
        Assert.assertEquals(rule3Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule3Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule3Group.getGroups().get(1)).getBoost(),100);

        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));
        //expecting boosting group with terms
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule4Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(3);
        Assert.assertEquals(rule4Group.getQuantType(), all);
        Assert.assertEquals(rule4Group.getGroups().size(), 2);
        Assert.assertTrue(rule4Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule4Group.getGroups().get(1)).getBoost(), 1);
        // expecting 3 stringValue filters on expression (:
        // 1) *Sherley
        // 2) Sherley*
        // 2) *Sherley*
        Assert.assertEquals(rule4Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule4Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule4Group.getGroups().get(1)).getBoost(),1);
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        // expecting 3 stringValue filters on expression:
        // 1) *mustafa
        // 2) mustafa*
        // 2) *mustafa*
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));

        //expecting boosting group with terms
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));
    }

    @Test
    public void testLikeWithAsterisksAllAround() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.like, "*Sherley*mustafa*"))))
                .build();

        asgStrategy.transform(asgQuery);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertEquals(actual.getQuantType(),all);
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        //1 root group for with 'some' operator
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: title
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //another wrapper group for the 4 conditions
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().size(), 1);
        //4 groups - one per ranking condition
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().size(), 4);

        //first condition (eq exact match = "Sherley mustafa")
        EPropGroup rule1Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(rule1Group.getQuantType(), all);
        Assert.assertEquals(rule1Group.getProps().size(), 1);

        //not expecting inner groups for eq ranking condition
        Assert.assertEquals(rule1Group.getGroups().size(), 0);
        Assert.assertEquals(rule1Group.getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(rule1Group.getProps().get(0).getCon().getOp(), eq);
        Assert.assertTrue(rule1Group.getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) rule1Group.getProps().get(0)).getBoost(), 1000000);

        //second condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule2Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(1);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule2Group.getQuantType(), all);
        Assert.assertEquals(rule2Group.getGroups().size(), 2);
        Assert.assertTrue(rule2Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule2Group.getGroups().get(1)).getBoost(), 10000);
        // expecting 6 stringValue filters on expression:
        // 1) * Sherley
        // 2) Sherley *
        // 2) * Sherley *
        // 1) *  mustafa
        // 2) mustafa *
        // 2) * mustafa *
        Assert.assertEquals(rule2Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith(" *"));
        //expecting boosting group with terms
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule3Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(2);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule3Group.getQuantType(), all);
        Assert.assertEquals(rule3Group.getGroups().size(), 2);
        Assert.assertTrue(rule3Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule3Group.getGroups().get(1)).getBoost(), 100);
        // expecting 3 stringValue filters on expression:
        // 1) *Sherley
        // 2) Sherley*
        // 3) *Sherley*
        // 4) *mustafa
        // 5) mustafa*
        // 6) *mustafa*
        Assert.assertEquals(rule3Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));
        //expecting boosting group with terms
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule4Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(3);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule4Group.getQuantType(), all);
        Assert.assertEquals(rule4Group.getGroups().size(), 2);
        Assert.assertTrue(rule4Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule4Group.getGroups().get(1)).getBoost(), 1);
        // expecting 3 stringValue filters on expression (:
        // 1) *Sherley
        // 2) Sherley*
        // 2) *Sherley*
        Assert.assertEquals(rule4Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        // expecting 3 stringValue filters on expression:
        // 1) *mustafa
        // 2) mustafa*
        // 2) *mustafa*
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));

        //expecting boosting group with terms
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));
    }

    @Test
    public void testLikeWithAsterisksInSides() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.like, "*Sherley mustafa*"))))
                .build();

        asgStrategy.transform(asgQuery);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertEquals(actual.getQuantType(),all);
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        //1 root group for with 'some' operator
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: title
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //another wrapper group for the 4 conditions
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().size(), 1);
        //4 groups - one per ranking condition
        Assert.assertEquals(actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().size(), 4);

        //first condition (eq exact match = "Sherley mustafa")
        EPropGroup rule1Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(rule1Group.getQuantType(), all);
        Assert.assertEquals(rule1Group.getProps().size(), 1);

        //not expecting inner groups for eq ranking condition
        Assert.assertEquals(rule1Group.getGroups().size(), 0);
        Assert.assertEquals(rule1Group.getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(rule1Group.getProps().get(0).getCon().getOp(), eq);
        Assert.assertTrue(rule1Group.getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) rule1Group.getProps().get(0)).getBoost(), 1000000);

        //second condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule2Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(1);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule2Group.getQuantType(), all);
        Assert.assertEquals(rule2Group.getGroups().size(), 2);
        Assert.assertTrue(rule2Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule2Group.getGroups().get(1)).getBoost(), 10000);

        // expecting 3 stringValue filters on expression:
        // 1) * Sherley mustafa
        // 2) Sherley mustafa *
        // 2) * Sherley mustafa *
        Assert.assertEquals(rule2Group.getGroups().get(0).getProps().size(), 3);
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith(" *"));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("* "));
        Assert.assertTrue(rule2Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith(" *"));
        //expecting boosting group with terms
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule2Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule2Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule3Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(2);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule3Group.getQuantType(), all);
        Assert.assertEquals(rule3Group.getGroups().size(), 2);
        Assert.assertTrue(rule3Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule3Group.getGroups().get(1)).getBoost(), 100);
        // expecting 3 stringValue filters on expression:
        // 1) * Sherley
        // 2) Sherley  *
        // 2) * Sherley *
        Assert.assertEquals(rule3Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule3Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));
        //expecting boosting group with terms
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule3Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule3Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));

        //third condition - expecting 2 inner groups - one for boosting and one for filter with asterisks rules
        EPropGroup rule4Group = actual.getGroups().get(0).getGroups().get(0).getGroups().get(0).getGroups().get(3);
        //filter & boosting must be in and condition
        Assert.assertEquals(rule4Group.getQuantType(), all);
        Assert.assertEquals(rule4Group.getGroups().size(), 2);

        Assert.assertTrue(rule4Group.getGroups().get(1) instanceof ScoreEPropGroup);
        Assert.assertEquals(((ScoreEPropGroup) rule4Group.getGroups().get(1)).getBoost(), 1);
        // expecting 3 stringValue filters on expression (:
        // 1) *Sherley
        // 2) Sherley*
        // 2) *Sherley*
        Assert.assertEquals(rule4Group.getGroups().get(0).getProps().size(), 6);
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(0).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(1).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(2).getCon().getExpr().toString().endsWith("*"));
        // expecting 3 stringValue filters on expression:
        // 1) *mustafa
        // 2) mustafa*
        // 2) *mustafa*
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(3).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(4).getCon().getExpr().toString().endsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().startsWith("*"));
        Assert.assertTrue(rule4Group.getGroups().get(0).getProps().get(5).getCon().getExpr().toString().endsWith("*"));

        //expecting boosting group with terms
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().size(), 1);
        //terms Sherley, mustafa
        Assert.assertEquals(rule4Group.getGroups().get(1).getProps().get(0).getCon().getOp(), inSet);
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("Sherley"));
        Assert.assertTrue(((Set) rule4Group.getGroups().get(1).getProps().get(0).getCon().getExpr()).contains("mustafa"));
    }

    @Test
    public void testEqual() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.eq, "Sherley mustafa"))))
                .build();

        asgStrategy.transform(asgQuery);


        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertEquals(actual.getQuantType(),all);
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        //1 root group for with 'some' operator
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: nicknames
        EPropGroup nicknameRule = actual.getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(nicknameRule.getProps().size(), 1);
        Assert.assertEquals(nicknameRule.getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(nicknameRule.getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(nicknameRule.getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //equal rule with score
        Assert.assertEquals(nicknameRule.getGroups().size(), 1);
        Assert.assertEquals(nicknameRule.getGroups().get(0).getQuantType(), all);
        Assert.assertEquals(nicknameRule.getGroups().get(0).getProps().size(), 1);
        Assert.assertTrue(nicknameRule.getGroups().get(0).getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) nicknameRule.getGroups().get(0).getProps().get(0)).getBoost(), 1000000);
        Assert.assertEquals(nicknameRule.getGroups().get(0).getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(nicknameRule.getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "Sherley mustafa");

        // second field id: title
        EPropGroup titleRule = actual.getGroups().get(0).getGroups().get(1);
        Assert.assertEquals(titleRule.getProps().size(), 1);
        Assert.assertEquals(titleRule.getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(titleRule.getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(titleRule.getProps().get(0).getCon().getExpr().toString(), "title");
        //equal rule with score
        Assert.assertEquals(titleRule.getGroups().size(), 1);
        Assert.assertEquals(titleRule.getGroups().get(0).getQuantType(), all);
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().size(), 1);
        Assert.assertTrue(titleRule.getGroups().get(0).getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) titleRule.getGroups().get(0).getProps().get(0)).getBoost(), 2000000);
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "Sherley mustafa");


    }

    @Test
    @Ignore("skipped:determine what to do with * in 'eq' statement ")
    public void testEqualWithAstersiks() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(31, "fieldId", Constraint.of(ConstraintOp.eq, new String[]{ "nicknames"})),
                        EProp.of(32, "stringValue", Constraint.of(ConstraintOp.eq, "Sherley* mustafa"))))
                .build();

        asgStrategy.transform(asgQuery);


        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        //verify original field filter was added with ranking eProp groups
        Assert.assertEquals(actual.getProps().size(), 2);
        Assert.assertEquals(actual.getProps().get(0).getpType(), "fieldId");
        //1 root group for with 'some' operator
        Assert.assertEquals(actual.getGroups().size(), 1);
        Assert.assertEquals(actual.getGroups().get(0).getQuantType(), some);
        //2 sub groups for each fieldId
        Assert.assertEquals(actual.getGroups().get(0).getGroups().size(), 2);

        // first field id: title
        EPropGroup titleRule = actual.getGroups().get(0).getGroups().get(0);
        Assert.assertEquals(titleRule.getProps().size(), 1);
        Assert.assertEquals(titleRule.getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(titleRule.getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(titleRule.getProps().get(0).getCon().getExpr().toString(), "title");
        //equal rule with score
        Assert.assertEquals(titleRule.getGroups().size(), 1);
        Assert.assertEquals(titleRule.getGroups().get(0).getQuantType(), all);
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().size(), 1);
        Assert.assertTrue(titleRule.getGroups().get(0).getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) titleRule.getGroups().get(0).getProps().get(0)).getBoost(), 100);
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(titleRule.getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "Sherley mustafa");

        // second field id: nicknames
        EPropGroup nickNamesRule = actual.getGroups().get(0).getGroups().get(1);
        Assert.assertEquals(nickNamesRule.getProps().size(), 1);
        Assert.assertEquals(nickNamesRule.getProps().get(0).getpType(), "fieldId");
        Assert.assertEquals(nickNamesRule.getProps().get(0).getCon().getOp(), eq);
        Assert.assertEquals(nickNamesRule.getProps().get(0).getCon().getExpr().toString(), "nicknames");
        //equal rule with score
        Assert.assertEquals(nickNamesRule.getGroups().size(), 1);
        Assert.assertEquals(nickNamesRule.getGroups().get(0).getQuantType(), all);
        Assert.assertEquals(nickNamesRule.getGroups().get(0).getProps().size(), 1);
        Assert.assertTrue(nickNamesRule.getGroups().get(0).getProps().get(0) instanceof ScoreEProp);
        Assert.assertEquals(((ScoreEProp) nickNamesRule.getGroups().get(0).getProps().get(0)).getBoost(), 100);
        Assert.assertEquals(nickNamesRule.getGroups().get(0).getProps().get(0).getpType(), "stringValue");
        Assert.assertEquals(nickNamesRule.getGroups().get(0).getProps().get(0).getCon().getExpr().toString(), "Sherley mustafa");


    }
    //endregion

    //region Fields
    private static AsgQueryTransformer asgStrategy;
    //endregion
}