package org.simplericity.jettyconsole.api;

/**
 */
public interface StartOption {

    String getName();

    String validate(String arg);

    String validate();
}
