package com.kayhut.fuse.neo4j.test.driver;

import com.kayhut.fuse.neo4j.driver.Neo4jDriver;

import org.junit.Test;
import org.junit.Rule;

import static org.junit.Assert.assertTrue;


/**
 * Created by EladW on 19/02/2017.
 */
public class Neo4JDriverTest {

    @Test
    public void checkDriverConnection() {

        Neo4jDriver driver = new Neo4jDriver();

        String result = driver.query("match (p:PERSON) return p");

        assertTrue(result != null);
    }

}