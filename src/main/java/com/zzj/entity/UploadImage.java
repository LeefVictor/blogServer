package com.zzj.entity;

import java.io.Serializable;

public class UploadImage extends SuperEntity<UploadImage> implements Serializable {
    private static final long serialVersionUID = 1L;

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
