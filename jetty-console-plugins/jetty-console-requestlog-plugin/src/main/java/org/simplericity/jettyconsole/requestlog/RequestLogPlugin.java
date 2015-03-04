/*
 * Copyright 2015 Eirik Bjørsnøs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simplericity.jettyconsole.requestlog;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;

/**
 */
public class RequestLogPlugin extends JettyConsolePluginBase {

    private File logFile;

    private StartOption requestLogOption = new DefaultStartOption("requestLog") {

        @Override
        public String validate(String value) {
            logFile = new File(value);

            if(logFile.getParentFile() != null && !logFile.getParentFile().exists()) {
                return "Directory does not exist: " + logFile.getParentFile().getPath();
            }
            return null;
        }
    };

    private boolean extended;

    private StartOption requestLogExtended = new DefaultStartOption("requestLogExtended") {

        @Override
        public String validate() {
            extended = true;
            return null;
        }
    };


    public RequestLogPlugin() {
        super(RequestLogPlugin.class);
        addStartOptions(requestLogOption, requestLogExtended);
    }

    @Override
    public void customizeServer(Server server) {
        if(logFile != null) {
        HandlerCollection rootHandler = (HandlerCollection) server.getHandler();

        List<Handler> handlers = new ArrayList<Handler>();

        handlers.addAll(Arrays.asList(rootHandler.getHandlers()));

        RequestLogHandler requestLogHandler = new RequestLogHandler();

        NCSARequestLog requestLog = new NCSARequestLog(logFile.getAbsolutePath());
        requestLog.setRetainDays(0);
        requestLog.setAppend(true);
        requestLog.setExtended(extended);
        requestLog.setLogTimeZone("GMT");
        requestLogHandler.setRequestLog(requestLog);

        handlers.add(requestLogHandler);

        rootHandler.setHandlers(handlers.toArray(new Handler[handlers.size()]));

        }
    }
}
