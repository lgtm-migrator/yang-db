package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Before;
import org.junit.Test;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadQuery;
import static org.junit.Assert.assertTrue;

/**
 * Created by Elad on 4/2/2017.
 */
public class AggL1CypherStrategyTest {

    Ontology ontology;
    AsgQuery asgQuery;

    @Before
    public void setUp() throws Exception {
        ontology = loadOntology("dragons.json");
        asgQuery = new RecTwoPassAsgQuerySupplier(loadQuery("Q059.json")).get();
    }

    @Test
    public void apply() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery, ontology);

        assertTrue(cypher.contains("MATCH p0 = (A:Person)-[r1:offspring_of]->(B:Person)\n" +
                "WITH count(A) AS agg1,A,r1,B\n" +
                "WHERE agg1 > 2\n" +
                "RETURN A,r1,agg1,B"));

    }
}