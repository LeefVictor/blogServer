package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.UploadImage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UploadImageDao extends BaseDao<UploadImage> {

    public UploadImageDao() {
        super(ApplicationConst.t_uploadPhoto);
    }

    public Multi<UploadImage> getLatest(int limit) {
        return queryWithCondition(" order by id desc limit ?", Tuple.of(limit)
                , "id", "whole_url", "name");
    }

    public Uni saveRecord(UploadImage uploadImage) {
        return insertOne("INSERT INTO `upload_image` (`name`, `whole_url`) VALUES (?,?)", Tuple.of(uploadImage.getName(), uploadImage.getWholeUrl()));
    }

    @Override
    public UploadImage transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new UploadImage()
                .setName(dr.getValue("name"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                .setId(dr.getId())
                .setWholeUrl(dr.getValue("whole_url"))
                .setVersion(dr.getVersion());
    }

}
