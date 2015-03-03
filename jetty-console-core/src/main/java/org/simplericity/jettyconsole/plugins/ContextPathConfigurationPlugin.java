package org.simplericity.jettyconsole.plugins;

import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

/**
 */
public class ContextPathConfigurationPlugin extends JettyConsolePluginBase {


    private String contextPath;

    private StartOption contextPathOption = new DefaultStartOption("contextPath") {
        @Override
        public String validate(String value) {
            contextPath = value;
            return null;
        }
    };
    public ContextPathConfigurationPlugin() {
        super(ContextPathConfigurationPlugin.class);
        addStartOptions(contextPathOption);
    }

    @Override
    public void configureConsole(Configuration configuration) {
        if(contextPath != null) {
            configuration.setContextPath(contextPath);
        }
    }

}
