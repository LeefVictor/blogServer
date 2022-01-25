package com.zzj.entity;

import java.time.LocalDateTime;

public class SuperEntity<T> {
    private long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private long version;

    public long getId() {
        return id;
    }

    public T setId(long id) {
        this.id = id;
        return (T) this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public T setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return (T) this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public T setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return (T) this;
    }

    public long getVersion() {
        return version;
    }

    public T setVersion(long version) {
        this.version = version;
        return (T) this;
    }

}
