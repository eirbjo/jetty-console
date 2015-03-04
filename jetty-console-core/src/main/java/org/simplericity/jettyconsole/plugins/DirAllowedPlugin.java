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

/**
 */
public class DirAllowedPlugin extends JettyConsolePluginBase {


    private boolean dirAllowed = false;

    private StartOption dirAllowedOption = new DefaultStartOption("dirAllowed") {
        @Override
        public String validate(String value) {
            if(!"true".equals(value) && !"false".equals(value)) {
                return "dirAllowed option must be 'true' or 'false'";
            }
            dirAllowed = "true".equals(value);
            return null;
        }
    };

    public DirAllowedPlugin() {
        super(DirAllowedPlugin.class);
        addStartOptions(dirAllowedOption);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.getInitParams().put("org.eclipse.jetty.servlet.Default.dirAllowed", Boolean.toString(dirAllowed));
    }
}