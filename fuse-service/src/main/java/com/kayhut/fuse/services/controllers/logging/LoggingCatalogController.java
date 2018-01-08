package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController implements CatalogController {
    public static final String controllerParameter = "LoggingCatalogController.@controller";
    public static final String loggerParameter = "LoggingCatalogController.@logger";

    //region Constructors
    @Inject
    public LoggingCatalogController(
            @Named(controllerParameter) CatalogController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getOntology")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getOntology");
            return controller.getOntology(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getOntology", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getOntology", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getOntology");
                this.metricRegistry.meter(name(this.logger.getName(), "getOntology", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getSchema")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getSchema");
            return controller.getSchema(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getSchema", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getSchema", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getSchema");
                this.metricRegistry.meter(name(this.logger.getName(), "getSchema", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private CatalogController controller;
    //endregion
}
