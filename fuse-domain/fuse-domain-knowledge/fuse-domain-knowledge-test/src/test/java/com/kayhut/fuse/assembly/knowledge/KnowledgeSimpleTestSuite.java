package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        //KnowledgeSimpleEntityWithRelationTests.class,
        //KnowledgeSimpleEntityWithFilterTests.class,
        //KnowledgeSimpleCdrWithCypherQueryTests.class,
        KnowledgeSimpleEntityWithFilterE2ETests.class,
        KnowledgeInnerQueryE2ETests.class,
        KnowledgeSimpleEfileWithFilterE2ETests.class,
        KnowledgeSimpleReferenceWithFilterE2ETests.class,
        KnowledgeSimpleRelationWithFilterE2ETests.class,
        KnowledgeSimpleRvalueWithFilterE2ETests.class,
        KnowledgeSimpleInsightWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndEvalueWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndEfileWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndInsightWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndRelationWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndReferenceWithFilterE2ETests.class,
        KnowledgeSimpleRelationAndRvalueWithFilterE2ETests.class,
        KnowledgeSimpleEntityRelationAndRvalueWithFilterE2ETests.class,
        KnowledgeSimpleEntityEvalueAndReferenceWithFilterE2ETests.class,
        KnowledgeSimpleSomeTests.class
})

public class KnowledgeSimpleTestSuite {
    public static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - setup");
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - teardown");
        Setup.cleanup();
    }
}
