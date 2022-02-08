package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.SystemConf;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemConfDao extends BaseDao<SystemConf> {

    private final String insertConf =
            """
                    INSERT INTO `system_conf` ( `name`, `value`, `type` )
                    VALUES(?,?,?)
                    ON DUPLICATE KEY UPDATE value=?,type=?
                    """;

    public SystemConfDao() {
        super(ApplicationConst.t_system_conf);
    }

    public Uni<SystemConf> queryWithName(String name) {
        return queryOneWithCondition("name=? ", Tuple.of(name), "value");
    }

    public Uni save(String name, String value, String type) {
        return insertOne(insertConf, Tuple.of(name, value, type, value, type));
    }


    @Override
    public SystemConf transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new SystemConf().setType(dr.getValue("type"))
                .setName(dr.getValue("name"))
                .setValue(dr.getValue("value"))
                .setId(dr.getId())
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setVersion(dr.getVersion());
    }
}
