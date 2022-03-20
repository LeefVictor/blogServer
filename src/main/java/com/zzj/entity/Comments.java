package com.zzj.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Comments extends SuperEntity<Comments> implements Serializable {
    private static final long serialVersionUID = 1L;
    private long articleId;
    private String nick;
    private String email;
    private String content;
    private int hidden;
    private String replyContent;
    private LocalDateTime replyTime;
    private int isSharp;

    public long getArticleId() {
        return articleId;
    }

    public Comments setArticleId(long articleId) {
        this.articleId = articleId;
        return this;
    }

    public String getNick() {
        return nick;
    }

    public Comments setNick(String nick) {
        this.nick = nick;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Comments setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Comments setContent(String content) {
        this.content = content;
        return this;
    }

    public int getHidden() {
        return hidden;
    }

    public Comments setHidden(int hidden) {
        this.hidden = hidden;
        return this;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public Comments setReplyContent(String replyContent) {
        this.replyContent = replyContent;
        return this;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public Comments setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
        return this;
    }

    public int getIsSharp() {
        return isSharp;
    }

    public Comments setIsSharp(int isSharp) {
        this.isSharp = isSharp;
        return this;
    }
}
