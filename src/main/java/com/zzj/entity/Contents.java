package com.zzj.entity;

import java.io.Serializable;

public class Contents extends SuperEntity<Contents> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String contentType;
    private String content;

    public String getContentType() {
        return contentType;
    }

    public Contents setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Contents setContent(String content) {
        this.content = content;
        return this;
    }
}
