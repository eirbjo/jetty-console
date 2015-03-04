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

import org.eclipse.jetty.server.*;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.URIUtil;

/**
 */
public class SSLProxyPlugin extends JettyConsolePluginBase {

    private boolean sslProxy = false;


    private StartOption sslOption = new DefaultStartOption("sslProxied") {
        @Override
        public String validate() {
            sslProxy = true;
            return null;
        }
    };

    public SSLProxyPlugin() {
        super(SSLProxyPlugin.class);
        addStartOptions(sslOption);
    }

    @Override
    public void customizeConnector(ServerConnector connector) {
        if(sslProxy) {
            HttpConfiguration config = connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
            config.addCustomizer(new HttpConfiguration.Customizer() {
                @Override
                public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
                    request.setScheme(URIUtil.HTTPS);
                    request.setSecure(true);
                }
            });
        }
    }
}
