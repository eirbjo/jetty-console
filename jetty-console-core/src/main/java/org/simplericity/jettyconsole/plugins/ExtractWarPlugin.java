package org.simplericity.jettyconsole.plugins;

import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

/**
 */
public class ExtractWarPlugin extends JettyConsolePluginBase {


    private boolean extractWar = true;

    private StartOption extractWarPlugin = new DefaultStartOption("extractWar") {
        @Override
        public String validate(String value) {
            if(!"true".equals(value) && !"false".equals(value)) {
                return "extractWar option must be 'true' or 'false'";
            }
            extractWar = "true".equals(value);
            return null;
        }
    };

    public ExtractWarPlugin() {
        super(ExtractWarPlugin.class);
        addStartOptions(extractWarPlugin);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.setExtractWAR(extractWar);
    }
}