package org.simplericity.jettyconsole.creator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class DefaultCreator implements Creator {
    private File workingDirectory;
    private File sourceWar;
    private URL backgroundImage;
    private File destinationWar;

    private String consoleDirectory = "META-INF/jettyconsole";
    private String name;
    private List<URL> additionalDependencies;

    private static final String MAIN_CLASS = "org/simplericity/jettyconsole/JettyConsoleBootstrapMainClass";
    private URL coreDependencyUrl;
    private String properties;
    private Map<String, String> manifestEntries;

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setSourceWar(File file) {
        this.sourceWar = file;
    }

    public void setBackgroundImage(URL url) {
        this.backgroundImage = url;
    }

    public void setDestinationWar(File destinationFile) {
        this.destinationWar = destinationFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoreDependency(URL coreDependencyUrl) {
        this.coreDependencyUrl = coreDependencyUrl;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setManifestEntries(Map<String, String> manifestEntries) {
        this.manifestEntries = manifestEntries;
    }

    public void setAdditionalDependecies(List<URL> additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    public void create() throws CreatorExecutionException {
        // Make sure the working directory exists
        workingDirectory.mkdirs();

        File consoleDir = new File(workingDirectory, consoleDirectory);
        consoleDir.mkdirs();
        writeDescriptor(consoleDir);

        extractWar(sourceWar);
        copyBackgroundImage();
        writePathDescriptor(consoleDir, copyAdditionalDependencies());
        packageConsole();

    }

    /** Write a txt file with one line for each unpacked class or resource
     * from dependencies.
     */
    private void writePathDescriptor(File consoleDir, Set<String> paths) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(consoleDir, "jettyconsolepaths.txt")))){
            for (String path : paths) {
                writer.println(path);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void packageConsole() throws CreatorExecutionException {
        try {
            Jar jarArchiver = new Jar();
            jarArchiver.setProject(new Project());
            jarArchiver.setDestFile(destinationWar);
            final FileSet fileSet = new FileSet();
            fileSet.setDir(workingDirectory);
            jarArchiver.addFileset(fileSet);

            jarArchiver.addConfiguredManifest(createManifest());

            jarArchiver.execute();

        } catch (ManifestException e) {
            throw new CreatorExecutionException(e.getMessage(), e);
        }
    }

    private Manifest createManifest() throws ManifestException {
        Manifest manifest = new Manifest();
        manifest.addConfiguredAttribute(new Manifest.Attribute("Main-Class", MAIN_CLASS));

        for (Map.Entry<String, String> entry : manifestEntries.entrySet())
            manifest.addConfiguredAttribute(new Manifest.Attribute(entry.getKey(), entry.getValue()));

        return manifest;
    }

    private Set<String> copyAdditionalDependencies() throws CreatorExecutionException {

        File libDirectory = new File(workingDirectory, "META-INF/jettyconsole/lib");
        libDirectory.mkdirs();

        Set<String> tldJars = new HashSet<>();

        List<String> options = new ArrayList<>();

        for (URL url : additionalDependencies) {
            copyIntoLib(url, libDirectory, tldJars, options);
        }

        copyIntoLib(coreDependencyUrl, libDirectory, tldJars, options);

        writeOptions(options);

        unpackMainClassAndFilter(coreDependencyUrl);

        return tldJars;
    }

    private void writeOptions(List<String> options) {
        File optionsFile = new File(new File(workingDirectory, consoleDirectory), "options.txt");
        try {
            FileUtils.writeLines(optionsFile, "utf-8", options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void copyIntoLib(URL url, File libDirectory, Set<String> tldJars, List<String> options) throws CreatorExecutionException {
        String name = url.getPath().substring(url.getPath().lastIndexOf("/"));

        try {
            File file = new File(libDirectory, name);
            FileOutputStream out = new FileOutputStream(file);
            IOUtils.copy(url.openStream(), out);
            out.close();


            try (JarFile jarFile = new JarFile(file)){

                Enumeration<JarEntry> entries = jarFile.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if(entry.getName().startsWith("META-INF/") && entry.getName().endsWith(".tld")) {
                        tldJars.add(file.getName());
                    }
                    if(entry.getName().startsWith("META-INF/services")
                            && entry.getName().endsWith("JettyConsolePlugin/options.txt")) {
                        options.add(IOUtils.toString(jarFile.getInputStream(entry)));
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    private void unpackMainClassAndFilter(URL coredep) {
        try (JarInputStream in = new JarInputStream(coredep.openStream())){
            while (true) {
                JarEntry entry = in.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                if (entry.getName().startsWith(MAIN_CLASS) && entry.getName().endsWith(".class")) {
                    File file = new File(workingDirectory, entry.getName());
                    file.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(file);
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyBackgroundImage() throws CreatorExecutionException {
        try {
            if (backgroundImage == null) {
                final URL resource = getClass().getClassLoader().getResource("default-background-image.jpg");
                setBackgroundImage(resource);
            }

            IOUtils.copy(backgroundImage.openStream(), new FileOutputStream(new File(new File(workingDirectory, "META-INF/jettyconsole"), "background-image.jpg")));

        } catch (IOException e) {
            throw new CreatorExecutionException("Cannot copy background image '" + backgroundImage + "'", e);
        }
    }

    private void extractWar(File file) throws CreatorExecutionException {
        Project project = new Project();
        Expand unArchiver = new Expand();
        unArchiver.setProject(project);
        unArchiver.setSrc(file);
        unArchiver.setDest(workingDirectory);
        unArchiver.execute();

    }

    private void writeDescriptor(File consoleDir) throws CreatorExecutionException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(consoleDir, "jettyconsole.properties")))){
            writer.println("name=" + name);

            if (properties != null && properties.trim().length() > 0) {

                try {
                    Properties p = new Properties();
                    p.load(new ByteArrayInputStream(properties.getBytes()));
                    writer.println(properties);
                } catch (IOException e) {
                    throw new CreatorExecutionException("Can't parse properties: [" + properties + "]", e);
                }
            }
        } catch (FileNotFoundException e) {
            throw new CreatorExecutionException("Cannot write to descriptor file", e);
        }
    }
}
