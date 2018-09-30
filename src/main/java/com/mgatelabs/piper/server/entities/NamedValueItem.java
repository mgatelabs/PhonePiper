package com.mgatelabs.piper.server.entities;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 2/15/2018
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
