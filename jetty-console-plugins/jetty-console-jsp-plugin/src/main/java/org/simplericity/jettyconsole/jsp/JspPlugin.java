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

package org.simplericity.jettyconsole.jsp;

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class JspPlugin extends JettyConsolePluginBase {


    public JspPlugin() {
        super(JspPlugin.class);
    }

    @Override
    public void beforeStart(WebAppContext context) {

        context.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*/taglibs-standard-impl-[^/]*\\.jar$");
        Configuration[] current = context.getConfigurations();

        List<Configuration> cfn = new ArrayList<Configuration>(Arrays.asList(current));
        cfn.add(new EmbeddedJspConfiguration());
        context.setConfigurations(cfn.toArray(new Configuration[cfn.size()]));
    }
}
