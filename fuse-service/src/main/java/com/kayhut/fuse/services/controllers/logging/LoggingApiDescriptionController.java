package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

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
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.requestExternalMetadataSupplier = requestExternalMetadataSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfo")).time();

        LogMessage.MDCWriter.Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get())).write();

        ContentResponse<FuseResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfo", LogType.of(start), getInfo).log();
            response = this.controller.getInfo();
            new LogMessage.Impl(this.logger, info, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfo", LogType.of(failure), getInfo, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private RequestExternalMetadataSupplier requestExternalMetadataSupplier;
    private MetricRegistry metricRegistry;
    private ApiDescriptionController controller;

    private static LogMessage.MDCWriter getInfo = MethodName.of("getInfo");
    //endregion
}
