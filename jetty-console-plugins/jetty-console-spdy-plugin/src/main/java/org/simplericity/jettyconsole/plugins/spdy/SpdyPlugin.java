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

package org.simplericity.jettyconsole.plugins.spdy;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.spdy.server.http.HTTPSPDYServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.List;

/**
 *
 */
public class SpdyPlugin extends JettyConsolePluginBase {

    private File keystore;
    private String keypass;

    private int spdyPort;

    public SpdyPlugin() {
        super(SpdyPlugin.class);

        addStartOptions(new DefaultStartOption("spdyPort") {
            @Override
            public String validate(String value) {
                try {
                    spdyPort = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return value + " is not a valid port number";
                }
                return null;
            }
        });

        addStartOptions(new DefaultStartOption("spdyKeystore") {
            @Override
            public String validate(String value) {
                File file = new File(value);
                if(!file.exists()) {
                    return "File does not exist: " + file;
                }
                keystore = file;
                return null;
            }
        });
        addStartOptions(new DefaultStartOption("spdyKeystorePasswordEnvVar") {
            @Override
            public String validate(String value) {
                keypass = System.getenv(value);
                if(keypass == null) {
                    return "Environment variable " + value + " is not set. Can't get SPDY keystore password.";
                }
                return null;
            }
        });

    }

    @Override
    public void bootstrap() {

        if(spdyPort != 0 ) {
            if(keystore == null) {
                throw new IllegalArgumentException("--spdyKeystorePath is required when --spdyPort is set!");
            }
            if(keypass == null) {
                throw new IllegalArgumentException("--spdyKeystorePasswordEnv is required when --spdyPort is set!");
            }
            ClassLoader loader = ClassLoader.getSystemClassLoader();

            if(loader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) loader;

                addToolsJar(urlClassLoader);

                attachAgent();
            }
        }


    }

    @Override
    public void customizeServer(Server server) {

        if(spdyPort != 0) {
            SslContextFactory factory = new SslContextFactory();
            factory.setIncludeProtocols("TLSv1");
            factory.setKeyStorePath(keystore.getAbsolutePath());
            factory.setKeyStorePassword(keypass);

            HTTPSPDYServerConnector connector = new HTTPSPDYServerConnector(server, factory);
            connector.setPort(spdyPort);
            server.addConnector(connector);
        }
    }

    private void attachAgent() {

        String agentPath = getJarFilePath(SpdyPlugin.class);

        String npnPath = URLDecoder.decode(getClass().getClassLoader().getResource("org/eclipse/jetty/npn/").getFile());

        npnPath = npnPath.substring("file:".length(), npnPath.indexOf("!"));

        List<VirtualMachineDescriptor> list = VirtualMachine.list();

        for (VirtualMachineDescriptor descriptor : list) {
            if(ManagementFactory.getRuntimeMXBean().getName().contains(descriptor.id())) {
                try {
                    VirtualMachine attach = VirtualMachine.attach(descriptor);
                    attach.loadAgent(agentPath, npnPath);
                    attach.detach();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private String getJarFilePath(Class<SpdyPlugin> ofClass) {
        String file = URLDecoder.decode(getClass().getResource(ofClass.getSimpleName() + ".class").getFile());

        file = file.substring("file:".length(), file.indexOf("!"));
        return file;
    }

    private void addToolsJar(URLClassLoader urlClassLoader) {
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

            addURL.setAccessible(true);

            URL toolsJar = new File(new File(System.getProperty("java.home")).getParentFile(), "lib/tools.jar").toURI().toURL();
            addURL.invoke(urlClassLoader, toolsJar);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
