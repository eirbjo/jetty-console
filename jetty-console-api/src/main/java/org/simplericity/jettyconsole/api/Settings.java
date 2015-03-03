package org.simplericity.jettyconsole.api;

import java.util.Collection;

/**
 */
public interface Settings {
    String getProperty(String name);
    Collection<String> getPropertyNames();
}
