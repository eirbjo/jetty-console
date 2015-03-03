package org.simplericity.jettyconsole.jsp;

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class JspPlugin extends JettyConsolePluginBase {


    public JspPlugin() {
        super(JspPlugin.class);
    }

    @Override
    public void beforeStart(WebAppContext context) {

        context.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*/taglibs-standard-impl-[^/]*\\.jar$");
        Configuration[] current = context.getConfigurations();

        List<Configuration> cfn = new ArrayList<Configuration>(Arrays.asList(current));
        cfn.add(new EmbeddedJspConfiguration());
        context.setConfigurations(cfn.toArray(new Configuration[cfn.size()]));
    }
}
