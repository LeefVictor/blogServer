package com.zzj.entity;

public class Article2Tags extends SuperEntity<Article2Tags> {
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
