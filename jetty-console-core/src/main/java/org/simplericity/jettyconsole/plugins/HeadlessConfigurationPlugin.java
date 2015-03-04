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
import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;

/**
 */
public class HeadlessConfigurationPlugin extends JettyConsolePluginBase {

    private boolean headless;

    private StartOption headLessOption = new DefaultStartOption("headless") {
        @Override
        public String validate() {
            headless = true;
            return null;
        }
    };
    public HeadlessConfigurationPlugin() {
        super(HeadlessConfigurationPlugin.class);
        addStartOptions(headLessOption);
    }


    @Override
    public void configureConsole(Configuration configuration) {
        if(headless) {
            configuration.setHeadless(true);
        }
    }

}
