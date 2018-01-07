package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingApiDescriptionController implements ApiDescriptionController {
    public static final String controllerParameter = "LoggingApiDescriptionController.@controller";
    public static final String loggerParameter = "LoggingApiDescriptionController.@logger";

    //region Constructors
    @Inject
    public LoggingApiDescriptionController(
            @Named(controllerParameter) ApiDescriptionController controller,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfo")).time();

        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
                this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private ApiDescriptionController controller;
    //endregion
}
