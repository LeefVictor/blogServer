package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.BlackList;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackListDao extends BaseDao<BlackList> {

    public BlackListDao() {
        super(ApplicationConst.t_blackList);
    }

    public Uni<Boolean> insert(String ip) {
        return getMySQLPool().withTransaction(sqlConnection ->
                sqlConnection.preparedQuery("insert into black_list(ip) values(?)").execute(Tuple.of(ip))
                        .onItem().transform(rows -> {
                            return Boolean.TRUE;
                        })
        );
    }

    @Override
    public BlackList transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new BlackList().setId(dr.getId())
                .setIp(dr.getValue("ip"))
                .setCreateTime(dr.getCreateTime())
                ;
    }
}
