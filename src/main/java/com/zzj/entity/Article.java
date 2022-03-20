package com.zzj.entity;

import java.io.Serializable;

public class Article extends SuperEntity<Article> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String subTitle;
    private String titleImage;
    private String articleType;
    private String author;
    private int hidden;
    private String summary;
    private int love;
    private int mainTag;
    private int commentCount;//每次增加评论都进行一次统计， 不然每条博客的评论数都要分别查询，浪费

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitleImage() {
        return titleImage;
    }

    public Article setTitleImage(String titleImage) {
        this.titleImage = titleImage;
        return this;
    }

    public String getArticleType() {
        return articleType;
    }

    public Article setArticleType(String articleType) {
        this.articleType = articleType;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Article setAuthor(String author) {
        this.author = author;
        return this;
    }

    public int getHidden() {
        return hidden;
    }

    public Article setHidden(int hidden) {
        this.hidden = hidden;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public Article setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public int getLove() {
        return love;
    }

    public Article setLove(int love) {
        this.love = love;
        return this;
    }

    public int getMainTag() {
        return mainTag;
    }

    public Article setMainTag(int mainTag) {
        this.mainTag = mainTag;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public Article setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public Article setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }
}
