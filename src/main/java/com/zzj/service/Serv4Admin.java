package com.zzj.service;

import com.zzj.constants.ApplicationConst;
import com.zzj.dao.*;
import com.zzj.entity.Article;
import com.zzj.entity.Contents;
import com.zzj.entity.UploadImage;
import com.zzj.enums.ContentType;
import com.zzj.utils.DateUtils;
import com.zzj.vo.CommentOuterClass;
import com.zzj.vo.HomeListOuterClass;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zzj.constants.ApplicationConst.author_desc_conf;
import static com.zzj.constants.ApplicationConst.tokenConfName;

@ApplicationScoped
public class Serv4Admin {

    private final Logger logger = LoggerFactory.getLogger(Serv4Admin.class);

    @Inject
    private ArticleDao articleDao;

    @Inject
    private Article2contentDao article2contentDao;

    @Inject
    private ContentDao contentDao;

    @Inject
    private Article2TagsDao article2TagsDao;

    @Inject
    private CommentsDao commentsDao;

    @Inject
    private ConfService confService;

    @Inject
    private UploadImageDao uploadImageDao;

    //后端请求的token校验
    public Uni<Boolean> validateToken(String token) {
        return confService.getConfUni(tokenConfName).onItem().transform(systemConf -> systemConf != null && systemConf.equals(token));
    }

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
                                .setMonth(DateUtils.getShortMonth(article.getCreateTime()))
                                .setDay(article.getCreateTime().getDayOfMonth() + "")
                                .build()
                ).collect().asList();


        return Uni.combine().all().unis(countUni, contentUni).combinedWith(objects -> {
            builder.setTotalPage((int) objects.get(0));
            builder.addAllArray(((List<HomeListOuterClass.content>) objects.get(1)));
            return builder.build();
        });
    }

    public Uni save(JsonObject object) {
        Article article = new Article()
                .setId(Long.parseLong(object.getString("id", "0")))
                .setTitle(object.getString("title"))
                .setSubTitle(object.getString("subTitle"))
                .setTitleImage(object.getString("titleImage"))
                .setMainTag(1)
                .setSummary(object.getString("summary"))
                .setArticleType(object.getString("articleType"))
                .setAuthor(confService.getConf(author_desc_conf));

        List<Contents> contents = new ArrayList<>();

        JsonArray array = object.getJsonArray("contents");

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.getJsonObject(i);
            contents.add(solve(obj));
        }

        Uni<List<Long>> contentUni = contentDao.insertContent(contents);
        Uni<Long> articleUni = articleDao.save(article);

        List<Long> removeIds = new ArrayList<>();

        if (object.containsKey("removeIds")) {
            JsonArray array1 = object.getJsonArray("removeIds");

            for (int i = 0; i < array1.size(); i++) {
                removeIds.add(array1.getLong(i));
            }
        }

        String[] tags = null;
        if (object.getString("articleTag") != null) {
            tags = object.getString("articleTag").replaceAll("，", ",").split(",");
        }

        String[] finalTags = tags;
        return Uni.combine().all().unis(contentUni, articleUni).combinedWith(objects -> {
            if (finalTags != null) {
                article2TagsDao.saveConcats(Arrays.asList(finalTags), ((Long) objects.get(1))).subscribe().with(aBoolean -> logger.info("save tag success"));
            }
            return article2contentDao.save(((Long) objects.get(1)), (List<Long>) objects.get(0), removeIds).onItem().transform(o -> ((Long) objects.get(1)));
        }).onItem().transformToUni(longUni -> longUni);
    }

    public void saveUploadRecord(String name, String url) {
        uploadImageDao.saveRecord(new UploadImage().setName(name).setWholeUrl(url)).subscribe().with(o -> {
            logger.info("保存图片上传记录成功");
        });
    }

    //评论,只加载未回复的
    public Uni<CommentOuterClass.Comments> queryComments() {
        return commentsDao.queryWithCondition("where hidden = 0 and reply_content is null ", Tuple.tuple())
                .onItem().transform(comments -> {
                    CommentOuterClass.Comment.Builder builder =
                            CommentOuterClass.Comment.newBuilder();
                    builder.setAuthor(comments.getNick())
                            .setId(comments.getId())
                            .setDate(DateUtils.transToYMD(comments.getCreateTime()))
                            .setContent(comments.getContent());
                    return builder.build();
                }).collect().asList().onItem().transform(list -> CommentOuterClass.Comments.newBuilder().addAllArray(list).build());

    }

    //作者_(:з」∠)_
    public Uni<Boolean> commentReply(long commentId, String replyContent, boolean sharp) {
        return commentsDao.reply(commentId, replyContent, sharp);
    }

    private Contents solve(JsonObject object) {
        ContentType ct = ContentType.valueOf(object.getString("type"));

        JsonObject jsonContent = object.getJsonObject("content");

        Contents contents = new Contents();
        Long id = jsonContent.getLong("id", 0L);
        contents.setId(id == null ? 0 : id);
        contents.setContentType(ct.name());

        jsonContent.remove("id");
        switch (ct) {
            case text:
                contents.setContent(jsonContent.getString("text"));
                break;

            case code:
                jsonContent.put("language", "");
                contents.setContent(jsonContent.toString());
                break;
            case block:
            case group:
            case photo:
            case tab:
                contents.setContent(jsonContent.toString());
                break;
        }
        return contents;
    }

    private int count2TotalPage(int integer, int pageSize) {
        return integer / pageSize + (integer % pageSize == 0 ? 0 : 1);
    }
}
