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

public class ConnectorConfigurationPlugin extends JettyConsolePluginBase {

    public static final int DEFAULT_PORT = 8080;
    private int port = DEFAULT_PORT;
    private String bindAddress = null;
    private boolean forwarded = false;

    private int requestHeaderSize = -1;

    private StartOption portOption = new DefaultStartOption("port") {
        @Override
        public String validate(String value) {
            final String msg = "--port option requires a numerical value between 1 and 65535";
            try {
                int port = Integer.parseInt(value);
                if(port < 1 || port > 65535) {
                    return msg;
                }
                ConnectorConfigurationPlugin.this.port = port;

            } catch (NumberFormatException e) {
                return msg;
            }
            return null;
        }
    };

    private StartOption bindAddressOption = new DefaultStartOption("bindAddress") {
        @Override
        public String validate(String value) {
            bindAddress = value;
            return null;
        }
    };

    private StartOption forwardedOption = new DefaultStartOption("forwarded") {
        @Override
        public String validate() {
            forwarded = true;
            return null;
        }
    };


    private StartOption requestHeaderSizeOption = new RequestHeaderStartOption();

    public ConnectorConfigurationPlugin() {
        super(ConnectorConfigurationPlugin.class);
        addStartOptions(portOption, bindAddressOption, forwardedOption, requestHeaderSizeOption);
    }

    @Override
    public void customizeConnector(ServerConnector connector) {
        connector.setPort(port);
        if(bindAddress != null) {
            connector.setHost(bindAddress);
        }
        HttpConfiguration config = connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
        if(forwarded) {
            config.addCustomizer(new ForwardedRequestCustomizer());
        }

        if(requestHeaderSize != -1){
            config.setRequestHeaderSize(requestHeaderSize);
        }
    }


    private class RequestHeaderStartOption extends DefaultStartOption {
        private RequestHeaderStartOption() {
            super("requestHeaderSize");
        }

        public String validate(String value) {
            final String illegalRequestHeaderSize = "--requestHeaderSize option requires a numerical value larger than 1";

            try {
                requestHeaderSize = Integer.parseInt(value);
            } catch (NumberFormatException e){
                return illegalRequestHeaderSize;
            }
            if(requestHeaderSize < 1){
                return illegalRequestHeaderSize;
            }
            return null;
        }

    }
}
