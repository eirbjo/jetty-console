package org.simplericity.jettyconsole;

import org.simplericity.jettyconsole.api.Configuration;

/**
 */
public class DefaultConfiguration implements Configuration {
    private boolean headless = false;
    private String contextPath = "/";

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
