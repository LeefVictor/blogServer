package com.zzj.dao;

import com.zzj.common.DelegateRow;
import com.zzj.constants.ApplicationConst;
import com.zzj.entity.TreeHollow;
import com.zzj.utils.DBUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class TreeHollowDao extends BaseDao<TreeHollow> {

    private final Logger logger = LoggerFactory.getLogger(TreeHollowDao.class);

    public TreeHollowDao() {
        super(ApplicationConst.t_treeHollow);
    }

    public Uni<List<TreeHollow>> queryRand(int limit) {
        return count("where 1=1", Tuple.tuple()).onItem().transform(integer -> {
            Random random = new Random();
            Set<Long> ids = new HashSet<>();
            for (int i = 0; i < limit; i++) {
                ids.add(random.nextLong(integer) + 1);
            }
            return ids;
        }).flatMap(longs -> {
            return queryWithCondition("where id in (" + DBUtils.packInCondition(longs) + ")", Tuple.tuple(Arrays.asList(longs.toArray()))).collect().asList();
        });
    }

    public Uni save(TreeHollow treeHollow) {
        return getMySQLPool().withTransaction(conn ->
                conn.preparedQuery("INSERT INTO `tree_hollow` (`from`, `msg_type`, `content`, `msg_id`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE version = version+1")
                        .execute(Tuple.from(Arrays.asList(treeHollow.getFrom(), treeHollow.getMsgType(),
                                treeHollow.getContent(), treeHollow.getMsgId())))
                        .onItem().transform(rows -> rows.size())
        ).onFailure().invoke(failure -> logger.error("保存异常", failure));
    }

    @Override
    public TreeHollow transForm(Row row) {
        DelegateRow delegateRow = new DelegateRow(row);
        return new TreeHollow().setContent(delegateRow.getValue("content"))
                .setMsgId(delegateRow.getValue("msg_id"))
                .setMsgType(delegateRow.getValue("msg_type"))
                .setId(delegateRow.getId())
                .setCreateTime(delegateRow.getCreateTime())
                .setUpdateTime(delegateRow.getUpdateTime())
                .setReply(delegateRow.getValue("reply"))
                .setVersion(delegateRow.getVersion())
                .setFrom(delegateRow.getValue("from"))
                ;
    }
}
