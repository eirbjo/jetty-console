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

package org.simplericity.jettyconsole.log4j;

import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 */
public class Log4jPlugin extends JettyConsolePluginBase {

    private File logFile;

    private StartOption logFileOption = new DefaultStartOption("logConfig") {
        @Override
        public String validate(String value) {
            logFile = new File(value);

            if(!logFile.exists()) {
                return "Could not read log4j configuration file. File does not exist: " + logFile.getAbsolutePath();
            }
            return null;
        }
    };

    public Log4jPlugin() {
        super(Log4jPlugin.class);
        addStartOptions(logFileOption);
    }

    @Override
    public void beforeStart(WebAppContext context) {
        List<String> systemClasses = new ArrayList(Arrays.asList(context.getSystemClasses()));
        systemClasses.add("org.apache.log4j.");
        context.setSystemClasses(systemClasses.toArray(new String[systemClasses.size()]));
    }

    @Override
    public void bootstrap() {
        
        if(logFile != null) {
            PropertyConfigurator.configureAndWatch(logFile.getAbsolutePath(), 10000);
        } else {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
        }
    }

}
