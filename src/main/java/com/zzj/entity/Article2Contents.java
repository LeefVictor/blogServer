package com.zzj.entity;

public class Article2Contents extends SuperEntity<Article2Contents>{
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
