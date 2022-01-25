package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article2Contents;
import io.vertx.mutiny.sqlclient.Row;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class Article2contentDao extends BaseDao<Article2Contents> {

    public Article2contentDao() {
        super(ApplicationConst.t_article2content);
    }

    @Override
    public Article2Contents transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Article2Contents().setArticleId(dr.getCol("article_id"))
                .setContentId(dr.getCol("content_id"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setVersion(dr.getVersion());
    }
}
