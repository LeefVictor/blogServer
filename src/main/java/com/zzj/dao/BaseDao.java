package com.zzj.dao;

import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class BaseDao<T> {

    private final Logger logger = LoggerFactory.getLogger(BaseDao.class);

    private String tableName;

    @Inject
    private MySQLPool mySQLPool;

    public BaseDao(String tableName) {
        this.tableName = tableName;
    }

    public MySQLPool getMySQLPool() {
        return mySQLPool;
    }

    public BaseDao setMySQLPool(MySQLPool mySQLPool) {
        this.mySQLPool = mySQLPool;
        return this;
    }

    public Uni<Integer> count(String where, Tuple value){
        return mySQLPool.preparedQuery(getCountSql(where, Optional.empty())).execute(value)
                .onItem().transform(this::transToCount);
    }

    public Uni<T> queryWithId(long id, String... columns) {
        String where = " where id = ? ";
        return mySQLPool.preparedQuery(getQuerySql(where, columns)).execute(Tuple.of(id))
                .onItem().transform(rows -> {
                    List<T> res = new ArrayList<>();
                    for (Row row : rows) {
                        res.add(transForm(row));
                    }
                    return res.size() == 0 ? null : res.get(0);
                });
    }

    public Multi<T> queryWithCondition(String where, Tuple value, String... columns) {
        return mySQLPool.preparedQuery(getQuerySql(where, columns)).execute(value)
                .onItem().transformToMulti(set-> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm);
    }

    private String getQuerySql(String where, String... columns) {
        String sql = "select " + (columns == null || columns.length == 0 ? "*" : String.join(",", columns)) + " from " + tableName;
        sql = sql + judiceWhere(where);
        logger.debug(sql);
        return sql;
    }

    private String getCountSql(String where, Optional<String> countField) {
        String sql = "select count(" + (countField.isPresent() ? countField:"id") + ") as `count`  from " + tableName;
        sql = sql + judiceWhere(where);
        logger.debug(sql);
        return sql;
    }

    private String judiceWhere(String where){
        String trim = where.trim();
        if (!StringUtil.isNullOrEmpty(trim)
                && !trim.startsWith("order by")
                && !trim.startsWith("group by")
                && !trim.startsWith("limit")
                && !where.trim().toLowerCase().startsWith("where")) {
            return " where " + where;
        }
        return " " + where;
    }

    public Integer transToCount(RowSet<Row> rows) {
        List<Integer> res = new ArrayList<>();
        for (Row row : rows) {
            if (row.getColumnIndex("count") != -1) {
                res.add(row.getInteger("count"));
            } else {
                res.add(row.getInteger(0));
            }

        }
        return res.size() == 0 ? null : res.get(0);
    }

    public abstract T transForm(Row row);

}
