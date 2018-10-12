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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.simplericity.jettyconsole.creator.Creator;
import org.simplericity.jettyconsole.creator.CreatorExecutionException;
import org.simplericity.jettyconsole.creator.DefaultCreator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

/**
 * This plugin creates a jar embedding Jetty and a webapp. Double clicking the jar starts a graphical console
 * that lets the user select a port and start/stop the webapp.
 *
 * This is intended to be an easy way to distribute webapps for review/testing etc without the need to distribute and/or
 * configure a servlet container.
 */
@Mojo(name = "createconsole", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution =  ResolutionScope.RUNTIME)
public class CreateDescriptorMojo
    extends AbstractMojo
{
    /**
     * Archive configuration to read MANIFEST.MF entries from.
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;


    /**
     * Name of the console project. Used in frame title and dialogs
     *
     * @parameter default-value="${project.name} ${project.version}"
     */
    private String name;

    /**
     * Background image to use for the console instead of the default.
     * @parameter
     */
    private File backgroundImage;

    /**
     * The Maven Project Object
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * Working directory where dependencies are unpacked before repackaging
     * @parameter default-value="${project.build.directory}/console-work"
     */
    private File workingDirectory;

    /**
     * Classifier for generated console war
     * @parameter default-value="jetty-console"
     */
    private String jettyConsoleClassifier;

    /**
     * Destination file for the packaged console
     * @parameter default-value="${project.build.directory}/${project.build.finalName}-jetty-console.war"
     */
    private File destinationFile;

    /**
     * War artifact
     *
     * @parameter
     */
    private Dependency warArtifact;

    /**
     * Any additional dependencies to include on the Jetty console class path
     * @parameter
     */
    private List<AdditionalDependency> additionalDependencies;

    /**
     * Maven ProjectHelper
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;


    /** @component */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    /** @component */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**@parameter expression="${localRepository}" */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    private java.util.List remoteRepositories;

    /** @component */
    private ArtifactMetadataSource artifactMetadataSource;

    private Creator creator;


    /**
     * @parameter default-value="true"
     */
    private boolean attachWithClassifier;

    /**
     * @parameter default-value=""
     */
    private String properties;
    private Properties props;

    public void execute()
            throws MojoExecutionException, MojoFailureException {

        getProps();

        // Check that the background image exists
        if(backgroundImage != null && !backgroundImage.exists()) {
            throw new MojoExecutionException("The 'backgroundImage' file you specified does not exist");
        }

        Artifact warArtifact = getWarArtifact();

        creator = new DefaultCreator();

        creator.setWorkingDirectory(workingDirectory);

        creator.setSourceWar(warArtifact.getFile());

        creator.setDestinationWar(destinationFile);

        creator.setName(name);

        creator.setProperties(properties);

        creator.setManifestEntries(archive.getManifestEntries());

        if(backgroundImage != null && backgroundImage.exists() && backgroundImage.isFile()) {
            try {
                creator.setBackgroundImage(backgroundImage.toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        setDeps();

        try {
            getLog().info("Creating JettyConsole package");
            creator.create();
        } catch (CreatorExecutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if(attachWithClassifier) {
            attachArtifact();
        } else {
            project.getArtifact().setFile(destinationFile);
        }

    }

    private void setDeps() throws MojoExecutionException, MojoFailureException {
        List<File> deps = getDependencies();
        List<URL> additionalDeps = new ArrayList<URL>();
        for (File file : deps) {
            if (file.getName().contains("jetty-console-core")) {
                try {
                    creator.setCoreDependency(file.toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            } else {
                try {
                    additionalDeps.add(file.toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }

        Set<Artifact> artifacts = new HashSet<Artifact>();

        if (additionalDependencies != null) {
            for (AdditionalDependency dep : additionalDependencies) {

                String groupId = dep.getGroupId();
                String version = dep.getVersion();
                if (groupId == null && version == null) {
                    groupId = "org.simplericity.jettyconsole";
                    version = props.getProperty("version");
                }

                artifacts.add(artifactFactory.createDependencyArtifact(groupId, dep.getArtifactId(), VersionRange.createFromVersion(version), dep.getType(), dep.getClassifier(), dep.getScope()));
            }
        }
        try {
            Set<Artifact> slf4jBindingArtifacts = new HashSet<Artifact>();

            if (artifacts.size() > 0 ) {
                ArtifactResolutionResult result = resolver.resolveTransitively(artifacts, project.getArtifact(), localRepository, remoteRepositories, artifactMetadataSource, new ScopeArtifactFilter("runtime"));
                for (Artifact artifact : (Set<Artifact>) result.getArtifacts()) {
                    if (!"slf4j-api".equals(artifact.getArtifactId())) {
                        additionalDeps.add(artifact.getFile().toURL());
                        if(hasSlf4jBinding(artifact.getFile())) {
                            slf4jBindingArtifacts.add(artifact);
                        }
                    }
                }
            }
            if (project.getPackaging().equals("jar")) {
                additionalDeps.add(project.getArtifact().getFile().toURL());
            }

            if (slf4jBindingArtifacts.isEmpty()) {
                String slf4jVersion = props.getProperty("slf4jVersion");
                final Artifact slf4jArtifact = artifactFactory.createDependencyArtifact("org.slf4j", "slf4j-simple", VersionRange.createFromVersion(slf4jVersion), "jar", null, "runtime");
                resolver.resolve(slf4jArtifact, remoteRepositories, localRepository);
                additionalDeps.add(slf4jArtifact.getFile().toURL());
            } else if(slf4jBindingArtifacts.size() > 1) {
                throw new MojoFailureException("You have dependencies on multiple SJF4J artifacts, please select a single one: " + slf4jBindingArtifacts);
            }
        } catch (ArtifactResolutionException | MalformedURLException | ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        creator.setAdditionalDependecies(additionalDeps);
    }

    /**
     * Returns true if the given jar file contains an SLF4J binding
     * @param file the file
     * @return true if the jar file contains an SLF4J binding
     */
    private boolean hasSlf4jBinding(File file) throws MojoExecutionException {
        try (JarFile jarFile = new JarFile(file)){
            return jarFile.getEntry("org/slf4j/impl/StaticLoggerBinder.class") != null;
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


    private List<File> getDependencies() throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<>();
        String version = props.getProperty("version");
        String jettyVersion = props.getProperty("jettyVersion");

        getLog().info("Resolving dependencies for version " + version +" of jetty-console-core");

        artifacts.add(artifactFactory.createDependencyArtifact("org.simplericity.jettyconsole", "jetty-console-core", VersionRange.createFromVersion(version), "jar", null, "runtime"));

        List<File> artifactFiles = new ArrayList<File>();
        try {
            ArtifactResolutionResult result = resolver.resolveTransitively(artifacts, project.getArtifact(), remoteRepositories, localRepository, artifactMetadataSource);
            for(Artifact artifact : (Set<Artifact>) result.getArtifacts()) {
                artifactFiles.add(artifact.getFile());
            }
            return artifactFiles;

        } catch (ArtifactResolutionException | ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void getProps() throws MojoExecutionException {
        props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("pluginversion.properties"));
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


    private void attachArtifact() {
        projectHelper.attachArtifact(project, "war", jettyConsoleClassifier, destinationFile);
    }

    public Artifact getWarArtifact() throws MojoExecutionException {

        if (warArtifact != null) {
            try {
                Artifact artifact = artifactFactory.createDependencyArtifact(warArtifact.getGroupId(), warArtifact.getArtifactId(),
                        VersionRange.createFromVersion(warArtifact.getVersion()), warArtifact.getType(), warArtifact.getClassifier(), "runtime");
                resolver.resolve(artifact, remoteRepositories, localRepository);
                return artifact;

            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Unable to resolve war artifact (" + e.getMessage() + ")", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException("Unable to find war artifact (" + e.getMessage() + ")", e);
            }
        }

        if(project.getArtifact().getFile().getName().endsWith(".war")) {
            return project.getArtifact();
        }

        List<Artifact> wars = new ArrayList<Artifact>();
        for (Iterator i = project.getArtifacts().iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
            if ("war".equals(artifact.getType())) {
                wars.add(artifact);
            }
        }
        if(wars.size() == 0) {
            throw new MojoExecutionException("Can't find any war dependency");
        } else if(wars.size() > 1) {
            throw new MojoExecutionException("Found more than one war dependency, can't continue");
        } else {
            return wars.get(0);
        }

    }

}
