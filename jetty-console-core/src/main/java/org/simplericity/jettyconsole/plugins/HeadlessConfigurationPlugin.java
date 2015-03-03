package org.simplericity.jettyconsole.plugins;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;

/**
 */
public class HeadlessConfigurationPlugin extends JettyConsolePluginBase {

    private boolean headless;

    private StartOption headLessOption = new DefaultStartOption("headless") {
        @Override
        public String validate() {
            headless = true;
            return null;
        }
    };
    public HeadlessConfigurationPlugin() {
        super(HeadlessConfigurationPlugin.class);
        addStartOptions(headLessOption);
    }


    @Override
    public void configureConsole(Configuration configuration) {
        if(headless) {
            configuration.setHeadless(true);
        }
    }

}
