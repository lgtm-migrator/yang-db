package com.kayhut.fuse.services;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.logging.StatusReportedJob;
import com.kayhut.fuse.services.appRegistrars.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;
import org.jooby.Jooby;
import org.jooby.RequestLogger;
import org.jooby.Results;
import org.jooby.caffeine.CaffeineCache;
import org.jooby.handlers.CorsHandler;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.quartz.Quartz;
import org.jooby.scanner.Scanner;
import org.jooby.swagger.SwaggerUI;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.quartz.JobBuilder.newJob;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {
    //region Consructors
    public FuseApp(AppUrlSupplier localUrlSupplier) {
        use(new Scanner());

        use("*", new RequestLogger().extended());
        //metrics statistics
        MetricRegistry metricRegistry = new MetricRegistry();
        bind(metricRegistry);
        use(new Metrics(metricRegistry)
                .request()
                .threadDump()
                .ping()
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet()));

        use(use(new CaffeineCache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>>() {}));
        get("", () ->  Results.redirect("/public/assets/earth.html"));
        get("/", () ->  Results.redirect("/public/assets/earth.html"));
        get("/collision", () ->  Results.redirect("/public/assets/collision.html"));
        get("swagger/swagger.json", () ->  Results.redirect("/public/assets/swagger/swagger.json"));

        //internal quarts reporting job scheduler
        use(new Quartz().with(StatusReportedJob.class));

        //'Access-Control-Allow-Origin' header
        use("*", new CorsHandler());
        //expose html assets
        assets("public/assets/**");
        assets("public/assets/lib/**");

        new LoggingJacksonRendererRegistrar(metricRegistry).register(this, localUrlSupplier);
        new BeforeAfterAppRegistrar().register(this, localUrlSupplier);
        new CorsAppRegistrar().register(this, localUrlSupplier);
        new HomeAppRegistrar().register(this, localUrlSupplier);
        new HealthAppRegistrar().register(this, localUrlSupplier);

        //dynamically load AppControllerRegistrar that comply with com.kayhut.fuse.services package and derive from AppControllerRegistrarBase
        additionalRegistrars(this, localUrlSupplier);
        //swagger
        new SwaggerUI()
                .filter(route -> route.pattern().startsWith("/fuse"))
                .install(this);
    }

    /**
     * dynamically load AppControllerRegistrar that comply with com.kayhut.fuse.services package and derive from AppControllerRegistrarBase
     * @param fuseApp
     * @param localUrlSupplier
     */
    private void additionalRegistrars(FuseApp fuseApp, AppUrlSupplier localUrlSupplier) {
        Reflections reflections = new Reflections(FuseApp.class.getPackage().getName());
        Set<Class<? extends AppControllerRegistrarBase>> allClasses = reflections.getSubTypesOf(AppControllerRegistrarBase.class);
        allClasses.forEach(clazz-> {
            try {
                clazz.getConstructor().newInstance().register(fuseApp,localUrlSupplier);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
    //endregion


    //region Public Methods
    public FuseApp conf(File file, String activeProfile, Tuple2<String,ConfigValue> ... values) {
        Config config = ConfigFactory.parseFile(file);
        config = config.withValue("application.profile", ConfigValueFactory.fromAnyRef(activeProfile, "FuseApp"));
        for (Tuple2<String, ConfigValue> value : values) {
            config = config.withValue(value._1, value._2);
        }
        super.use(config);
        return this;
    }
    //endregion
}
