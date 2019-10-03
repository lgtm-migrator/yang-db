package com.yangdb.fuse.services.engine2;

/**
 * Created by Roman on 21/06/2017.
 */

import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseRunner;
import com.yangdb.fuse.services.engine2.data.DfsNonRedundantEntityRelationEntityTest;
import com.yangdb.fuse.services.engine2.data.PromiseEdgeTest;
import com.yangdb.fuse.services.engine2.data.SingleEntityTest;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CursorTest.class,
        PageTest.class,
        QueryTest.class,
        SingleEntityTest.class,
        PromiseEdgeTest.class,
        DfsNonRedundantEntityRelationEntityTest.class
})
public class NonRedundantTestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("NonRedundantTestSuite start");
        start = System.currentTimeMillis();

        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("Dragons");

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine2.dev.conf").toString()),
                        "m1.dfs.non_redundant");

        app.start("server.join=false");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("NonRedundantTestSuite elapsed: " + elapsed);

    }

    //region Fields
    private static long start;
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}
