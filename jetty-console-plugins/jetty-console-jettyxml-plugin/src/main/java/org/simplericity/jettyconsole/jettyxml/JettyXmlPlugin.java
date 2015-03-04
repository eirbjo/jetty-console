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

package org.simplericity.jettyconsole.jettyxml;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.xml.sax.SAXException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class JettyXmlPlugin extends JettyConsolePluginBase {

    private List<File> jettyXmlFiles = new ArrayList<File>();

    private List<File> jettyWebXmlFiles = new ArrayList<File>();

    private StartOption jettyXmlOption = new JettyXmlFileOption("jettyXml", jettyXmlFiles);

    private StartOption jettyWebXmlOption = new JettyXmlFileOption("jettyWebXml", jettyWebXmlFiles);

    public JettyXmlPlugin() {
        super(JettyXmlPlugin.class);
        addStartOptions(jettyXmlOption, jettyWebXmlOption);
    }

    class JettyXmlFileOption extends DefaultStartOption {
        private final List<File> fileList;

        JettyXmlFileOption(String name, List<File> fileList) {
            super(name);
            this.fileList = fileList;
        }

        @Override
        public String validate(String value) {
            File file = new File(value);
            if(!file.exists()) {
                return "Jetty XML file " + file.toString() + " does not exist";
            } else {
                fileList.add(file);
                return null;
            }
        }
    }

    @Override
    public void customizeServer(Server server) {
        for(File file : jettyXmlFiles) {
            configure(server, file);
        }
    }

    @Override
    public void beforeStart(WebAppContext context) {
        for(File file : jettyWebXmlFiles) {
            configure(context, file);
        }
    }

    private void configure(Object object, File file) {
        try {
            XmlConfiguration configuration = new XmlConfiguration(file.toURI().toURL());
            configuration.configure(object);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
