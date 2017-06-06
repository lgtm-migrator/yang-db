package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.helper.TestUtil;
import org.apache.commons.configuration.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;

/**
 * Created by benishue on 05/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsGeneratorTest.class,
        GuildsGeneratorTest.class,
        HorsesGeneratorTest.class,
        KingdomsGeneratorTest.class,
        PersonsGeneratorTest.class,
        DataGeneratorTest.class,
        RandomUtilTest.class
})
public class TestSuite {

    static String CONFIGURATION_FILE_PATH = "test.generator.properties";


    static Configuration configuration;

    @BeforeClass
    public static void setUp() {
        System.out.println("setting up");
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();

    }

    @AfterClass
    public static void tearDown() throws Exception{
        System.out.println("tearing down");
        String dir = System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath");
        TestUtil.cleanDirectory(new File(dir));
        Thread.sleep(1000);
    }
}