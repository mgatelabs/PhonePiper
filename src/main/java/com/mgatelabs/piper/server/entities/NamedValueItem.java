package com.mgatelabs.piper.server.entities;

/**
 * @author <a href="mailto:developer@mgatelabs.com">Michael Fuller</a>
 * Creation Date: 2/15/2018
 */
public class NamedValueItem implements Comparable<NamedValueItem> {
    private String name;
    private String value;

    public NamedValueItem(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(NamedValueItem o) {
        return value.compareTo(o.value);
    }
}
