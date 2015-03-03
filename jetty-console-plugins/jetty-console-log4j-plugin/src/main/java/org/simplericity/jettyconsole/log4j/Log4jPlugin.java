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
