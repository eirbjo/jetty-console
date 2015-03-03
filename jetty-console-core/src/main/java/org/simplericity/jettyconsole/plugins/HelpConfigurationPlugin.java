package org.simplericity.jettyconsole.plugins;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;

/**
 */
public class HelpConfigurationPlugin extends JettyConsolePluginBase {
    private StartOption helpOption = new DefaultStartOption("help");

    public HelpConfigurationPlugin() {
        super(HelpConfigurationPlugin.class);
        addStartOptions(helpOption);
    }

}
