package com.zzj.entity;

public class Tags extends SuperEntity<Tags>{
    private String name;
    private int hidden;

    public String getName() {
        return name;
    }

    public Tags setName(String name) {
        this.name = name;
        return this;
    }

    public int getHidden() {
        return hidden;
    }

    public Tags setHidden(int hidden) {
        this.hidden = hidden;
        return this;
    }
}
