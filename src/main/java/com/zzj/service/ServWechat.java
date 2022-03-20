package com.zzj.service;

import com.zzj.dao.TreeHollowDao;
import com.zzj.entity.TreeHollow;
import com.zzj.vo.response.WxMsg;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ServWechat {

    private final Logger logger = LoggerFactory.getLogger(ServWechat.class);

    @Inject
    private TreeHollowDao treeHollowDao;

    public void toDb(String body) {
        //异步入库，同时查看是否需要回复， 要回复的话通过event进行触发
        //可以根据msgId进行重复接收排查，但这里是立即返回且异步保存的，所以不需要

        Uni.createFrom().item(() -> WxMsg.parse(body))
                .onItem().transform(wxMsg -> new TreeHollow().setFrom(wxMsg.getFromUserName())
                        .setMsgType(wxMsg.getMsgType())
                        .setMsgId(wxMsg.getMsgId())
                        .setContent(wxMsg.getContent())).subscribe().with(treeHollow -> {
                    if (!treeHollow.getMsgType().equals("text")) {
                        //TODO 发送消息通知不支持？
                    } else {
                        treeHollowDao.save(treeHollow).subscribe().with(o -> {
                            logger.info("save finish");
                        });
                    }

                });

    }

}
