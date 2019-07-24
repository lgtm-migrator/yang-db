package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.yangdb.fuse.services.controllers.IdGeneratorController.IDGENERATOR_INDEX;

public abstract class Setup {
    public static final Path path = Paths.get( "resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf");
    public static FuseApp app = null;
    public static ElasticEmbeddedNode elasticEmbeddedNode = null;
    public static KnowledgeConfigManager manager = null;
    public static FuseClient fuseClient = null;
    public static TransportClient client = null;

    public static void setup() throws Exception {
        setup(true);
    }

    public static void setup(boolean embedded) throws Exception {
        setup(embedded,true);
    }

    public static void setup(boolean embedded, boolean init) throws Exception {
        init(embedded,init,true);
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
    }

    public static void setup(boolean embedded, boolean init, boolean startFuse, FuseClient givenFuseClient) throws Exception {
        init(embedded,init,startFuse);
        //set fuse client
        fuseClient = givenFuseClient;
    }

    private static void init(boolean embedded, boolean init, boolean startFuse) throws Exception {
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
            client = elasticEmbeddedNode.getClient();
            try {
                new KnowledgeIdGenerator(client,IDGENERATOR_INDEX).init(Arrays.asList("Entity","Relation","Evalue","Rvalue","workerId"));
            } catch (Exception e) {
                //probably index already exists
                System.out.println(e.getMessage());
            }
        } else {
            //use existing running ES
            client = elasticEmbeddedNode.getClient("knowledge", 9300);
        }
        // Load fuse engine config file
        String confFilePath = path.toString();
        // Start elastic data manager
        manager = new KnowledgeConfigManager(confFilePath, client);
        // Connect to elastic
        // Create indexes by templates
        if(init) {
            manager.init();
        }

        // Start fuse app (based on Jooby app web server)
        if(startFuse) {
            app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                    .conf(path.toFile(), "activeProfile");
            app.start("server.join=false");
        }

    }


    public static void cleanup() throws Exception {
        if (manager != null) {
            manager.drop();
        }
        if (app != null) {
            app.stop();
        }
    }
}