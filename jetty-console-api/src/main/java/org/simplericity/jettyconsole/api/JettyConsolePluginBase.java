package org.simplericity.jettyconsole.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class JettyConsolePluginBase implements JettyConsolePlugin {

    private List<StartOption> startOptions = new ArrayList<StartOption>();
    private String pluginUid;

    public JettyConsolePluginBase(String pluginUid) {
        this.pluginUid = pluginUid;
    }

    protected JettyConsolePluginBase(Class<? extends JettyConsolePluginBase> pluginClass) {
        this(pluginClass.getName());
    }

    protected void addStartOptions(StartOption... startOptions) {
        this.startOptions.addAll(Arrays.asList(startOptions));
    }
    public void beforeStart(WebAppContext context) {
        
    }

    public void configureConsole(Configuration configuration) {
        
    }

    public void bootstrap() {
        
    }

    public void customizeConnector(ServerConnector connector) {
        
    }

    public void customizeServer(Server server) {
        
    }

    public void beforeStop(WebAppContext context) {
        
    }

    public List<StartOption> getStartOptions() {
        return startOptions;
    }
}
