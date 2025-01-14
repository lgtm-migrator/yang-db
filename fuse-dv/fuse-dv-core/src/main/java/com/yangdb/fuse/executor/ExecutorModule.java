package com.yangdb.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.internal.SingletonScope;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.yangdb.fuse.client.export.GraphWriterStrategy;
import com.yangdb.fuse.core.driver.StandardCursorDriver;
import com.yangdb.fuse.core.driver.StandardPageDriver;
import com.yangdb.fuse.core.driver.StandardQueryDriver;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.resource.store.LoggingResourceStore;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStoreFactory;
import com.yangdb.fuse.executor.opensearch.ClientProvider;
import com.yangdb.fuse.executor.opensearch.TimeoutClientAdvisor;
import com.yangdb.fuse.executor.opensearch.logging.LoggingClient;
import com.yangdb.fuse.executor.logging.LoggingCursorFactory;
import com.yangdb.fuse.executor.ontology.CachedGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.executor.ontology.schema.*;
import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphInitiator;
import com.yangdb.fuse.executor.resource.PersistentResourceStore;
import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.opensearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.configuration.UniGraphConfiguration;

import java.util.Arrays;
import java.util.List;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    public static final String globalClient = "ExecutorModule.@globalClient";

    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindGraphWriters(env, conf, binder);
        bindResourceManager(env, conf, binder);
        bindGraphInitiator(env, conf, binder);
        bindGraphDataLoader(env, conf, binder);
        bindCSVDataLoader(env, conf, binder);
        bindCursorFactory(env, conf, binder);
        bindElasticClient(env, conf, binder);
        bindRawSchema(env, conf, binder);
        bindSchemaProviderFactory(env, conf, binder);
        bindUniGraphProvider(env, conf, binder);

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);

        binder.bind(SearchOrderProviderFactory.class).to(getSearchOrderProvider(conf));
    }

    //endregion

    //region Private Methods
    protected void bindGraphWriters(Env env, Config conf, Binder binder) {
        binder.bind(GraphWriterStrategy.class).toInstance(new GraphWriterStrategy());
    }

    protected void bindResourceManager(Env env, Config conf, Binder binder) {
        // resource store and persist processor
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(ResourceStoreFactory.injectionName))
                .to(PersistentResourceStore.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(LoggingResourceStore.injectionName))
                .to(ResourceStoreFactory.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .to(LoggingResourceStore.class)
                .in(new SingletonScope());

    }

    protected void bindGraphInitiator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphInitiator.class)
                            .to(getGraphInitiator(conf));
                    this.expose(GraphInitiator.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindGraphDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphDataLoader.class)
                            .to(getGraphDataLoader(conf));
                    this.expose(GraphDataLoader.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindCSVDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CSVDataLoader.class)
                            .to(getCSVDataLoader(conf));
                    this.expose(CSVDataLoader.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindRawSchema(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PrefixedRawSchema.rawSchemaParameter))
                            .to(getRawElasticSchemaClass(conf))
                            .asEagerSingleton();

                    String prefix = conf.hasPath(conf.getString("assembly") + ".physical_raw_schema_prefix") ?
                            conf.getString(conf.getString("assembly") + ".physical_raw_schema_prefix") :
                            "";
                    this.bindConstant().annotatedWith(named(PrefixedRawSchema.prefixParameter)).to(prefix);

                    this.bind(IndicesProvider.class)
                            .annotatedWith(named(CachedRawSchema.systemIndicesParameter))
                            .to(SystemIndicesProvider.class)
                            .asEagerSingleton();
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PartitionFilteredRawSchema.rawSchemaParameter))
                            .to(PrefixedRawSchema.class)
                            .asEagerSingleton();

                    this.bind(RawSchema.class)
                            .annotatedWith(named(CachedRawSchema.rawSchemaParameter))
                            .to(PartitionFilteredRawSchema.class)
                            .asEagerSingleton();

                    this.bind(RawSchema.class).to(CachedRawSchema.class).asEagerSingleton();

                    this.expose(RawSchema.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindCursorFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorFactory.class)
                        .annotatedWith(named(LoggingCursorFactory.cursorFactoryParameter))
                        .to(CompositeCursorFactory.class)
                        .asEagerSingleton();
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCursorFactory.cursorLoggerParameter))
                        .toInstance(LoggerFactory.getLogger(Cursor.class));
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCursorFactory.traversalLoggerParameter))
                        .toInstance(LoggerFactory.getLogger(Traversal.class));
                this.bind(CursorFactory.class)
                        .to(LoggingCursorFactory.class)
                        .asEagerSingleton();

                this.expose(CursorFactory.class);
            }
        });
    }

    protected void bindElasticClient(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                boolean createMock = conf.hasPath("fuse.opensearch.mock") && conf.getBoolean("fuse.opensearch.mock");
                OpensearchGraphConfiguration opensearchGraphConfiguration = createElasticGraphConfiguration(conf);
                this.bind(OpensearchGraphConfiguration.class).toInstance(opensearchGraphConfiguration);

                ClientProvider provider = new ClientProvider(createMock, opensearchGraphConfiguration);
                Client client = provider.get();

                this.bindConstant()
                        .annotatedWith(named(ClientProvider.createMockParameter))
                        .to(createMock);

                this.bind(Client.class)
                        .annotatedWith(named(LoggingClient.clientParameter))
                        .toInstance(client);
                //.toProvider(ClientProvider.class).asEagerSingleton();

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingClient.class));
                this.bind(Client.class)
                        .to(TimeoutClientAdvisor.class)
                        .in(RequestScoped.class);

                this.bind(Client.class)
                        .annotatedWith(named(globalClient))
                        .toInstance(client);

                this.expose(Client.class);
                this.expose(Client.class).annotatedWith(named(globalClient));
                this.expose(OpensearchGraphConfiguration.class);
            }
        });
    }

    protected void bindSchemaProviderFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(OntologyGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(getSchemaProviderFactoryClass(conf));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(CachedGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(OntologyGraphElementSchemaProviderFactory.class);
                    /*this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.warnLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class));
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.verboseLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class.getName() + ".Verbose"));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .to(LoggingGraphElementSchemaProviderFactory.class);*/

                    this.bind(GraphElementSchemaProviderFactory.class)
                            .to(CachedGraphElementSchemaProviderFactory.class)
                            .asEagerSingleton();

                    this.expose(GraphElementSchemaProviderFactory.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindUniGraphProvider(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(UniGraphConfiguration.class).toInstance(createUniGraphConfiguration(conf));
                    this.bind(UniGraphProvider.class)
                            .to(getUniGraphProviderClass(conf))
                            .in(RequestScoped.class);

                    this.expose(UniGraphProvider.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Class<? extends RawSchema> getRawElasticSchemaClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_raw_schema"));
    }

    private Class<? extends GraphDataLoader> getGraphDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphDataLoader>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_data_loader")));
    }

    private Class<? extends GraphInitiator> getGraphInitiator(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphInitiator>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_initiator")));
    }

    private Class<? extends CSVDataLoader> getCSVDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends CSVDataLoader>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_csv_data_loader")));
    }

    private Class<? extends SearchOrderProviderFactory> getSearchOrderProvider(Config conf) throws ClassNotFoundException {
        return (Class<? extends SearchOrderProviderFactory>) (Class.forName(conf.getString(conf.getString("assembly") + ".search_order_provider")));
    }

    private OpensearchGraphConfiguration createElasticGraphConfiguration(Config conf) {
        OpensearchGraphConfiguration configuration = new OpensearchGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(getStringList(conf, "opensearch.hosts", Arrays.asList("127.0.0.1"))).toJavaArray(String.class));
        configuration.setClusterPort(getInt(conf,"opensearch.port",9200));
        configuration.setClusterName(getString(conf,"opensearch.cluster_name",""));
        configuration.setElasticGraphDefaultSearchSize(getLong(conf,"opensearch.default_search_size",10000000L));
        configuration.setElasticGraphMaxSearchSize(getLong(conf,"opensearch.max_search_size",1000000000L));
        configuration.setElasticGraphScrollSize(getInt(conf,"opensearch.scroll_size",1000));
        configuration.setElasticGraphScrollTime(getInt(conf,"opensearch.scroll_time",600000));

        configuration.setClientTransportIgnoreClusterName(conf.hasPath("client.transport.ignore_cluster_name") &&
                conf.getBoolean("client.transport.ignore_cluster_name"));

        return configuration;
    }

    private UniGraphConfiguration createUniGraphConfiguration(Config conf) {
        UniGraphConfiguration configuration = new UniGraphConfiguration();
        configuration.setBulkMax(conf.hasPath("unipop.bulk.max") ? conf.getInt("unipop.bulk.max") : 1000);
        configuration.setBulkMin(conf.hasPath("unipop.bulk.min") ? conf.getInt("unipop.bulk.min") : configuration.getBulkMax());
        configuration.setBulkDecayInterval(conf.hasPath("unipop.bulk.decayInterval") ? conf.getLong("unipop.bulk.decayInterval") : 200L);
        configuration.setBulkStart(conf.hasPath("unipop.bulk.start") ? conf.getInt("unipop.bulk.start") : configuration.getBulkMax());
        configuration.setBulkMultiplier(conf.hasPath("unipop.bulk.multiplier") ? conf.getInt("unipop.bulk.multiplier") : 1);
        return configuration;
    }

    protected Class<? extends GraphElementSchemaProviderFactory> getSchemaProviderFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphElementSchemaProviderFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_provider_factory_class"));
    }

    protected Class<? extends UniGraphProvider> getUniGraphProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends UniGraphProvider>) Class.forName(conf.getString(conf.getString("assembly") + ".unigraph_provider"));
    }

    protected Class<? extends CursorFactory> getCursorFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends CursorFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".cursor_factory"));
    }

    private int getInt(Config conf, String key, int defaults) {
        try {
            return conf.getInt(key);
        } catch (Exception e) {
            return defaults;
        }
    }

    private long getLong(Config conf, String key, long defaults) {
        try {
            return conf.getLong(key);
        } catch (Exception e) {
            return defaults;
        }
    }

    private String getString(Config conf, String key, String defaults) {
        try {
            return conf.getString(key);
        } catch (Exception e) {
            return defaults;
        }
    }

    private List<String> getStringList(Config conf, String key, List<String> defaults) {
        try {
            return conf.getStringList(key);
        } catch (Exception e1) {
            try {
                String strList = conf.getString(key);
                return Stream.of(strList.split(",")).toJavaList();
            } catch (Exception e2) {
                return defaults;
            }
        }
    }
    //endregion
}
