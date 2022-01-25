package com.zzj.entity;

public class SystemConf extends SuperEntity<SystemConf>{
    private String name;
    private String value;

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
