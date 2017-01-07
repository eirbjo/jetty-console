package org.simplericity.jettyconsole.websocket;

import javax.servlet.ServletException;

import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

public class JettyWebsocketPlugin extends JettyConsolePluginBase {

	public JettyWebsocketPlugin() {
		super(JettyWebsocketPlugin.class);
	}

	@Override
	public void beforeStart(WebAppContext context) {
		try {
			WebSocketServerContainerInitializer.configureContext(context);
		} catch (ServletException e) {
			throw new RuntimeException(e);
		}
	}

}
