package com.zzj.entity;

import java.time.LocalDateTime;

public class BlackList {

    private long id;
    private String ip;
    private LocalDateTime createTime;

    public long getId() {
        return id;
    }

    public BlackList setId(long id) {
        this.id = id;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public BlackList setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public BlackList setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }
}
