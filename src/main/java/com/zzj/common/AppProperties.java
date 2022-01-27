package com.zzj.common;

import javax.enterprise.context.ApplicationScoped;

//全局配置类
@ApplicationScoped
public class AppProperties {

    //或者通过订阅队列触发
    //@Scheduled(cron = "0/1 * * * * ? ")
    void loadSystemConf(){
        System.out.println("loading .....");
    }
}
