package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Article;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArticleDao extends BaseDao<Article> {

    private static final String searchWithTagSql =
            """
                   SELECT
                    a.id,a.title,a.article_type,a.title_image
                   FROM
                   articles a left join 
                    articles2tags
                    at on a.id = at.article_id LEFT JOIN tags t ON AT.tag_id = t.id 
                   WHERE
                    t.NAME = ?
                    """;

    private static final String searchWithTypeSql =
            """
                   SELECT
                    a.id,a.title,a.article_type,a.title_image
                   FROM
                   articles a WHERE
                    a.article_type = ?
                    """;

    private static final String searchWithTitle =
            """
                   SELECT
                    a.id,a.title,a.article_type,a.title_image
                   FROM
                   articles a WHERE
                    a.title like concat('%',?,'%')
                    """;

    public ArticleDao() {
        super(ApplicationConst.t_article);
    }

    public Multi<Tuple> queryAllType() {
        return getMySQLPool().query("select article_type, count(*) as `count` from " + ApplicationConst.t_article + " group by article_type")
                .execute().onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> Tuple.of(row.getString("article_type"), row.getInteger("count")));
    }


    public Multi<Article> searchWithTag(String tag) {
        return getMySQLPool().preparedQuery(searchWithTagSql).execute(Tuple.of(tag))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm);
    }

    public Multi<Article> searchWithType(String type) {
        return getMySQLPool().preparedQuery(searchWithTypeSql).execute(Tuple.of(type))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm);
    }

    public Multi<Article> searchWithTitle(String keyword) {
        return getMySQLPool().preparedQuery(searchWithTitle).execute(Tuple.of(keyword))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm);
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

}
