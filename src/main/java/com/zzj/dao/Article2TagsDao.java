package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article;
import com.zzj.entity.Article2Tags;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Article2TagsDao extends BaseDao<Article2Tags> {

    public Article2TagsDao() {
        super(ApplicationConst.t_article2tags);
    }




    @Override
    public Article2Tags transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Article2Tags().setId(dr.getId())
                .setArticleId(dr.getCol("article_id"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setTagId(dr.getCol("tag_id"))
                .setVersion(dr.getVersion());
    }
}
