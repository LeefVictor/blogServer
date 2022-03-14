package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article2Contents;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            Uni<Integer> del = Uni.createFrom().item(1);
            Uni<Integer> insert = Uni.createFrom().item(1);
            if (!removeContentIds.isEmpty()) {
                List tuples = new ArrayList();
                tuples.add(articleId);
                List<String> separ = new ArrayList<>();
                for (int i = 0; i < removeContentIds.size(); i++) {
                    separ.add("?");
                    tuples.add(removeContentIds.get(i));
                }
                del = conn.preparedQuery("delete from article2contents where article_id=? and content_id in (" + String.join(",", separ) + ")").execute(Tuple.from(tuples)).onItem().transform(rows -> Integer.valueOf(rows.size()));
            }

            if (!contentIds.isEmpty()) {
                insert = conn.preparedQuery(insertSql).executeBatch(params).onItem().transform(rows -> Integer.valueOf(rows.size()));
            }

            return Uni.combine().all().unis(del, insert).combinedWith(o -> {
                return (Integer) o.get(1);
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
