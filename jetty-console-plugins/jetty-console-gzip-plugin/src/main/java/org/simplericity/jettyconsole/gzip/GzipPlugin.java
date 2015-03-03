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
