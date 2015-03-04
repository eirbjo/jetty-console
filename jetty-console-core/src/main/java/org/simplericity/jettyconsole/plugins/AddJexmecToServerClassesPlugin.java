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
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;

/**
 */
public class AddJexmecToServerClassesPlugin extends JettyConsolePluginBase {
    public AddJexmecToServerClassesPlugin() {
        super(AddJexmecToServerClassesPlugin.class.getName());
    }

    @Override
    public void beforeStart(WebAppContext context) {
        context.addServerClass("org.kantega.jexmec.");
    }
}
