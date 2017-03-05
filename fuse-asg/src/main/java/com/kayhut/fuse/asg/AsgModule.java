package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.AsgDefaultStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.SimpleStrategyRegisteredAsgDriver;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class AsgModule implements Jooby.Module  {
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class).to(AsgDefaultStrategyRegistrar.class).asEagerSingleton();
        binder.bind(AsgDriver.class).to(SimpleStrategyRegisteredAsgDriver.class).asEagerSingleton();
    }
}
