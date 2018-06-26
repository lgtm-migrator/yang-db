package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingPageController implements PageController {
    public static final String controllerParameter = "LoggingPageController.@controller";
    public static final String loggerParameter = "LoggingPageController.@logger";

    //region Constructors
    @Inject
    public LoggingPageController(
            @Named(controllerParameter) PageController controller,
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

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        ContentResponse<PageResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start create", sequence, LogType.of(start), create).log();
            this.metricRegistry.counter(name(this.logger.getName(), "count")).inc();
            response = this.controller.create(queryId, cursorId, createPageRequest);
            new LogMessage.Impl(this.logger, info, "finish create",
                    sequence, LogType.of(success), create, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish create",
                    sequence, LogType.of(success), create, ElapsedFrom.now()).log();
            this.metricRegistry.counter(name(this.logger.getName(), "count")).dec();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "success")).mark();
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
    public ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), createAndFetch.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        ContentResponse<PageResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start createAndFetch", sequence, LogType.of(start), createAndFetch).log();
            response = this.controller.createAndFetch(queryId, cursorId, createPageRequest);
            new LogMessage.Impl(this.logger, info, "finish createAndFetch",
                    sequence, LogType.of(success), createAndFetch, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish createAndFetch",
                    sequence, LogType.of(success), createAndFetch, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), createAndFetch.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed createAndFetch", sequence, LogType.of(failure), createAndFetch, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), createAndFetch.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        ContentResponse<StoreResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorId",
                    sequence, LogType.of(start), getInfoByQueryIdAndCursorId).log();
            response = this.controller.getInfo(queryId, cursorId);
            new LogMessage.Impl(this.logger, info, "finish getInfoByQueryIdAndCursorId",
                    sequence, LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryIdAndCursorId",
                    sequence, LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryIdAndCursorId",
                    sequence, LogType.of(failure), getInfoByQueryIdAndCursorId, ElapsedFrom.now())
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
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())).write();

        ContentResponse<PageResourceInfo> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorIdAndPageId",
                    sequence, LogType.of(start), getInfoByQueryIdAndCursorIdAndPageId).log();
            response = this.controller.getInfo(queryId, cursorId, pageId);
            new LogMessage.Impl(this.logger, info, "finish getInfoByQueryIdAndCursorIdAndPageId",
                    sequence, LogType.of(success), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryIdAndCursorIdAndPageId",
                    sequence, LogType.of(success), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryIdAndCursorIdAndPageId",
                    sequence, LogType.of(failure), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getData.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())).write();

        ContentResponse<Object> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getData", sequence, LogType.of(start), getData).log();
            response = this.controller.getData(queryId, cursorId, pageId);
            new LogMessage.Impl(this.logger, info, "finish getData", sequence, LogType.of(success), getData, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getData", sequence, LogType.of(success), getData, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getData.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getData", LogType.of(failure), getData, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getData.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), delete.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())).write();

        ContentResponse<Boolean> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start delete", sequence, LogType.of(start), delete).log();
            response = this.controller.delete(queryId, cursorId, pageId);
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
    private PageController controller;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter createAndFetch = MethodName.of("createAndFetch");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorIdAndPageId = MethodName.of("getInfoByQueryIdAndCursorIdAndPageId");
    private static MethodName.MDCWriter getData = MethodName.of("getData");
    private static MethodName.MDCWriter delete = MethodName.of("delete");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
