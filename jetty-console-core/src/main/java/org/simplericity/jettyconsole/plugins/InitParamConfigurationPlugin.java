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

import org.eclipse.jetty.webapp.WebAppContext;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class InitParamConfigurationPlugin extends JettyConsolePluginBase {


    private Map<String, String> initParams = new HashMap<String, String>();

    private StartOption initParamOption = new DefaultStartOption("initParam") {
        @Override
        public String validate(String value) {
            if(!value.contains("=")) {
                return "--initParam must be specified as 'name=value'";
            }
            String name = value.substring(0, value.indexOf("="));
            String val = value.substring(value.indexOf("=")+1);
            if(initParams.containsKey(name)) {
                return "--initParam " + name + " already specified as " + initParams.get(name);
            }
            initParams.put(name, val);
            return null;
        }
    };
    public InitParamConfigurationPlugin() {
        super(InitParamConfigurationPlugin.class);
        addStartOptions(initParamOption);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.getInitParams().putAll(initParams);
    }
}
