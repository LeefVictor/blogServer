package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Anime;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnimeDao extends BaseDao<Anime> {

    private final Logger logger = LoggerFactory.getLogger(AnimeDao.class);

    public AnimeDao() {
        super(ApplicationConst.t_anime);
    }


    //注意设置
    // SET GLOBAL group_concat_max_len = 9096;
    //SET SESSION group_concat_max_len = 9096;
    public Multi<Anime> query(String keyword, int limit, int offset) {
        return queryWithCondition(" where name like concat('%',?,'%') group by name  limit ?,?",
                Tuple.of(keyword == null ? "" : keyword, limit, offset), "id", "name", "image_url", "total", "is_finish", "GROUP_CONCAT(site_name) as site_name");
    }

    public Uni<Integer> countWithSearch(String keyword) {
        return getMySQLPool().preparedQuery("select COUNT(DISTINCT name) as count from anime where name like concat('%',?,'%')  ").execute(Tuple.of(keyword))
                .onItem().transform(super::transToCount);
    }

    public Multi<Anime> findAnimeById(long id) {
        return queryWithCondition(" where base_id = ? order by update_time desc",
                Tuple.of(id));
    }

    public Uni<Integer> updateImgUrl(long id, String url) {
        return getMySQLPool().preparedQuery("update anime_base set image_url=? where id = ?")
                .execute(Tuple.of(url, id)).onItem().transform(rows -> rows.size());
    }

    @Override
    public Anime transForm(Row row) {
        DelegateRow delegateRow = new DelegateRow(row);
        return new Anime()
                .setId(delegateRow.getId())
                .setCreateTime(delegateRow.getCreateTime())
                .setUpdateTime(delegateRow.getUpdateTime())
                .setVersion(delegateRow.getVersion())
                .setName(delegateRow.getValue("name"))
                .setImageUrl(delegateRow.getValue("image_url"))
                .setTotal(delegateRow.getIntCol("total"))
                .setIsFinish(delegateRow.getIntCol("is_finish"))
                .setSiteName(delegateRow.getValue("site_name"))
                .setEpisode(delegateRow.getIntCol("episode"))
                .setUrl(delegateRow.getValue("url"));
    }
}
