package com.zzj.entity;

import java.io.Serializable;

public class Article2Contents extends SuperEntity<Article2Contents> implements Serializable {
    private static final long serialVersionUID = 1L;
    private long articleId;
    private long contentId;

    public long getArticleId() {
        return articleId;
    }

    public Article2Contents setArticleId(long articleId) {
        this.articleId = articleId;
        return this;
    }

    public long getContentId() {
        return contentId;
    }

    public Article2Contents setContentId(long contentId) {
        this.contentId = contentId;
        return this;
    }
}
