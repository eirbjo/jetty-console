/*
 * Copyright 2015 Eirik Bjørsnøs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simplericity.jettyconsole;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.*;
import org.kantega.jexmec.PluginManager;
import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.JettyConsolePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class DefaultJettyManager implements JettyManager {

    private Server server;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Properties settings;
    private String name;
    private Runnable shutdownHook;
    private List<JettyListener> listenerList = new ArrayList<JettyListener>();
    private PluginManager<JettyConsolePlugin> pluginManager;
    private final File jettyWorkDirectory;
    private WebAppContext webapp;

    public DefaultJettyManager(Properties settings, PluginManager<JettyConsolePlugin> pluginManager, File jettyWorkDirectory) {
        this.settings = settings;
        this.pluginManager = pluginManager;
        this.jettyWorkDirectory = jettyWorkDirectory;
        this.name = DefaultJettyManager.this.settings.getProperty("name");

        shutdownHook = new Runnable() {
            public void run() {
                shutdown();
            }
        };
    }


    public void shutdown() {
        log.info("Shutting down " + name + "..");
        try {
            if (server != null && (server.isStarted() || server.isStarting())) {
                server.stop();
                log.info("Shutdown of " + name + " complete.");
                for (JettyListener listener : listenerList) {
                    listener.serverStopped();
                }
            }
        } catch (Exception e) {
            log.info("Exception shutting down " + name + ": " + e.getMessage(), e);
        }
    }

    public void startServer(Configuration configuration) {
        server = new Server();

        ServerConnector connector = new ServerConnector(server);

        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            plugin.customizeConnector(connector);
        }

        server.addConnector(connector);

        HandlerCollection handlers = new HandlerCollection();

        ContextHandlerCollection contexts = new ContextHandlerCollection();

        handlers.setHandlers(new Handler[] {contexts});

        server.setHandler(handlers);

        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            plugin.customizeServer(server);
        }

        File war = getWarLocation();

        webapp = new JettyConsoleWebappContext(contexts, war.getAbsolutePath(), configuration.getContextPath());

        webapp.setTempDirectory(jettyWorkDirectory);

        // If set to false Jetty will delete the files we already have unpacked
        webapp.setPersistTempDirectory(true);

        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            plugin.beforeStart(webapp);
        }
        webapp.setAttribute("shutdownhook", shutdownHook);
        
        log.info("Added web application on path " + configuration.getContextPath() + " from war " + war.getAbsolutePath());

        log.info("Starting web application on port " + connector.getPort());

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(JettyConsoleStarter.class.getClassLoader());

            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }


    }

    public void stopServer() {
        if(webapp != null) {
            for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
                plugin.beforeStop(webapp);
            }
        }
        try {
            if (server != null && (server.isStarted() || server.isStarting())) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getWarLocation() {
        URL resource = getClass().getResource("/META-INF/jettyconsole/jettyconsole.properties");
        String file = resource.getFile();
        file = file.substring("file:".length(), file.indexOf("!"));
        try {
            file = URLDecoder.decode(file, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new File(file);
    }

    public void addListener(JettyListener listener) {
        listenerList.add(listener);
    }
}
