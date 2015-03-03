package org.simplericity.jettyconsole;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.webapp.*;

/**
 *
 */
public class JettyConsoleWebappContext extends WebAppContext {

    public JettyConsoleWebappContext(HandlerContainer parent, String webApp, String contextPath) {
        super(parent, webApp, contextPath);
        setConfigurations(new Configuration[]{
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration(),
                new org.eclipse.jetty.plus.webapp.PlusConfiguration(),
                new AnnotationConfiguration(),
                new JettyWebXmlConfiguration()
        });
    }
}
