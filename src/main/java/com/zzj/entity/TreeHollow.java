package com.zzj.entity;

import java.io.Serializable;

public class TreeHollow extends SuperEntity<TreeHollow> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgType;

    private String content;

    private String msgId;

    private String reply;

    private String from;

    public String getFrom() {
        return from;
    }

    public TreeHollow setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getMsgType() {
        return msgType;
    }

    public TreeHollow setMsgType(String msgType) {
        this.msgType = msgType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public TreeHollow setContent(String content) {
        this.content = content;
        return this;
    }

    public String getMsgId() {
        return msgId;
    }

    public TreeHollow setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public String getReply() {
        return reply;
    }

    public TreeHollow setReply(String reply) {
        this.reply = reply;
        return this;
    }
}
