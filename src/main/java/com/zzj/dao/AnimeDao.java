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
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnimeDao extends BaseDao<Anime> {

    private final Logger logger = LoggerFactory.getLogger(AnimeDao.class);

    private final String update_sql = "update anime_base set image_url=?, download_img = 1, version = version + 1 where id = ?";

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
        return queryWithCondition(" where id = ? order by update_time desc",
                Tuple.of(id));
    }

    public Uni<Integer> updateImgUrl(long id, String url) {
        return getMySQLPool().preparedQuery(update_sql)
                .execute(Tuple.of(url, id)).onItem().transform(rows -> rows.size());
    }

    public Uni<Integer> updateImgUrls(List<Anime> anime) {
        List<Tuple> tuples = anime.stream().map(m -> Tuple.of(m.getImageUrl(), m.getId())).collect(Collectors.toList());
        return getMySQLPool().preparedQuery(update_sql)
                .executeBatch(tuples).onItem().transform(rows -> rows.size());
    }

    public Multi<Anime> findUnDownloadImgData() {
        return getMySQLPool().preparedQuery("select id, image_url from anime_base where download_img = ? limit ?")
                .execute(Tuple.of(0, 100))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::transForm).onFailure().invoke(failure -> logger.error("查询异常", failure));
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
