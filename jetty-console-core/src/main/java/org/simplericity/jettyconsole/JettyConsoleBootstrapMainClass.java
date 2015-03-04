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

package org.simplericity.jettyconsole;

import org.simplericity.jettyconsole.plugins.ConnectorConfigurationPlugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * The purpose of this class is to be the Main-Class of a JAR file.
 * On startup, the class will extract the "real" application by extracing the jar files
 * in /META-INF/jettyconsole/lib into a temporary directory, setting up a class loader
 * and executing the {@link org.simplericity.jettyconsole.JettyConsoleStarter}'s main method.
 *
 * On shutdown, this class will remove the temporatily extracted jar files.
 */
public class JettyConsoleBootstrapMainClass implements Runnable {

    private static JettyConsoleBootstrapMainClass instance;
    private ClassLoader cl;
    private Runnable shutdown;

    public static void start(String[] arguments) throws Exception {
        System.out.println("JettyConsole Windows Service starting");
        instance = new JettyConsoleBootstrapMainClass();
        instance.setupWindowsService();
        instance.run(arguments);
        synchronized (instance) {
            instance.wait();
        }
        instance.shutdown.run();
        System.out.println("JettyConsole Windows Service main thread exiting");
    }

    private void setupWindowsService() {
        File tempDirectory = new File(".", "temp");
        System.setProperty("java.io.tmpdir", tempDirectory.getAbsolutePath());
        tempDirectory.mkdirs();
        for(File child : tempDirectory.listFiles()) {
            delete(child);
        }
    }

    private void delete(File file) {
        if(file.isDirectory()) {
            for(File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    public static void stop(String[] args) {
        System.out.println("JettyConsole Windows Service stopping");
        synchronized (instance) {
            instance.notifyAll();
        }
    }


    /**
     * Create an instance of Main and invoke the {@link JettyConsoleBootstrapMainClass#run()} method.
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new JettyConsoleBootstrapMainClass().run(arguments);
    }

    /**
     * Extract jar files, set up a class loader and execute {@link org.simplericity.jettyconsole.JettyConsoleStarter}'s main method.
     * @param arguments
     * @throws Exception
     */
    private void run(String[] arguments) throws Exception {

        if(isHelpRequested(arguments)) {
            usage();
        }

        validateArguments(arguments);

        checkTemporaryDirectory(arguments);


        addShutdownHook();

        File war = getWarLocation();

        File tempDirectory = createTempDirectory(war.getName(), getPort(arguments));

        cl = createClassLoader(war, tempDirectory);

        Thread.currentThread().setContextClassLoader(cl);

        startJettyConsole(cl, arguments, tempDirectory);
    }

    private void validateArguments(String[] args) {
        Map<String, Option> optionsByName = getOptionsByName();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if (!arg.startsWith("--")) {
                usage("Options must start with '--': " + args);
            }
            arg = arg.substring(2);

            Option option = optionsByName.get(arg.toLowerCase());
            if (option == null) {
                usage("Unknown option: '" + arg +"'");
            } else if (option.getSample() != null) {

                if(i == args.length - 1) {
                    usage("--" + arg +" option requires a value");
                }
                i++;
            }
        }

    }

    public static Map<String, Option> getOptionsByName() {
        Map<String, Option> optionsByName = new HashMap<String, Option>();

        for(Option option : readOptions()) {
            optionsByName.put(option.getName().toLowerCase(), option);
        }
        return optionsByName;
    }

    private File createTempDirectory(String name, int port) {
        File javaTemp = new File(System.getProperty("java.io.tmpdir"));

        File temp = new File(javaTemp, name +"_" + port);

        temp.deleteOnExit();

        temp.mkdirs();

        return temp;
    }

    private int getPort(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if("--port".equals(argument)) {
                if(i +1 == arguments.length ){
                    err("--port option requires a value");
                }
                try {
                    return Integer.parseInt(arguments[i + 1]);
                } catch (NumberFormatException e) {
                    err("--port value must be an integer");
                }
                i+=2;
            }
        }
        return ConnectorConfigurationPlugin.DEFAULT_PORT;

    }

    private void checkTemporaryDirectory(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if("--tmpDir".equals(argument)) {
                if(i == arguments.length -1) {
                    err("--tmpDir must take a path as an argument");
                } else {
                    File tmpDir = new File(arguments[i+1]);
                    if(!tmpDir.exists()) {
                        err("tmpDir does not exist: " + tmpDir);
                    } else if(!tmpDir.isDirectory()) {
                        err("tmpDir is not a directory: " + tmpDir);
                    } else {
                        System.setProperty("java.io.tmpdir", tmpDir.getAbsolutePath());
                    }

                }
            }
        }
    }

    private void err(String msg) {
        System.err.println();
        System.err.println("ERROR: " +msg);
        System.exit(0);
    }

    /**
     * Load {@link org.simplericity.jettyconsole.JettyConsoleStarter} and execute its main method.
     * @param cl The class loader to use for loading {@link JettyConsoleStarter}
     * @param arguments the arguments to pass to the main method
     */
    private void startJettyConsole(ClassLoader cl, String[] arguments, File tempDirectory) {
        try {
            Class starterClass = cl.loadClass("org.simplericity.jettyconsole.JettyConsoleStarter");
            starterClass.getField("jettyWorkDirectory").set(null, tempDirectory);
            Method main = starterClass.getMethod("main", arguments.getClass());
            main.invoke(null, new Object[] {arguments});
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopJettyConsole(ClassLoader cl) {
        if(cl != null) {
            try {
                Class starterClass = cl.loadClass("org.simplericity.jettyconsole.JettyConsoleStarter");
                Method main = starterClass.getMethod("stop");
                main.invoke(null);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Create a URL class loader containing all jar files in the given directory
     *
     * @param warFile the war file to look for libs dirs in
     * @return
     */
    private ClassLoader createClassLoader(File warFile, File tempDirectory) {
        try {

            File condiLibDirectory = new File(tempDirectory, "condi");
            condiLibDirectory.mkdirs();
            File jettyWebappDirectory = new File(tempDirectory, "webapp");
            jettyWebappDirectory.mkdirs();


            List<URL> urls = new ArrayList<>();

            JarInputStream in = new JarInputStream(new FileInputStream(warFile));

            while(true) {
                JarEntry entry = in.getNextJarEntry();
                if(entry == null) {
                    break;
                }
                String name = entry.getName();
                String prefix = "META-INF/jettyconsole/lib/";
                if(!entry.isDirectory()) {
                    if( name.startsWith(prefix) ) {
                        String simpleName = name.substring(name.lastIndexOf("/")+1);
                        File file = new File(condiLibDirectory, simpleName);
                        unpackFile(in, file);
                        urls.add(file.toURI().toURL());
                    } else if(!name.startsWith("META-INF/jettyconsole")
                            && !name.contains("JettyConsoleBootstrapMainClass")) {
                        File file = new File(jettyWebappDirectory, name);
                        file.getParentFile().mkdirs();
                        unpackFile(in, file);
                    }
                }
            }
            in.close();
            return new URLClassLoader(urls.toArray(new URL[urls.size()]), JettyConsoleBootstrapMainClass.class.getClassLoader());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown = this));
    }

    /**
     * Delete the temporary directory on shutdown.
     */
    public void run() {
        stopJettyConsole(cl);
    }

    /**
     * Return a File pointing to the location of the Jar file this Main method is executed from.
     * @return
     */
    public static File getWarLocation() {
        URL resource = JettyConsoleBootstrapMainClass.class.getResource("/META-INF/jettyconsole/jettyconsole.properties");
        String file = resource.getFile();
        file = file.substring("file:".length(), file.indexOf("!"));
        try {
            file = URLDecoder.decode(file, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new File(file);
    }
    /**
     * Write the contents of an InputStream to a file
     * @param in the input stream to read
     * @param file the File to write to
     */
    private static void unpackFile(InputStream in, File file) {
        byte[] buffer = new byte[4096];

        try {
            OutputStream out = new FileOutputStream(file);
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isHelpRequested(String[] args) {

        for (String arg : args) {
            if ("--help".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public static void usage() {
        usage(null);
    }

    public static void usage(String args) {
        PrintStream err = System.err;

        if (args != null) {
            err.println("ERROR: " + args);
            err.println();
        }
        err.println("Usage: java -jar " + getWarLocation().getName() + " [--option value] [--option2 value2] [--option3]");

        err.println();

        Map<String, List<Option>> options = createOptionsBySectionMap();

        Set<String> sortedSections = new TreeSet<String>(new Comparator<String>() {
            public int compare(String s, String s1) {
                if (s.equals("Options")) {
                    return -1;
                } else if (s1.equals("Options")) {
                    return 1;
                } else {
                    return s.compareTo(s1);
                }
            }
        });
        sortedSections.addAll(options.keySet());

        for (String section : sortedSections) {
            err.println(section + ":");
            for (Option option : options.get(section)) {
                StringBuilder line = new StringBuilder(" --" + option.getName() + " ");


                if (option.getSample() != null) {
                    line.append(option.getSample()).append(" ");
                }
                while (line.length() < 21) {
                    line.append(" ");
                }

                line.append("- ").append(option.getDescription());

                err.println(line);

            }
            err.println();
        }

        System.exit(1);
    }

    private static Map<String, List<Option>> createOptionsBySectionMap() {
        Map<String, List<Option>> map = new TreeMap<String, List<Option>>();

        for(Option option : readOptions()) {
            if(!map.containsKey(option.getSection())) {
                map.put(option.getSection(), new ArrayList<Option>());
            }

            map.get(option.getSection()).add(option);
        }
        return map;
    }

    private static List<Option> readOptions() {
        return parseOptions(readOptionLines(JettyConsoleBootstrapMainClass.class.getClassLoader()
                .getResource("META-INF/jettyconsole/options.txt")));
    }

    public static class Option {

        private final String name;
        private String sample;
        private String description;
        private String section;

        public Option(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getSample() {
            return sample;
        }

        public String getDescription() {
            return description;
        }

        public void setSample(String sample) {
            this.sample = sample;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }
    }

    public static String OPTION_NAME = "option:";
    public static String SAMPLE = "sample:";
    public static String DESCRIPTION = "desc:";
    public static String SECTION = "section:";

    static List<String> readOptionLines(URL resource) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), "utf-8"));

            List<String> lines = new ArrayList<String>();

            String line;
            while((line = reader.readLine()) != null) {
                lines.add(line);
            }

            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    static List<Option> parseOptions(List<String> lines) {
        List<Option> options = new ArrayList<>();

        Option current = null;
        for (String line : lines) {
            line = line.trim();
            if(line.startsWith(OPTION_NAME)) {
                String name = line.substring(OPTION_NAME.length()).trim();
                options.add(current = new Option(name));
            } else if (line.startsWith(SAMPLE)) {
                String sample = line.substring(SAMPLE.length()).trim();
                current.setSample(sample);
            } else if (line.startsWith(DESCRIPTION)) {
                String description = line.substring(DESCRIPTION.length()).trim();
                current.setDescription(description);
            } else if (line.startsWith(SECTION)) {
                String section = line.substring(SECTION.length()).trim();
                current.setSection(section);
            }
        }
        return options;
    }
}
