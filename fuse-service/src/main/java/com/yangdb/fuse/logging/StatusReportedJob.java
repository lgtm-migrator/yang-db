package com.yangdb.fuse.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.resource.store.NodeStatusResource;
import org.jooby.quartz.Scheduled;

public class StatusReportedJob {
        private NodeStatusResource statusResource;

        @Inject
        public StatusReportedJob(NodeStatusResource statusResource) {
            this.statusResource = statusResource;
        }

        @Scheduled("15s; delay=20s; repeat=*")
        public void report() {
            this.statusResource.report();
    }
}