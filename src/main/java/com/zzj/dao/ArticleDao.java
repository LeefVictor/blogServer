package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticleDao extends BaseDao<Article> {

    private final Logger logger = LoggerFactory.getLogger(ArticleDao.class);

    public ArticleDao() {
        super(ApplicationConst.t_article);
    }

    public Multi<Tuple> queryAllType() {
        return getMySQLPool().query("select article_type, count(*) as `count` from " + ApplicationConst.t_article + " group by article_type")
                .execute().onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> Tuple.of(row.getString("article_type"), row.getInteger("count")))
                .onFailure().invoke(failure -> logger.error("查询异常", failure));
    }


    public Multi<Article> searchWithTag(String tag, int limit, int offset) {
        return getMySQLPool().preparedQuery(searchWithTagSql).execute(Tuple.of(tag, limit, offset))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Multi<Article> searchWithType(String type, int limit, int offset) {
        return getMySQLPool().preparedQuery(searchWithTypeSql).execute(Tuple.of(type, limit, offset))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Multi<Article> searchWithTitle(String keyword, int limit, int offset) {
        return getMySQLPool().preparedQuery(searchWithTitle).execute(Tuple.of(keyword, limit, offset))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm).onFailure().invoke(failure -> logger.error("查询异常", failure));
    }

    public Uni<Integer> countWithTag(String tag) {
        return getMySQLPool().preparedQuery(countWithTagSql).execute(Tuple.of(tag))
                .onItem().transform(super::transToCount);
    }

    public Uni<Integer> countWithType(String type) {
        return getMySQLPool().preparedQuery(countWithTypeSql).execute(Tuple.of(type))
                .onItem().transform(super::transToCount);
    }

    public Uni<Integer> countWithTitle(String keyword) {
        return getMySQLPool().preparedQuery(countWithTitle).execute(Tuple.of(keyword))
                .onItem().transform(super::transToCount);
    }

    public Uni<Long> save(Article article) {
        List params = new ArrayList();
        params.add(article.getId() == 0 ? null : article.getId());
        params.add(article.getTitle());
        params.add(article.getTitleImage());
        params.add(article.getArticleType());
        params.add(article.getAuthor() == null ? "梓健_(:з」∠)_" : article.getAuthor());
        params.add(article.getSummary());
        params.add(article.getSubTitle());
        params.add(article.getHidden());

        params.addAll(params.subList(1, params.size()));

        return getMySQLPool().withTransaction(conn ->
                conn.preparedQuery(insertArticle).execute(Tuple.from(params)).onItem().transformToUni(rows -> {
                    if (article.getId() > 0) {
                        return Uni.createFrom().item(article.getId());
                    }
                    return conn.query("select LAST_INSERT_ID() as `id`").execute()
                            .onItem().transform(rr -> {
                                Long id = 0L;
                                for (Row row : rr) {
                                    id = row.getLong("id");
                                }
                                return id;
                            });
                })
        ).onFailure().invoke(failure -> logger.error("保存异常", failure));
    }

    public void addOneView(long articleId) {
        getMySQLPool().preparedQuery("update articles set love = love + 1 where id=?").execute(Tuple.of(articleId)).subscribe().with(o -> {
            logger.debug("add one view with " + articleId);
        });
    }


    @Override
    public Article transForm(Row row) {
        DelegateRow delegateRow = new DelegateRow(row);
        return new Article()
                .setId(delegateRow.getId())
                .setCreateTime(delegateRow.getCreateTime())
                .setUpdateTime(delegateRow.getUpdateTime())
                .setVersion(delegateRow.getVersion())
                .setTitle(delegateRow.getValue("title"))
                .setTitleImage(delegateRow.getValue("title_image"))
                .setArticleType(delegateRow.getValue("article_type"))
                .setAuthor(delegateRow.getValue("author"))
                .setHidden(delegateRow.getIntCol("hidden"))
                .setSummary(delegateRow.getValue("summary"))
                .setLove(delegateRow.getIntCol("love"))
                .setMainTag(delegateRow.getIntCol("main_tag"))
                .setSubTitle(delegateRow.getValue("sub_title"))
                .setCommentCount(delegateRow.getIntCol("comment_count"));
    }

    private final String insertArticle = "INSERT INTO `articles` (`id`, `title`, `title_image`, `article_type`, `author`, `summary`, `sub_title`, `hidden`) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE title=?,title_image=?,article_type = ?,author=?,summary=?,sub_title=?,hidden=?,version = version+1";


    private static final String searchWithTagSql =
            """
                    SELECT
                     a.id,a.title,a.article_type,a.title_image
                    FROM
                    articles a left join 
                     articles2tags
                     at on a.id = at.article_id 
                    WHERE
                    at.tag = ? limit ?,?
                    """;

    private static final String searchWithTypeSql =
            """
                   SELECT
                    a.id,a.title,a.article_type,a.title_image
                   FROM
                   articles a WHERE
                    a.article_type = ? limit ?,?
                    """;

    private static final String searchWithTitle =
            """
                   SELECT
                    a.id,a.title,a.article_type,a.title_image
                   FROM
                   articles a WHERE
                    a.title like concat('%',?,'%') limit ?,?
                    """;

    private static final String countWithTagSql =
            """
                    SELECT
                     count(distinct at.article_id)
                    FROM
                     articles2tags
                     at  
                    WHERE
                     at.tag = ? 
                     """;

    private static final String countWithTypeSql =
            """
                   SELECT
                    count(a.id)
                   FROM
                   articles a WHERE
                    a.article_type = ? 
                    """;

    private static final String countWithTitle =
            """
                   SELECT
                    count(a.id)
                   FROM
                   articles a WHERE
                    a.title like concat('%',?,'%') 
                    """;
}
