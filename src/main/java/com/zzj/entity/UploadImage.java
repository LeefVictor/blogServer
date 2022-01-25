package com.zzj.entity;

public class UploadImage extends SuperEntity<UploadImage>{

    private String name;
    private String wholeUrl;

    public String getName() {
        return name;
    }

    public UploadImage setName(String name) {
        this.name = name;
        return this;
    }

    public String getWholeUrl() {
        return wholeUrl;
    }

    public UploadImage setWholeUrl(String wholeUrl) {
        this.wholeUrl = wholeUrl;
        return this;
    }
}
