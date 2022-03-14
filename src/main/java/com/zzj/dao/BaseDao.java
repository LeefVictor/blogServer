package com.zzj.dao;

import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .onItem().transform(this::transToCount).onFailure().invoke(failure -> logger.error("查询异常", failure));
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
                }).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Multi<T> queryWithCondition(String where, Tuple value, String... columns) {
        return mySQLPool.preparedQuery(getQuerySql(where, columns)).execute(value)
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Uni<T> queryOneWithCondition(String where, Tuple value, String... columns) {
        return mySQLPool.preparedQuery(getQuerySql(where, columns)).execute(value)
                .onItem().transform(rows -> {
                    List<T> res = new ArrayList<>();
                    for (Row row : rows) {
                        res.add(transForm(row));
                    }
                    return res.size() == 0 ? null : res.get(0);
                }).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Uni<Long> insertOne(String sql, Tuple tuple) {
        //因为LAST_INSERT_ID是基于Connection的,在同一个连接中确保获取id时的原子性
        return getMySQLPool()
                .withTransaction(sqlConnection ->
                        sqlConnection.preparedQuery(sql).execute(tuple)
                                .onItem().transformToUni(rows -> {
                                    return sqlConnection.query("select LAST_INSERT_ID() as `id`").execute()
                                            .onItem().transform(rr -> {
                                                long id = 0;
                                                for (Row row : rr) {
                                                    id = row.getLong("id");
                                                }
                                                return id;
                                            });
                                })

                );

    }

    private String getQuerySql(String where, String... columns) {
        String sql = "select " + (columns == null || columns.length == 0 ? "*" : String.join(",", columns)) + " from " + tableName;
        sql = sql + judiceWhere(where);
        logger.debug(sql);
        return sql;
    }

    private String getCountSql(String where, Optional<String> countField) {
        String sql = "select count(" + (countField.isPresent() ? countField : "id") + ") as `count`  from " + tableName;
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
