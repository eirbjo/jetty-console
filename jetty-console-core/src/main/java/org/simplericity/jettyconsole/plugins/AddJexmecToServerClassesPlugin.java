package org.simplericity.jettyconsole.plugins;

import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

/**
 */
public class AddJexmecToServerClassesPlugin extends JettyConsolePluginBase {
    public AddJexmecToServerClassesPlugin() {
        super(AddJexmecToServerClassesPlugin.class.getName());
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.addServerClass("org.kantega.jexmec.");
    }
}
