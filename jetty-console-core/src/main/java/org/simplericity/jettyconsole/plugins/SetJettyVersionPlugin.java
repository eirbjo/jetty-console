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

package org.simplericity.jettyconsole.plugins;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Manifest;

/**
 */
public class SetJettyVersionPlugin extends JettyConsolePluginBase {

    public SetJettyVersionPlugin() {
        super(SetJettyVersionPlugin.class);
    }

    @Override
    public void bootstrap() {
        String path = "org/eclipse/jetty/server/";
        URL resource = getClass().getClassLoader().getResource(path);

        try {
            URL manifestURL = new URL("jar:" + resource.getFile().substring(0, resource.getFile().length()-path.length()) +"META-INF/MANIFEST.MF");
            Manifest mf = new Manifest(manifestURL.openStream());

            System.setProperty("jetty.version", mf.getMainAttributes().getValue("Implementation-Version"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}