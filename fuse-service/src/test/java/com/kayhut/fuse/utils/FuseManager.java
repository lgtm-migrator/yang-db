package com.kayhut.fuse.utils;

import com.kayhut.fuse.client.BaseFuseClient;
import com.kayhut.fuse.client.FuseClient;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.FuseRunner;

import java.io.File;
import java.nio.file.Paths;

public class FuseManager {

    public FuseManager(String confFile, String activeProfile) {
        this.confFile = confFile;
        this.activeProfile = activeProfile;
    }

    public void init() throws Exception {
        startFuse();
    }

    public void cleanup() {
        teardownFuse();
    }

    private void teardownFuse() {
        if (fuseApp != null) {
            fuseApp.stop();
        }
    }

    private void startFuse() throws Exception {

        fuseApp = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("fuse-test", "fuse-benchmarks-test", "src", "main", "resources", "conf", confFile).toString()), activeProfile);
        new FuseRunner().run(fuseApp, new FuseRunner.Options(Paths.get("fuse-test", "fuse-benchmarks-test", "src", "main", "resources", "conf", "logback.xml").toString(), false));

        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
    }

    public FuseApp getFuseApp() {
        return fuseApp;
    }

    public FuseClient getFuseClient() {
        return fuseClient;
    }

    private String confFile;
    private String activeProfile;

    private FuseApp fuseApp;
    private FuseClient fuseClient;


}
