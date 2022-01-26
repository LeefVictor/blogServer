package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.SystemConf;
import io.vertx.mutiny.sqlclient.Row;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemConfDao extends BaseDao<SystemConf> {


    public SystemConfDao() {
        super(ApplicationConst.t_system_conf);
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
