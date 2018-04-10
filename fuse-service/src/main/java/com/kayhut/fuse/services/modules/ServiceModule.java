package com.kayhut.fuse.services.modules;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.services.suppliers.CachedRequestIdSupplier;
import com.kayhut.fuse.services.suppliers.ExternalRequestIdSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.services.controllers.*;
import com.kayhut.fuse.services.controllers.logging.*;
import com.kayhut.fuse.services.suppliers.SnowflakeRequestIdSupplier;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 * Created by lior on 15/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class ServiceModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        // bind common components
        binder.bind(RequestIdSupplier.class)
                .annotatedWith(named(CachedRequestIdSupplier.RequestIdSupplierParameter))
                .to(SnowflakeRequestIdSupplier.class)
                .asEagerSingleton();
        binder.bind(RequestIdSupplier.class).to(CachedRequestIdSupplier.class).in(RequestScoped.class);
        binder.bind(ExternalRequestIdSupplier.class).to(ExternalRequestIdSupplier.Impl.class).in(RequestScoped.class);

        // bind service controller
        bindApiDescriptionController(env, config, binder);
        bindInternalsController(env, config, binder);
        bindQueryController(env, config, binder);
        bindCursorController(env, config, binder);
        bindPageController(env, config, binder);
        bindSearchController(env, config, binder);
        bindCatalogController(env, config, binder);
        bindDataLoaderController(env, config, binder);
        bindIdGeneratorController(env, config, binder);

        // bind requests
        binder.bind(InternalsDriver.class).to(StandardInternalsDriver.class);
        binder.bind(CreateQueryRequest.class).in(RequestScoped.class);
        //binder.bind(CreateCursorRequest.class).in(RequestScoped.class);
        binder.bind(CreatePageRequest.class).in(RequestScoped.class);

        //bind request parameters
        binder.bind(PlanTraceOptions.class).in(RequestScoped.class);
    }
    //endregion

    //region Private Methods
    private void bindApiDescriptionController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(ApiDescriptionController.class)
                        .annotatedWith(named(LoggingApiDescriptionController.controllerParameter))
                        .to(StandardApiDescriptionController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingApiDescriptionController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardApiDescriptionController.class));

                this.bind(ApiDescriptionController.class)
                        .to(LoggingApiDescriptionController.class);

                this.expose(ApiDescriptionController.class);
            }
        });
    }

    private void bindInternalsController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(InternalsController.class)
                        .to(StandardInternalsController.class);
                this.expose(InternalsController.class);
            }
        });
    }

    private void bindQueryController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorController.class)
                        .annotatedWith(named(StandardQueryController.cursorControllerParameter))
                        .to(StandardCursorController.class);

                this.bind(PageController.class)
                        .annotatedWith(named(StandardQueryController.pageControllerParameter))
                        .to(StandardPageController.class);

                this.bind(QueryController.class)
                        .annotatedWith(named(LoggingQueryController.controllerParameter))
                        .to(StandardQueryController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingQueryController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardQueryController.class));

                this.bind(QueryController.class)
                        .to(LoggingQueryController.class);

                this.expose(QueryController.class);
            }
        });
    }

    private void bindCursorController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorController.class)
                        .annotatedWith(named(LoggingCursorController.controllerParameter))
                        .to(StandardCursorController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCursorController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardCursorController.class));

                this.bind(CursorController.class)
                        .to(LoggingCursorController.class);

                this.expose(CursorController.class);
            }
        });
    }

    private void bindPageController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(PageController.class)
                        .annotatedWith(named(LoggingPageController.controllerParameter))
                        .to(StandardPageController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingPageController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardPageController.class));

                this.bind(PageController.class)
                        .to(LoggingPageController.class);

                this.expose(PageController.class);
            }
        });
    }

    private void bindSearchController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(SearchController.class)
                        .annotatedWith(named(LoggingSearchController.controllerParameter))
                        .to(StandardSearchController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingSearchController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardSearchController.class));

                this.bind(SearchController.class)
                        .to(LoggingSearchController.class);

                this.expose(SearchController.class);
            }
        });
    }

    private void bindCatalogController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CatalogController.class)
                        .annotatedWith(named(LoggingCatalogController.controllerParameter))
                        .to(StandardCatalogController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCatalogController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardCatalogController.class));

                this.bind(CatalogController.class)
                        .to(LoggingCatalogController.class);

                this.expose(CatalogController.class);
            }
        });
    }

    private void bindDataLoaderController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(DataLoaderController.class)
                        .annotatedWith(named(LoggingDataLoaderController.controllerParameter))
                        .to(StandardDataLoaderController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingDataLoaderController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardDataLoaderController.class));

                this.bind(DataLoaderController.class)
                        .to(LoggingDataLoaderController.class);

                this.expose(DataLoaderController.class);
            }
        });
    }

    private void bindIdGeneratorController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<IdGeneratorController<Object>>(){})
                        .to(new TypeLiteral<StandardIdGeneratorController<Object>>(){}).asEagerSingleton();

                this.expose(new TypeLiteral<IdGeneratorController<Object>>(){});
            }
        });
    }
    //endregion
}
