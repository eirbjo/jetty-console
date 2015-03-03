package org.simplericity.jettyconsole.winsrv;

import org.eclipse.jetty.util.IO;
import org.simplericity.jettyconsole.api.DefaultStartOption;
import org.simplericity.jettyconsole.api.JettyConsolePluginBase;
import org.simplericity.jettyconsole.api.StartOption;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

/**
 */
public class WinSrvPlugin extends JettyConsolePluginBase {

    private StartOption installWinSrv = new DefaultStartOption("installWindowsService") {
        @Override
        public String validate(String serviceName) {
            try {


                final File warLocation = getWarLocation();

                final File wd = warLocation.getParentFile();

                final File bin = new File(wd, "bin");
                bin.mkdirs();

                final File logs = new File(wd, "logs");
                logs.mkdirs();

                final File temp  = new File(wd, "temp");
                temp.mkdirs();

                final File stdOut = new File(logs, "stdout.txt");
                final File stdErr = new File(logs, "stderr.txt");


                File exeFile = unpackExeFile("jettyconsole.exe", new File(bin, serviceName +".exe"));
                unpackExeFile("jettyconsolew.exe", new File(bin, serviceName +"w.exe"));
                String[] cmd = {
                        exeFile.getAbsolutePath(),
                        "//IS//" + serviceName,
                        "--Classpath=" + warLocation.getAbsolutePath(),
                        "--Jvm=auto",

                        "--StartMode=jvm",
                        "--StartClass=JettyConsoleBootstrapMainClass",
                        "--StartPath=" + wd.getAbsolutePath(),
                        "--StartMethod=start",
                        "--StartParams=--headless",

                        "--StopMode=jvm",
                        "--StopClass=JettyConsoleBootstrapMainClass",
                        "--StopPath=" + wd.getAbsolutePath(),
                        "--StopMethod=stop",


                        "--LogPath="+logs.getAbsolutePath(),
                        "--LogLevel=INFO",
                        "--LogPrefix=service.log",
                        "--StdOutput=" +stdOut.getAbsolutePath(),
                        "--StdError=" + stdErr.getAbsolutePath()
                };

                System.out.println("Installing Windows Service " + serviceName +"..");
                final Process process = Runtime.getRuntime().exec(cmd);
                final int returnValue = process.waitFor();
                if(returnValue != 0) {
                    System.out.println("Process returned " + returnValue);
                }
                System.out.println("Finished installing service " + serviceName);
                System.exit(0);
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    };

    private StartOption removeWinSrv = new DefaultStartOption("removeWindowsService") {
        @Override
        public String validate(String serviceName) {
            try {


                final File warLocation = getWarLocation();
                final File wd = warLocation.getParentFile();
                final File bin = new File(wd, "bin");
                bin.mkdirs();
                File exeFile = unpackExeFile("jettyconsole.exe", new File(bin, serviceName +".exe"));

                String[] cmd = {
                        exeFile.getAbsolutePath(),
                        "//DS//" + serviceName
                };

                System.out.println("Removing Windows Service " +serviceName);
                final Process process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                System.out.println("Finished removing service " + serviceName);
                System.exit(0);
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    };

    public WinSrvPlugin() {
        super(WinSrvPlugin.class);
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            addStartOptions(installWinSrv, removeWinSrv);
        }
    }

    /**
     * Return a File pointing to the location of the Jar file this Main method is executed from.
     * @return
     */
    public File getWarLocation() {
        URL resource = WinSrvPlugin.class.getResource("/META-INF/jettyconsole/jettyconsole.properties");
        String file = resource.getFile();
        file = file.substring("file:".length(), file.indexOf("!"));
        try {
            file = URLDecoder.decode(file, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return new File(file);
    }

    private File unpackExeFile(String resourceName, File file) throws IOException {
            System.out.println("Install service");
            URL resource = getClass().getResource(resourceName);
            FileOutputStream out = new FileOutputStream(file);
            final InputStream in = resource.openStream();
            IO.copy(in, out);
            out.close();
            in.close();
            return file;
        }
}
