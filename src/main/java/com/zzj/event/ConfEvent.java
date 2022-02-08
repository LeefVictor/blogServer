package com.zzj.event;

import java.util.List;

/**
 * 配置加载的event事件
 */
public class ConfEvent {
    public List<Object> getNames() {
        return names;
    }

    public ConfEvent setNames(List<Object> names) {
        this.names = names;
        return this;
    }

    //为空时加载全部
    private List<Object> names;

}
