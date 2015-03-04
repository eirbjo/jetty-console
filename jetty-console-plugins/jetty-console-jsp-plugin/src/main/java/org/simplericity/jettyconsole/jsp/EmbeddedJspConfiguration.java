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

package org.simplericity.jettyconsole.jsp;

import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Set TLD jars which were found when packaging the war.
 *
 * Configure Jasper's system classPath to only include Jasper/JSP/Servlet related jars
 *
 */
public class EmbeddedJspConfiguration extends AbstractConfiguration {

    @Override
    public void postConfigure(WebAppContext context) throws Exception {

        rewriteJasperSystemClasspath(context);

    }

    private void rewriteJasperSystemClasspath(WebAppContext context) throws NoSuchFieldException, IllegalAccessException {
        JettyJspServlet jspServlet = (JettyJspServlet) context.getServletHandler().getServlet("jsp").getServletInstance();
        // the options field is in JettyJspServlet's parent, JspServlet
        Class<?> JspServletClass = jspServlet.getClass().getSuperclass();
        Field optionsField = JspServletClass.getDeclaredField("options");
        optionsField.setAccessible(true);
        EmbeddedServletOptions options = (EmbeddedServletOptions) optionsField.get(jspServlet);

        String sysClassPath = options.getProperty("com.sun.appserv.jsp.classpath");

        String filteredSysClassPath = filterSysClassPath(sysClassPath);
        options.setProperty("com.sun.appserv.jsp.classpath", filteredSysClassPath);
    }

    private String filterSysClassPath(String sysClassPath) {
        if(sysClassPath == null) {
            return sysClassPath;
        } else {
            List<String> paths = new ArrayList<>();
            for(String path : sysClassPath.split(File.pathSeparator)) {
                if(isJspCompilePath(path)) {
                    paths.add(path);
                }
            }
            StringBuilder newPath = new StringBuilder();

            for(String path : paths) {
                if(newPath.length() != 0) {
                    newPath.append(File.pathSeparator);
                }
                newPath.append(path);
            }

            return newPath.toString();
        }


    }

    private boolean isJspCompilePath(String path) {
        String fileName = path.substring(path.lastIndexOf(File.separator)+1);
        return !path.contains(File.separator + "condi" + File.separator) ||
                fileName.startsWith("javax") ||
                fileName.contains("apache-jsp") ||
                fileName.contains("apache-el") ||
                fileName.contains("jasper") ||
                fileName.contains("taglibs-standard");

    }

}
