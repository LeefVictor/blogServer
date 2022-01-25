package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Contents;
import com.zzj.enums.ContentType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ContentDao extends BaseDao<Contents> {

    private  final String queryContentWithArticleId = "select * from contents c left join article2contents ac on c.id = ac.content_id where ac.article_id = ? order by ac.id asc";

    public ContentDao() {
        super(ApplicationConst.t_contents);
    }

    public Uni<List<Contents>> queryContent(long articleId) {
        return getMySQLPool().preparedQuery(queryContentWithArticleId)
                .execute(Tuple.of(articleId))
                .onItem().transform(rows -> {
                    List<Contents> list = new ArrayList<>();
                    //不知道使用multi会不会导致顺序的问题， 顺序在这里很重要
                    for (Row row : rows) {
                        list.add(transForm(row));
                    }

                    return list;
                });
    }

    @Override
    public Contents transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Contents().setId(dr.getId())
                .setContent(dr.getValue("content"))
                .setVersion(dr.getVersion())
                .setContentType(dr.getValue("content_type"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                ;
    }
}
