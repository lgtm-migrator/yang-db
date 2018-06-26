package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController implements CursorController {
    public static final String controllerParameter = "LoggingCursorController.@controller";
    public static final String loggerParameter = "LoggingCursorController.@logger";

    //region Constructors
    @Inject
    public LoggingCursorController(
            @Named(controllerParameter) CursorController controller,
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

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).get())).write();

        ContentResponse<CursorResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start create", sequence, LogType.of(start), create).log();
            this.metricRegistry.counter(name(this.logger.getName(), "count")).inc();
            response = this.controller.create(queryId, createCursorRequest);
            new LogMessage.Impl(this.logger, info, "finish create", sequence, LogType.of(success), create, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish create", sequence, LogType.of(success), create, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "success")).mark();
            this.metricRegistry.counter(name(this.logger.getName(), "count")).dec();

        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed create", sequence, LogType.of(failure), create, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "failure")).mark();
            this.metricRegistry.counter(name(this.logger.getName(), "count")).dec();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).get())).write();

        ContentResponse<StoreResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryId", sequence, LogType.of(start), getInfoByQueryId).log();
            response = this.controller.getInfo(queryId);
            new LogMessage.Impl(this.logger, info, "finish getInfoByQueryId", sequence, LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryId", sequence, LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryId", sequence, LogType.of(failure), getInfoByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        ContentResponse<CursorResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorId", sequence, LogType.of(start), getInfoByQueryIdAndCursorId).log();
            response = this.controller.getInfo(queryId, cursorId);
            new LogMessage.Impl(this.logger, info, "finish getInfoByQueryIdAndCursorId", sequence, LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryIdAndCursorId", sequence, LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryIdAndCursorId", sequence, LogType.of(failure), getInfoByQueryIdAndCursorId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), delete.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        ContentResponse<Boolean> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start delete", sequence, LogType.of(start), delete).log();
            response = this.controller.delete(queryId, cursorId);
            new LogMessage.Impl(this.logger, info, "finish delete", sequence, LogType.of(success), delete, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish delete", sequence, LogType.of(success), delete, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed delete", sequence, LogType.of(failure), delete, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "failure")).mark();
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
    private CursorController controller;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
