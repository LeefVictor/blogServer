package com.zzj.service;

import com.zzj.dao.SystemConfDao;
import com.zzj.entity.SystemConf;
import com.zzj.enums.ConfType;
import com.zzj.event.ConfEvent;
import com.zzj.utils.DBUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ConfService {

    private volatile Map<String, String> cache = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ConfService.class);

    @Inject
    private SystemConfDao systemConfDao;

    //监听程序启动事件
    public void onStart(@Observes ConfEvent ev) {
        logger.info("加载所有配置项");
        String where = "";
        Tuple tuple = Tuple.tuple();
        if (ev.getNames() != null) {
            where = "name in (" + DBUtils.packInCondition(ev.getNames()) + ") ";
            tuple = Tuple.tuple(ev.getNames());
        }
        systemConfDao.queryWithCondition(where, tuple, "name", "value").subscribe().with(systemConf -> {
            logger.info("加载 " + systemConf.getName() + "=" + systemConf.getValue());
            cache.put(systemConf.getName(), systemConf.getValue());
        });
    }

    public Uni<String> getConfUni(String name) {
        return cache.containsKey(name) ? Uni.createFrom().item(cache.get(name)) :
                systemConfDao.queryWithName(name).onItem().transform(systemConf -> {
                    logger.info("加载 " + systemConf.getName() + "=" + systemConf.getValue());
                    cache.putIfAbsent(systemConf.getName(), systemConf.getValue());
                    return systemConf.getValue();
                });
    }

    public String getConf(String name) {
        return Optional.ofNullable(cache.get(name)).orElseThrow();
    }

    //所需的前端配置，
    public Uni<JsonObject> getDefaultConf(ConfType type) {
        return systemConfDao.queryWithCondition(" where type = ?", Tuple.of(type.name()), "name", "value")
                .collect().asList().onItem().transform(systemConfs -> {
                    JsonObject jsonObject = new JsonObject();
                    for (SystemConf conf : systemConfs) {
                        jsonObject.put(conf.getName(), conf.getValue());
                    }
                    return jsonObject;
                });
    }

    public Uni<JsonArray> allConf() {
        return systemConfDao.queryWithCondition("", Tuple.tuple(), "name", "value", "type").onItem().transform(systemConf -> {
            return new JsonObject().put("name", systemConf.getName()).put("value", systemConf.getValue()).put("type", systemConf.getType());
        }).collect().asList().onItem().transform(l -> {
            return new JsonArray(l);
        });
    }

    public Uni<Boolean> save(String name, String value, String type) {
        return systemConfDao.save(name, value, type).onItem().transform(o -> {
            cache.put(name, value);
            return Boolean.TRUE;
        });
    }


}
