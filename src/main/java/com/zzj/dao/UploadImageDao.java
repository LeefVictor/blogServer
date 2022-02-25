package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.UploadImage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UploadImageDao extends BaseDao<UploadImage> {

    private final String insert_sql = "INSERT INTO `upload_image` (`name`, `whole_url`) VALUES (?,?)";

    public UploadImageDao() {
        super(ApplicationConst.t_uploadPhoto);
    }

    public Multi<UploadImage> getLatest(int limit) {
        return queryWithCondition(" order by id desc limit ?", Tuple.of(limit)
                , "id", "whole_url", "name");
    }

    public Uni<String> queryWithName(String name) {
        return queryWithCondition(" where name = ?", Tuple.of(name)
                , "whole_url").onItem().transform(uploadImage -> uploadImage.getWholeUrl())
                .collect().asList().onItem().transform(l -> l == null || l.isEmpty() ? null : l.get(0));
    }

    public Uni saveRecord(UploadImage uploadImage) {
        return insertOne(insert_sql, Tuple.of(uploadImage.getName(), uploadImage.getWholeUrl()));
    }

    public Uni saveRecords(List<UploadImage> uploadImages) {
        List<Tuple> tuples = uploadImages.stream().map(m -> Tuple.of(m.getName(), m.getWholeUrl())).collect(Collectors.toList());
        return getMySQLPool().preparedQuery(insert_sql).executeBatch(tuples).onItem().transform(rows -> Integer.valueOf(rows.size()));
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
