package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article2Contents;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Article2contentDao extends BaseDao<Article2Contents> {

    private final Logger logger = LoggerFactory.getLogger(Article2contentDao.class);

    private final String insertSql = "INSERT INTO `article2contents` (`article_id`, `content_id`) VALUES (?,?) ON DUPLICATE KEY UPDATE article_id=?, content_id=?, version = version +1";

    public Article2contentDao() {
        super(ApplicationConst.t_article2content);
    }

    public Uni<Integer> save(long articleId, List<Long> contentIds, List<Long> removeContentIds) {
        return getMySQLPool().withTransaction(conn -> {
            List<Tuple> params = new ArrayList<>();
            contentIds.forEach(it -> params.add(Tuple.of(articleId, it, articleId, it)));
            logger.info("保存关系表" + articleId + "," + contentIds);

            return conn.preparedQuery(insertSql).executeBatch(params).onItem().transform(rows -> Integer.valueOf(rows.size())).invoke(integer -> {
                if (removeContentIds.isEmpty()) {
                    return;
                }
                List tuples = new ArrayList();
                tuples.add(articleId);
                List<String> separ = new ArrayList<>();
                for (int i = 0; i < removeContentIds.size(); i++) {
                    separ.add("?");
                    tuples.add(removeContentIds.get(i));
                }
                conn.preparedQuery("delete from article2contents where article_id=? and content_id in (" + String.join(",", separ) + ")").execute(Tuple.from(tuples)).subscribe().with(c -> {
                    logger.info("remove delete content" + removeContentIds);
                });
            });
        }).onFailure().invoke(failure -> logger.error("保存异常", failure));
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
