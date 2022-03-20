package com.zzj.entity;

import java.io.Serializable;

public class Article2Tags extends SuperEntity<Article2Tags> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tag;
    private long articleId;

    public String getTag() {
        return tag;
    }

    public Article2Tags setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public long getArticleId() {
        return articleId;
    }

    public Article2Tags setArticleId(long articleId) {
        this.articleId = articleId;
        return this;
    }
}
