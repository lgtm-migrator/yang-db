package com.kayhut.test.framework.index;

import com.kayhut.es.plugins.script.CommonScriptPlugin;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import static com.kayhut.test.framework.TestUtil.deleteFolder;

/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
        }
    }
    //endregion

    //region Members
    private final int httpPort;
    private final String esWorkingDir;
    private final int numberOfShards;
    private Node node;

    static int httpTransportPort;
    static String nodeName;
    static TransportClient client = null;
    //endregion

    //region Constructors
    public ElasticEmbeddedNode(String clusterName) throws Exception {
        this("target/es", 9200, 9300, clusterName);
    }

    public ElasticEmbeddedNode(String clusterName, int numberOfShards) throws Exception {
        this("target/es", 9200, 9300, clusterName, numberOfShards);
    }

    public ElasticEmbeddedNode() throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic");
    }

    public ElasticEmbeddedNode(ElasticIndexConfigurer... configurers) throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic", configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer... configurers) throws Exception {
        this(esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, ElasticIndexConfigurer... configurers) throws Exception {
        ElasticEmbeddedNode.httpTransportPort = httpTransportPort;
        ElasticEmbeddedNode.nodeName = nodeName;
        this.esWorkingDir = esWorkingDir;
        this.httpPort = httpPort;
        this.numberOfShards = numberOfShards;
        prepare();

        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName,httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static TransportClient getClient() {
        return getClient(nodeName,httpTransportPort);
    }

    public static TransportClient getClient(String nodeName,int httpTransportPort) {
        if (client == null) {
            try {
                Settings settings = Settings.builder()
                        .put("cluster.name", nodeName)
                        .build();
                client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return client;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        deleteFolder(esWorkingDir);
    }

    public static void closeClient() {
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void prepare() throws Exception {
        this.close();

        Settings settings = Settings.builder()
                .put("cluster.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.enabled", "true")
                .put("script.auto_reload_enabled", "false")
                .put("transport.tcp.port", httpTransportPort)
                .build();

        this.node = new PluginConfigurableNode(settings, Arrays.asList(
                Netty4Plugin.class,
                CommonScriptPlugin.class));

        this.node = this.node.start();
    }
    //endregion
}
