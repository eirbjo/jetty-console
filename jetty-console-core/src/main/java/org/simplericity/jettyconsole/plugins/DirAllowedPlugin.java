package org.simplericity.jettyconsole.plugins;

import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

/**
 */
public class DirAllowedPlugin extends JettyConsolePluginBase {


    private boolean dirAllowed = false;

    private StartOption dirAllowedOption = new DefaultStartOption("dirAllowed") {
        @Override
        public String validate(String value) {
            if(!"true".equals(value) && !"false".equals(value)) {
                return "dirAllowed option must be 'true' or 'false'";
            }
            dirAllowed = "true".equals(value);
            return null;
        }
    };

    public DirAllowedPlugin() {
        super(DirAllowedPlugin.class);
        addStartOptions(dirAllowedOption);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.getInitParams().put("org.eclipse.jetty.servlet.Default.dirAllowed", Boolean.toString(dirAllowed));
    }
}