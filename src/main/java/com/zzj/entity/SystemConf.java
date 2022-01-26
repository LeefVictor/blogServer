package com.zzj.entity;

public class SystemConf extends SuperEntity<SystemConf>{
    private String name;
    private String value;
    private String type;

    public String getType() {
        return type;
    }

    public SystemConf setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public SystemConf setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SystemConf setValue(String value) {
        this.value = value;
        return this;
    }
}
