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