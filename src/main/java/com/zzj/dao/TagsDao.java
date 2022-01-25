package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Comments;
import com.zzj.entity.Tags;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagsDao extends BaseDao<Tags> {

    public TagsDao() {
        super(ApplicationConst.t_tags);
    }

    public Multi<Tags> queryTags(int limit){
        return queryWithCondition(" order by id desc limit ?", Tuple.of(limit));
    }

    @Override
    public Tags transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Tags().setHidden(dr.getIntCol("hidden"))
                .setName(dr.getValue("name"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setId(dr.getId())
                .setVersion(dr.getVersion());
    }

}
