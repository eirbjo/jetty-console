package org.simplericity.jettyconsole.api;

public interface Configuration {

    boolean isHeadless();

    void setHeadless(boolean headless);

    String getContextPath();

    void setContextPath(String contextPath);
}
