package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Comments;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentsDao extends BaseDao<Comments> {

    public CommentsDao() {
        super(ApplicationConst.t_comments);
    }

    public Multi<Comments> latestComment(int limit){
        return queryWithCondition(" order by id desc limit ?", Tuple.of(limit));
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
