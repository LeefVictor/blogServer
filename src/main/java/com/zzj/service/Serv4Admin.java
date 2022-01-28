package com.zzj.service;

import com.zzj.constants.ApplicationConst;
import com.zzj.dao.ArticleDao;
import com.zzj.vo.HomeListOuterClass;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class Serv4Admin {

    @Inject
    private ArticleDao articleDao;

    public Uni<HomeListOuterClass.HomeList> homeList(PageVO request) {
        HomeListOuterClass.HomeList.Builder builder = HomeListOuterClass.HomeList.newBuilder();
        int limit = (request.getPage() - 1) * request.getPageSize();
        int offset = request.getPageSize();
        builder.setPage(request.getPage());

        Uni<Integer> countUni = articleDao.count(" where hidden = 0 ", Tuple.tuple())
                .onItem().transform(integer -> { //先转换为总页数
                    int total = 0;
                    if (integer != 0) {
                        total = count2TotalPage(integer, request.getPageSize());
                    }
                    return total;
                });

        Uni<List<HomeListOuterClass.content>> contentUni = articleDao.queryWithCondition(" where hidden = 0 order by id desc limit ?,?", Tuple.of(limit, offset))
                .onItem().transform(article ->
                        HomeListOuterClass.content.newBuilder()
                                .setId(article.getId())
                                .setMainTag(ApplicationConst.getMainTag(article.getMainTag()))
                                .setSummary(article.getSummary())
                                .setTitle(article.getTitle())
                                .setTitleImage(article.getTitleImage())
                                .setYear((article.getCreateTime().getYear() + "").substring(2))
                                .setMonth(article.getCreateTime().getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                                .setDay(article.getCreateTime().getDayOfMonth() + "")
                                .build()
                ).collect().asList();


        return Uni.combine().all().unis(countUni, contentUni).combinedWith(objects -> {
            builder.setTotalPage((int) objects.get(0));
            builder.addAllArray(((List<HomeListOuterClass.content>) objects.get(1)));
            return builder.build();
        });
    }


    private int count2TotalPage(int integer, int pageSize) {
        return integer / pageSize + (integer % pageSize == 0 ? 0 : 1);
    }
}
