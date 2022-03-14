package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.Contents;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContentDao extends BaseDao<Contents> {
    private final Logger logger = LoggerFactory.getLogger(ContentDao.class);

    private final String queryContentWithArticleId =
            """
                    SELECT
                    	*
                    FROM
                    	contents c
                    	LEFT JOIN article2contents ac ON c.id = ac.content_id
                    WHERE
                    	ac.article_id = ?
                    ORDER BY
                    	ac.id ASC
                    """;

    private final String insertContent = "INSERT INTO `contents` (`id`, `content_type`, `content`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE content_type=?, content=?, version = version +1";


    public ContentDao() {
        super(ApplicationConst.t_contents);
    }

    public Uni<List<Contents>> queryContent(long articleId) {
        return getMySQLPool().preparedQuery(queryContentWithArticleId)
                .execute(Tuple.of(articleId))
                .onItem().transform(rows -> {
                    List<Contents> list = new ArrayList<>();
                    //不知道使用multi会不会导致顺序的问题， 顺序在这里很重要
                    for (Row row : rows) {
                        list.add(transForm(row));
                    }

                    return list;
                });
    }

    public Uni<List<Long>> insertContent(List<Contents> contents) {
        if (contents == null || contents.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        List<Uni<Long>> unis = new ArrayList<>();
        for (Contents content : contents) {
            Long cid = content.getId() == 0 ? null : content.getId();
            unis.add(getMySQLPool().withConnection(conn ->
                    conn.preparedQuery(insertContent)
                            .execute(Tuple.of(cid, content.getContentType(), content.getContent(), content.getContentType(), content.getContent()))
                            .onItem().transformToUni(rows -> {
                                if (content.getId() > 0) {
                                    return Uni.createFrom().item(content.getId());
                                }
                                        return conn.query("select LAST_INSERT_ID() as `id`").execute()
                                                .onItem().transform(rr -> {
                                                    Long id = 0L;
                                                    for (Row row : rr) {
                                                        id = row.getLong("id");
                                                    }
                                                    return id;
                                                });
                                    }

                            )
            ).onFailure().invoke(failure -> logger.error("保存异常", failure)));
        }
        return Uni.combine().all().unis(unis.toArray(new Uni[0])).combinedWith(objects -> objects.stream().map(o -> (Long) o).collect(Collectors.toList()));


    }

    @Override
    public Contents transForm(Row row) {
        DelegateRow dr = new DelegateRow(row);
        return new Contents().setId(dr.getId())
                .setContent(dr.getValue("content"))
                .setVersion(dr.getVersion())
                .setContentType(dr.getValue("content_type"))
                .setCreateTime(dr.getCreateTime())
                .setUpdateTime(dr.getUpdateTime())
                ;
    }
}
