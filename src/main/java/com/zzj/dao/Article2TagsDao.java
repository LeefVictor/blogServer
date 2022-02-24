package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article2Tags;
import com.zzj.utils.DBUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Article2TagsDao extends BaseDao<Article2Tags> {

    private final Logger logger = LoggerFactory.getLogger(Article2TagsDao.class);

    public Article2TagsDao() {
        super(ApplicationConst.t_article2tags);
    }


    private final String deleteSql = "delete from articles2tags where article_id = ? and tag in (##)";


    private final String saveSql = "INSERT INTO `articles2tags` (`tag`, `article_id`) VALUES (?,?)  ON DUPLICATE KEY UPDATE version = version + 1 ";

    public Multi<Article2Tags> queryTags(int limit) {
        return queryWithCondition(" GROUP BY tag ORDER BY cc desc limit  ?", Tuple.of(limit), " tag ", " count(at.tag) as cc ");
    }

    public Multi<Article2Tags> queryTags(long articleId) {
        return queryWithCondition(" article_id=?", Tuple.of(articleId), " DISTINCT tag ");
    }

    public Uni<Boolean> saveConcats(List tags, long articleId) {
        return getMySQLPool().withTransaction(conn -> {
            String sept = DBUtils.packInCondition(tags);
            List del = new ArrayList(tags.size() + 1);
            del.add(articleId);
            del.addAll(tags);
            return conn.preparedQuery(deleteSql.replace("##", sept)).execute(Tuple.from(del)).onItem().transform(rows -> Boolean.TRUE).invoke(integer -> {
                List<Tuple> params = new ArrayList<>(tags.size());
                tags.forEach(it -> params.add(Tuple.of(it, articleId)));
                conn.preparedQuery(saveSql).executeBatch(params).subscribe().with(c -> {
                    logger.info("save success");
                });
            });
        }).onFailure().invoke(failure -> logger.error("保存异常", failure));
    }


    @Override
    public Article2Tags transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Article2Tags().setId(dr.getId())
                .setArticleId(dr.getCol("article_id"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setTag(dr.getValue("tag"))
                .setVersion(dr.getVersion());
    }
}
