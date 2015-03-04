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

package org.simplericity.jettyconsole.gzip;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 */
public class GzipPlugin extends JettyConsolePluginBase {
    private static final String DEFAULT_MIME_TYPES = "text/html,text/xhtml,text/javascript,application/x-javascript,text/css,text/plain";

    public GzipPlugin() {
        super(GzipPlugin.class);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        FilterHolder filterHolder  = new FilterHolder(GzipFilter.class);
        filterHolder.setInitParameter("mimeTypes", DEFAULT_MIME_TYPES);

        context.addFilter(filterHolder, "/*",  EnumSet.of(DispatcherType.FORWARD, DispatcherType.REQUEST));
    }
}
