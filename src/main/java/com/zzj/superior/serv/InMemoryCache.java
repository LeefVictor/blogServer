package com.zzj.superior.serv;

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class InMemoryCache implements Cache {

    private Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    //非主动清除时，1小时后删除
    private com.google.common.cache.Cache<String, Object> localCache =
            CacheBuilder.newBuilder().initialCapacity(16).expireAfterAccess(Duration.of(1, ChronoUnit.HOURS)).build();

    @Override
    public Object read(String key) {
        return localCache.getIfPresent(key);
    }

    @Override
    public Object write(String key, Object obj) {
        try {
            localCache.put(key, obj);
            logger.info("写入缓存{}", key);
        } catch (Exception e) {
            logger.error("写入缓存失败", e);
        }
        return obj;
    }

    //TODO 后续加个event监听， 发送信号过来就删指定缓存


}
