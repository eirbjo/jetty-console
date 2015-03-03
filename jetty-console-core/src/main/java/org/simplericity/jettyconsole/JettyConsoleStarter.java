package org.simplericity.jettyconsole;

/*
 * Copyright 2007 Eirik Bjorsnos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.eclipse.jetty.util.IO;
import org.kantega.jexmec.ServiceKey;
import org.kantega.jexmec.ctor.ConstructorInjectionPluginLoader;
import org.kantega.jexmec.manager.DefaultPluginManager;
import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.JettyConsolePlugin;
import org.simplericity.jettyconsole.api.Settings;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.io.MultiOutputStream;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationAdapter;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.DefaultApplication;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

import static org.simplericity.jettyconsole.JettyConsoleBootstrapMainClass.usage;

/**
 * Wrapper for the JettyConsole that:
 * <ul>
 * <li>makes sure System.out and System are redirected before any output/logging is done</li>
 * <li>sets up capturing of the quit callbacks on Apply by way of reflection</li>
 * </ul>
 */
public class JettyConsoleStarter extends ApplicationAdapter {
    private JettyConsole console;
    private DefaultPluginManager<JettyConsolePlugin> pluginManager;
    private PrintStream origOut = System.out;
    private PrintStream origErr = System.err;
    private MultiOutputStream multiErr;
    private MultiOutputStream multiOut;
    private static JettyConsoleStarter starter;
    private JettyManager jettyManager;

    public static File jettyWorkDirectory;


    public static void main(String[] args) throws Exception {

        starter = new JettyConsoleStarter();

        starter.startPluginManager();
        starter.run(starter.readConfiguration(args));

    }

    public static void stop() {
        starter.shutdown();
    }

    private void shutdown() {
        if(jettyManager != null) {
            jettyManager.shutdown();
        }
        if(jettyWorkDirectory != null) {
            IO.delete(jettyWorkDirectory);
        }
    }

    private void startPluginManager() {
        pluginManager = createPluginManager(getSettings());
        pluginManager.start();
    }

    private Configuration readConfiguration(String[] args) {
        return parseCommandLine(args, new DefaultConfiguration());
    }

    private Configuration parseCommandLine(String[] args, Configuration configuration) {
        Map<String, StartOption> pluginOptions = createOptionByNameMap();

        Map<String, JettyConsoleBootstrapMainClass.Option> declaredOptions = JettyConsoleBootstrapMainClass.getOptionsByName();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if (!arg.startsWith("--")) {
                usage("Options must start with '--': " + arg);
            }
            arg = arg.substring(2);

            StartOption option = pluginOptions.get(arg.toLowerCase());

            if (option == null) {
                usage("Unknown option: '" + arg +"'");
            }

            JettyConsoleBootstrapMainClass.Option declaredOption = declaredOptions.get(option.getName().toLowerCase());


            if (declaredOption != null && declaredOption.getSample() != null) {
                if (i == args.length - 1) {
                    usage("--" + arg +" option requires a value");
                } else {
                    String errorMessage = option.validate(args[++i].trim());
                    if (errorMessage != null) {
                        usage(errorMessage);
                    }

                }
            } else {
                option.validate();
            }
        }
        return configuration;
    }

    private Map<String, StartOption> createOptionByNameMap() {
        final Map<String, StartOption> options = new HashMap<String, StartOption>();
        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            for (StartOption option : plugin.getStartOptions()) {
                options.put(option.getName().toLowerCase(), option);
            }
        }
        return options;
    }

    private void run(Configuration configuration) throws Exception {
        setupStreams();
        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            plugin.configureConsole(configuration);
        }

        for (JettyConsolePlugin plugin : pluginManager.getPlugins()) {
            plugin.bootstrap();
        }

        jettyManager = new DefaultJettyManager(getSettings(), pluginManager, jettyWorkDirectory);

        if (configuration.isHeadless() || GraphicsEnvironment.isHeadless()) {
            initConsoleApp(configuration, jettyManager);
        } else {
            initSwingApp(configuration, jettyManager);
        }
    }


    private DefaultPluginManager<JettyConsolePlugin> createPluginManager(Properties settings) {
        DefaultPluginManager<JettyConsolePlugin> manager = new DefaultPluginManager<>(JettyConsolePlugin.class);
        manager.addPluginClassLoader(getClass().getClassLoader());
        manager.addPluginLoader(new ConstructorInjectionPluginLoader<JettyConsolePlugin>());
        manager.addService(ServiceKey.by(Settings.class), new DefaultSettings(settings));
        return manager;
    }


    private void initConsoleApp(Configuration configuration, JettyManager jettyManager) {
        jettyManager.startServer(configuration);
    }

    private void initSwingApp(final Configuration configuration, final JettyManager jettyManager) throws Exception {

        final Properties settings = getSettings();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console = new JettyConsole(settings, jettyManager, pluginManager);
                console.setMultiOut(multiOut);
                console.setMultiErr(multiErr);
                try {
                    console.init(configuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        initAppleExtensions(settings);
    }

    private void setupStreams() {
        multiOut = new MultiOutputStream(origOut);
        System.setOut(new PrintStream(multiOut, true));

        multiErr = new MultiOutputStream(origErr);
        System.setErr(new PrintStream(multiErr, true));
    }

    private void initAppleExtensions(Properties settings) throws Exception {


        Application application = new DefaultApplication();

        if (application.isMac()) {
            application.addApplicationListener(this);

            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("META-INF/jettyconsole/background-image.jpg");
            if (imageStream != null) {

                BufferedImage image = ImageIO.read(imageStream);
                BufferedImage icon = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

                float imageRatio = (float) image.getWidth() / image.getHeight();

                int width = imageRatio > 1 ? (int) (icon.getWidth() * imageRatio) : icon.getWidth();
                int height = imageRatio < 1 ? (int) (icon.getHeight() / imageRatio) : icon.getHeight();
                int x = icon.getWidth() / 2 - width / 2;
                int y = icon.getHeight() / 2 - height / 2;

                Graphics2D g2 = (Graphics2D) icon.getGraphics();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D.Double rect = new RoundRectangle2D.Double(1, 1, 126, 126, 50, 50);

                g2.setClip(rect);
                g2.drawImage(image, x, y, width, height, null);
                g2.setClip(0, 0, icon.getWidth(), icon.getHeight());
                g2.setColor(Color.DARK_GRAY);
                g2.draw(rect);
                g2.dispose();
                icon.flush();
                application.setApplicationIconImage(icon);
            }

        }
    }

    public void handleQuit(ApplicationEvent event) {
        console.conditionalExit();
    }


    public Properties getSettings() {

        try {
            Properties settings = new Properties();
            settings.load(getClass().getClassLoader().getResourceAsStream("META-INF/jettyconsole/jettyconsole.properties"));
            return settings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    class DefaultSettings implements Settings {

        private Properties properties;

        DefaultSettings(Properties properties) {
            this.properties = properties;
        }

        public String getProperty(String name) {
            return properties.getProperty(name);
        }

        public Collection<String> getPropertyNames() {
            return new HashSet(properties.keySet());
        }
    }
}
