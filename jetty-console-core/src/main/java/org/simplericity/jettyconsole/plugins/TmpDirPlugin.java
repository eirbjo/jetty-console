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