package org.simplericity.jettyconsole.plugins;

import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class InitParamConfigurationPlugin extends JettyConsolePluginBase {


    private Map<String, String> initParams = new HashMap<String, String>();

    private StartOption initParamOption = new DefaultStartOption("initParam") {
        @Override
        public String validate(String value) {
            if(!value.contains("=")) {
                return "--initParam must be specified as 'name=value'";
            }
            String name = value.substring(0, value.indexOf("="));
            String val = value.substring(value.indexOf("=")+1);
            if(initParams.containsKey(name)) {
                return "--initParam " + name + " already specified as " + initParams.get(name);
            }
            initParams.put(name, val);
            return null;
        }
    };
    public InitParamConfigurationPlugin() {
        super(InitParamConfigurationPlugin.class);
        addStartOptions(initParamOption);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.getInitParams().putAll(initParams);
    }
}
