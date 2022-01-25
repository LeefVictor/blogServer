package com.zzj.service;

import com.zzj.constants.ApplicationConst;
import com.zzj.dao.*;
import com.zzj.entity.Article;
import com.zzj.entity.Contents;
import com.zzj.enums.ArticleTypeEnum;
import com.zzj.enums.ContentType;
import com.zzj.superior.CacheIt;
import com.zzj.utils.DateUtils;
import com.zzj.vo.ArticleOuterClass;
import com.zzj.vo.CommentOuterClass;
import com.zzj.vo.HomeListOuterClass;
import com.zzj.vo.RightSideListOuterClass;
import com.zzj.vo.request.PageVO;
import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

;

@ApplicationScoped
public class Serv4Web {

    //图片就放服务器就行了， 不需要搞什么图床，

    @Inject
    private CommentsDao commentsDao;
    @Inject
    private ArticleDao articleDao;

    @Inject
    private UploadImageDao uploadImageDao;

    @Inject
    private TagsDao tagsDao;

    @Inject
    private ContentDao contentDao;

    //主页
    public Uni<HomeListOuterClass.HomeList> homeList(PageVO request) {
        HomeListOuterClass.HomeList.Builder builder = HomeListOuterClass.HomeList.newBuilder();
        int limit = (request.getPage() - 1) * request.getPageSize();
        int offset = request.getPageSize();
        builder.setPage(request.getPage());

        Uni<Integer> countUni = articleDao.count("", Tuple.tuple())
                .onItem().transform(integer -> { //先转换为总页数
                    int total = 0;
                    if (integer != 0) {
                        total = integer / request.getPageSize() + (integer % request.getPageSize() == 0 ? 0 : 1);
                    }
                    return total;
                });

        Uni<List<HomeListOuterClass.content>> contentUni = articleDao.queryWithCondition(" where hidden = 0 order by id desc limit ?,?", Tuple.of(limit, offset))
                .onItem().transform(article ->
                        HomeListOuterClass.content.newBuilder()
                                .setId(article.getId()).setLove(article.getLove()).setCommentNum(article.getCommentCount()).setAuthor(article.getAuthor())
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

    public Uni<RightSideListOuterClass.RightSideList> rightSideList() {
        return Uni.combine().all().unis(latestComment(), latestPhoto(), articleCategory(), tags()).combinedWith(objects ->
                RightSideListOuterClass.RightSideList.newBuilder()
                        .addAllComments(((List<RightSideListOuterClass.LatestComment>) objects.get(0)))
                        .addAllPhotos(((List<RightSideListOuterClass.Photo>) objects.get(1)))
                        .addAllType(((List<RightSideListOuterClass.ArticleType>) objects.get(2)))
                        .addAllTags(((List<RightSideListOuterClass.Tags>) objects.get(3))).build());
    }

    //新评,
    @CacheIt
    public Uni<List<RightSideListOuterClass.LatestComment>> latestComment() {
        return commentsDao.latestComment(3)
                .onItem().transform(comments -> RightSideListOuterClass.LatestComment.newBuilder()
                        .setCommentId(comments.getId())
                        .setArticleId(comments.getArticleId())
                        .setContent(comments.getContent().substring(0,20)+"...")
                        .setDate(DateUtils.transToYMD(comments.getCreateTime())).build())
                .collect().asList();
    }

    //新图
    @CacheIt
    public Uni<List<RightSideListOuterClass.Photo>> latestPhoto() {
        return uploadImageDao.getLatest(6)
                .onItem().transform(uploadImage -> RightSideListOuterClass.Photo.newBuilder().setSrc(uploadImage.getWholeUrl())
                        .setTitle(uploadImage.getName())
                        .setAlt(uploadImage.getName()).build())
                .collect().asList();

    }

    // 文章分类
    @CacheIt
    public Uni<List<RightSideListOuterClass.ArticleType>> articleCategory() {
        return articleDao.queryAllType().onItem().transform(tuple -> RightSideListOuterClass.ArticleType.newBuilder().setName(ArticleTypeEnum.valueOf(tuple.getString(0)).getDesc())
                .setCount(tuple.getInteger(1)).build()).collect().asList();
    }

    //博客标签，
    @CacheIt
    public Uni<List<RightSideListOuterClass.Tags>> tags() {
        return tagsDao.queryTags(20)
                .onItem().transform(tags -> RightSideListOuterClass.Tags.newBuilder().setId(((Long) tags.getId()).intValue())
                        .setName(tags.getName()).build()).collect().asList();
    }


    //详情页
    public Uni<ArticleOuterClass.Articles> detail(long articleId) {
        Uni<Article> article = articleDao.queryWithId(articleId,
                "id", "title", "sub_title", "author", "title_image"
        );

        Uni<List<Contents>> contents = contentDao.queryContent(articleId);

        return Uni.combine().all().unis(article, contents).combinedWith(objects -> {
            ArticleOuterClass.Articles.Builder builder = ArticleOuterClass.Articles.newBuilder();

            Article res = (Article) objects.get(0);
            List<Contents> contentsList = (List<Contents>) objects.get(1);

            builder.setTitle(res.getTitle())
                    .setSubTitle(res.getSubTitle())
                    .setAuthor(res.getAuthor())
                    .setTitleImage(res.getTitleImage());

            for (Contents contents1 : contentsList) {
                builder.addArt(solve(contents1));
            }


            return builder.build();
        });
    }


    //评论, 不和详情一并返回， 错开来
    public Uni<CommentOuterClass.Comments> queryComments(long articleId) {
        return commentsDao.queryWithCondition("where article_id = ? ", Tuple.of(articleId))
                .onItem().transform(comments -> {
                    CommentOuterClass.Comment.Builder builder =
                            CommentOuterClass.Comment.newBuilder();
                    builder.setAuthor(comments.getNick())
                            .setDate(DateUtils.transToYMD(comments.getCreateTime()))
                            .setContent(comments.getContent());

                    if (!StringUtil.isNullOrEmpty(comments.getReplyContent())) {
                        builder.setReply(CommentOuterClass.Reply.newBuilder()
                                .setAuthor("作者")
                                .setDate(DateUtils.transToYMD(comments.getReplyTime()))
                                .setContent(comments.getReplyContent() == null ? "" : comments.getReplyContent()).build());
                    }

                    return builder.build();
                }).collect().asList().onItem().transform(list -> CommentOuterClass.Comments.newBuilder().addAllArray(list).build());

    }


    private ArticleOuterClass.Article solve(Contents content) {
        ContentType ct = ContentType.valueOf(content.getContentType());
        ArticleOuterClass.Article.Builder builder = ArticleOuterClass.Article.newBuilder();

        builder.setType(ct.name());

        JsonObject json;
        switch (ct) {
            case text:
                builder.setText(content.getContent());
                break;
            case blank:
                break;
            case block:
                json = new JsonObject(content.getContent());
                builder.setText(json.getString("text"))
                        .setBlockauthor(json.getString("author"))
                        .setBlockfromsite(json.getString("fromSite"));
                break;
            case code:
                json = new JsonObject(content.getContent());
                builder.setText(json.getString("text"))
                        .setLanguage(json.getString("language"));
                break;
            case group:
                json = new JsonObject(content.getContent());

                ArticleOuterClass.Group.Builder gbuilder = ArticleOuterClass.Group.newBuilder();
                gbuilder.setTitle(json.getString("title"));
                JsonArray items = json.getJsonArray("items");
                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.getJsonObject(i);
                    gbuilder.addItems(ArticleOuterClass.GroupItem.newBuilder()
                            .setTitle(item.getString("title"))
                            .addAllBodys(item.getJsonArray("bodys").getList())
                            .build());
                }

                builder.setGroup(gbuilder.build());
                break;
            case photo:
                json = new JsonObject(content.getContent());
                builder.setPhoto(ArticleOuterClass.Photo.newBuilder()
                        .setAlt(json.getString("alt"))
                        .setSrc(json.getString("src"))
                        .setAuthor(json.getString("author")).build());
                break;
            case tab:
                json = new JsonObject(content.getContent());
                builder.setTab(ArticleOuterClass.Tab.newBuilder()
                        .setTitle(json.getString("title"))
                        .addAllHeaders(json.getJsonArray("headers").getList())
                        .addAllBodys(json.getJsonArray("bodys").getList()).build());
                break;
        }
        return builder.build();
    }



    /*

    //搜索页
    public Multi search() {

    }

    //锐评
    public Multi getSharpComments() {

    }



    //发表评论
    public Uni comment() {

    }*/


}
