package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Comments;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentsDao extends BaseDao<Comments> {


    private final String insertComment =
            """
                    INSERT INTO `comments` ( `article_id`, `nick`, `email`, `content` )
                    VALUES(?,?,?,?)
                    """;

    public CommentsDao() {
        super(ApplicationConst.t_comments);
    }

    public Multi<Comments> latestComment(int limit){
        return queryWithCondition(" where hidden = 0 order by id desc limit ?", Tuple.of(limit));
    }

    public Multi<Comments> SharpComment(int limit){
        return queryWithCondition(" where hidden = 0 and is_sharp = 1 order by id desc limit ?", Tuple.of(limit));
    }

    public Uni<Long> insertComments(long articleId, String nick, String email, String comment) {
        //在同一个事务中确保获取id时的原子性
        return getMySQLPool()
                .withTransaction(sqlConnection ->
                        sqlConnection.preparedQuery(insertComment).execute(Tuple.of(articleId, nick, email, comment))
                                .onItem().transformToUni(rows -> {
                                    for (Row row : rows) {
                                        System.out.println(row.toJson());
                                    }
                                    return sqlConnection.query("select LAST_INSERT_ID() as `id`").execute()
                                            .onItem().transform(rr -> {
                                                long id = 0;
                                                for (Row row : rr) {
                                                    id = row.getInteger("id");
                                                }
                                                return id;
                                            });
                                })

                );

    }


    @Override
    public Comments transForm(Row row) {
        DelegateRow row1 = new DelegateRow(row);
        return new Comments().setId(row1.getId())
                .setArticleId(row1.getCol("article_id"))
                .setHidden(row1.getIntCol("hidden"))
                .setContent(row1.getValue("content"))
                .setEmail(row1.getValue("email"))
                .setIsSharp(row1.getIntCol("is_sharp"))
                .setNick(row1.getValue("nick"))
                .setReplyContent(row1.getValue("reply_content"))
                .setReplyTime(row1.getTime("reply_time"))
                .setCreateTime(row1.getCreateTime())
                .setUpdateTime(row1.getUpdateTime())
                .setVersion(row1.getVersion());
    }
}
