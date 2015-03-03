package org.simplericity.jettyconsole;

import org.simplericity.jettyconsole.api.Configuration;

public interface JettyManager {
    public void startServer(Configuration configuration);
    public void stopServer();

    interface JettyListener {
        void serverStopped();
    }
    void addListener(JettyListener listener);

    void shutdown();
}
