JettyConsole Maven Plugin
=========================

Embeds Jetty in your war file, making it runnable with java -jar yourwebapp.war

How to create your standalone war
----------

Add the following configuration to your Maven webapp pom.xml:

```
<plugin>
    <groupId>org.simplericity.jettyconsole</groupId>
    <artifactId>jetty-console-maven-plugin</artifactId>
    <version>${jettyconsole.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>createconsole</goal>
            </goals>
            <configuration>
                <additionalDependencies>
                    <additionalDependency>
                        <artifactId>jetty-console-jsp-plugin</artifactId>
                    </additionalDependency>
                    <additionalDependency>
                        <artifactId>jetty-console-startstop-plugin</artifactId>
                    </additionalDependency>
                </additionalDependencies>
            </configuration>
        </execution>
    </executions>
</plugin>
```
This should output a jetty-console version in target/_artifactId_-_version_-jetty-console.war, including plugins for JSP support, and for creating Unix start/stop service scripts for your service (see plugins below)

How to run your standalone war
----------

To get help on command line options, run:

    java -jar mypp-jetty-console.war --help

To start your webapp on port 8080, run:

    java -jar myapp-jetty-console.war --port 8080 --headless

Plugins
-------

Plugin artifactId  | What does it do?
------------- | -------------
jetty-console-startstop-plugin | Running java -jar myapp.war --createStartScript creates a Unix service script + config file
jetty-console-winsrv-plugin | Running java -jar myapp.war --installWindowsService installs a Windows Service for running your service
jetty-console-jsp-plugin  | Adds support for service JSP pages
jetty-console-jettyxml-plugin | Lets you configure the Jetty Server or WebappContext from Jetty XML config files.


