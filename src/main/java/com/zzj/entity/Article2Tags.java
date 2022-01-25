package com.zzj.entity;

public class Article2Tags extends SuperEntity<Article2Tags>{
    private long tagId;
    private long articleId;

    public long getTagId() {
        return tagId;
    }

    public Article2Tags setTagId(long tagId) {
        this.tagId = tagId;
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
