package com.mgatelabs.piper.server.entities;

/**
 * @author <a href="mailto:developer@mgatelabs.com">Michael Fuller</a>
 * Creation Date: 2/15/2018
 */
public class NamedValueDescriptionItem implements Comparable<NamedValueDescriptionItem> {
    private String name;
    private String value;
    private String description;

    public NamedValueDescriptionItem(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(NamedValueDescriptionItem o) {
        return value.compareTo(o.value);
    }
}
