package org.simplericity.jettyconsole.api;

/**
 */
public class DefaultStartOption implements StartOption {
    private String name;

    public DefaultStartOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String validate() {
        return null;
    }

    public String validate(String value) {
        return null;
    }

}
