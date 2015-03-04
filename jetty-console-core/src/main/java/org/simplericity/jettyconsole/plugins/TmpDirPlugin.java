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
import org.simplericity.jettyconsole.api.Settings;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 */
public class TmpDirPlugin extends JettyConsolePluginBase {
    private Settings settings;

    private File tmpDir;

    private StartOption tmpDirOption = new DefaultStartOption("tmpDir") {
        @Override
        public String validate(String value) {
            tmpDir = new File(value);
            if(!tmpDir.exists()) {
                return "tmpDir does not exist: " + value;
            }
            if(!tmpDir.isDirectory()) {
                return "tmpDir is not a directory: " + value;
            }
            return  null;
        }
    };
    public TmpDirPlugin(Settings settings) {
        super(TmpDirPlugin.class);
        this.settings = settings;
        addStartOptions(tmpDirOption);
    }

}