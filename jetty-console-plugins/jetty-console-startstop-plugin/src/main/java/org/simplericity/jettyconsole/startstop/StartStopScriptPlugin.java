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

package org.simplericity.jettyconsole.startstop;

import org.eclipse.jetty.util.IO;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

public class StartStopScriptPlugin extends JettyConsolePluginBase {

    public StartStopScriptPlugin() {
        super(StartStopScriptPlugin.class);
        if(new File("/bin/sh").exists()) {
            addStartOptions(createStartStopScript, showStartStopScript);
        }
    }


    private StartOption createStartStopScript = new DefaultStartOption("createStartScript") {

        public String validate(String serviceName) {

            String error = validateServiceName(serviceName);

            if (error != null) {
                return error;
            }


            File war = getWarLocation();

            File sh = new File(war.getParentFile(), serviceName);

            File cnf = new File(war.getParentFile(), serviceName + ".cnf");

            File tmp = new File(war.getParentFile(), "jettyConsoleTmp");
            tmp.mkdirs();

            if (sh.exists()) {
                return "File " + sh + " already exists";
            }
            if (cnf.exists()) {
                return "File " + cnf + " already exists";
            }

            final URL startStopCnfResource = getClass().getResource("startstop.cnf");


            try {
                System.out.println("Creating start/stop script '" + sh.getName() +"'");
                String ss = produceStartScript(serviceName);
                writeToFile(sh,  new ByteArrayInputStream(ss.getBytes("ascii")));

                makeExecutable(sh);

                System.out.println("Creating default configuration file '" + cnf.getName() + "' (please edit)");
                String cnfS = IO.toString(startStopCnfResource.openStream());
                cnfS = String.format(cnfS, serviceName, System.getProperty("java.home"), System.getProperty("user.name"));
                writeToFile(cnf, new ByteArrayInputStream(cnfS.getBytes("ascii")));

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);

            return null;
        }




    };

     private StartOption showStartStopScript = new DefaultStartOption("showStartScript") {

        public String validate(String serviceName) {

            String error = validateServiceName(serviceName);

            if (error != null) {
                return error;
            }

            System.out.println(produceStartScript(serviceName));
            System.exit(0);

            return null;
        }
    };


    private String validateServiceName(String serviceName) {
        if (serviceName.length() <= 1) {
            return "Service name must be at least 2 characters long";
        }

        for (char c : serviceName.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != "-".charAt(0)) {
                return "Service name contains illegal character " + c;
            }
        }
        return null;
    }

    private String produceStartScript(String serviceName) {
        final URL startStopResource = getClass().getResource("startstop.sh");

        String ss = null;
        try {
            ss = IO.toString(startStopResource.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ss = String.format(ss, serviceName, getWarLocation().getAbsolutePath());
        return ss;
    }

    private void makeExecutable(File sh) throws InterruptedException, IOException {
        Runtime.getRuntime().exec(new String[]{"chmod", "u+x", sh.getAbsolutePath()}).waitFor();
    }

    private void writeToFile(File sh, InputStream is) {
        FileOutputStream shOut = null;
        try {
            shOut = new FileOutputStream(sh);
            IO.copy(is, shOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (shOut != null) {
                try {
                    shOut.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public File getWarLocation() {
        URL resource = getClass().getResource("/META-INF/jettyconsole/jettyconsole.properties");
        String file = resource.getFile();
        file = file.substring("file:".length(), file.indexOf("!"));
        try {
            file = URLDecoder.decode(file, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new File(file);
    }
}