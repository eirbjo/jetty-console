package org.simplericity.jettyconsole.api;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.List;


public interface JettyConsolePlugin {
    
     List<StartOption> getStartOptions();
     void beforeStart(WebAppContext context);
     void beforeStop(WebAppContext context);

     void customizeServer(Server server);

     void customizeConnector(ServerConnector connector);

     void bootstrap();

     void configureConsole(Configuration configuration);
}
