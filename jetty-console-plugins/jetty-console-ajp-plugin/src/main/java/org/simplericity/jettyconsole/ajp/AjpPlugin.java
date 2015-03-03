package org.simplericity.jettyconsole.ajp;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.Server;

/**
 */
public class AjpPlugin extends JettyConsolePluginBase {
    private int ajpPort;

    private StartOption ajpOption = new DefaultStartOption("ajpPort") {
        @Override
        public String validate(String value) {
            try {
                ajpPort = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return "--ajpPort option requires a numerical value between 1 and 65535";
            }
            return null;
        }
    };

    public AjpPlugin() {
        super(AjpPlugin.class.getName());
        addStartOptions(ajpOption);
    }

    @Override
    public void customizeServer(Server server) {
        if (ajpPort > 0) {

            Ajp13SocketConnector ajpConnector = new Ajp13SocketConnector();
            ajpConnector.setPort(ajpPort);
            server.addConnector(ajpConnector);
        }
    }
}
