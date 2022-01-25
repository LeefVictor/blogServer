package com.zzj.entity;

public class Contents extends SuperEntity<Contents>{
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
