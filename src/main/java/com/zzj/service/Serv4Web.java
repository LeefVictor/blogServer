package com.zzj.service;

import com.zzj.constants.ApplicationConst;
import com.zzj.dao.*;
import com.zzj.entity.Article;
import com.zzj.entity.Contents;
import com.zzj.entity.SystemConf;
import com.zzj.enums.ArticleTypeEnum;
import com.zzj.enums.ConfType;
import com.zzj.enums.ContentType;
import com.zzj.superior.CacheIt;
import com.zzj.utils.ContentUtils;
import com.zzj.utils.DateUtils;
import com.zzj.vo.*;
import com.zzj.vo.request.PageVO;
import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


@ApplicationScoped
public class Serv4Web {

    private final Logger logger = LoggerFactory.getLogger(Serv4Web.class);
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

    @Inject
    private SystemConfDao systemConfDao;

    //主页
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
                        .setContent(ContentUtils.subString(comments.getContent(), 20))
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
        return articleDao.queryAllType().onItem().transform(tuple -> {
            ArticleTypeEnum are = ArticleTypeEnum.valueOf(tuple.getString(0));
            return RightSideListOuterClass.ArticleType.newBuilder().setValue(are.name()).setName(are.getDesc())
                    .setCount(tuple.getInteger(1)).build();
        }).collect().asList();
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
        return commentsDao.queryWithCondition("where article_id = ? and hidden = 0  ", Tuple.of(articleId))
                .onItem().transform(comments -> {
                    CommentOuterClass.Comment.Builder builder =
                            CommentOuterClass.Comment.newBuilder();
                    builder.setAuthor(comments.getNick())
                            .setDate(DateUtils.transToYMD(comments.getCreateTime()))
                            .setContent(comments.getContent());

                    if (!StringUtil.isNullOrEmpty(comments.getReplyContent())) {
                        builder.setReply(CommentOuterClass.Reply.newBuilder()
                                .setAuthor("作者_(:з」∠)_")
                                .setDate(DateUtils.transToYMD(comments.getReplyTime()))
                                .setContent(comments.getReplyContent() == null ? "" : comments.getReplyContent()).build());
                    }

                    return builder.build();
                }).collect().asList().onItem().transform(list -> CommentOuterClass.Comments.newBuilder().addAllArray(list).build());

    }


    //TODO 发表评论,写操作拦截，
    // 同一个文章一个 ip只要发送超过了3次，就列入黑名单
    public Uni<String> writeComment(long articleId, String nick, String email, String comment) {
        return commentsDao.insertComments(articleId, nick, email, comment)
                .onItem().transform(aLong -> {
                    logger.info("save data id is " + aLong);
                    return "success";
                });
    }


    //锐评
    public Uni<SharpCommentOuterClass.SharpComments> getSharpComments() {
        return commentsDao.queryWithCondition("where is_sharp = 1 and hidden = 0  ", Tuple.tuple(), "article_id", "content", "nick")
                .onItem().transform(comments ->
                        SharpCommentOuterClass.SharpComment.newBuilder()
                                .setNick(comments.getNick())
                                .setArticleId(comments.getArticleId())
                                .setContent(ContentUtils.subString(comments.getContent(), 80)).build())
                .collect().asList().onItem().transform(list -> SharpCommentOuterClass.SharpComments.newBuilder().addAllComments(list).build());

    }


    //所需的前端配置，
    public Uni<JsonObject> getDefaultConf(ConfType type) {
        return systemConfDao.queryWithCondition(" where type = ?", Tuple.of(type.name()), "name", "value")
                .collect().asList().onItem().transform(systemConfs -> {
                    JsonObject jsonObject = new JsonObject();
                    for (SystemConf conf : systemConfs) {
                        jsonObject.put(conf.getName(), conf.getValue());
                    }
                    return jsonObject;
                });
    }


    //搜索页, 所有资源都是一个article， 所以这里就搜article表即可，
    //#keyword 是按照类型搜索
    //#keyword# 是按照标签搜索
    public Uni<SearchData.Items> search(String keyword, PageVO pageVO) {
        Multi<Article> search;
        Uni<Integer> count;
        if (keyword == null) {
            keyword = "";
        }

        int limit = (pageVO.getPage() - 1) * pageVO.getPageSize();
        int offset = pageVO.getPageSize();


        if (keyword.startsWith("@") && keyword.endsWith("@")) {
            //标签搜索
            keyword = keyword.substring(1, keyword.length() - 1);
            search = articleDao.searchWithTag(keyword, limit, offset);
            count = articleDao.countWithTag(keyword);
        } else if (keyword.startsWith("@")) {
            //article 类型搜索
            ArticleTypeEnum typeEnum = ArticleTypeEnum.valueOf(keyword.substring(1));
            search = articleDao.searchWithType(typeEnum.name(), limit, offset);
            count = articleDao.countWithType(typeEnum.name());
        } else {
            //title 搜索
            search = articleDao.searchWithTitle(keyword, limit, offset);
            count = articleDao.countWithTitle(keyword);
        }

        return Uni.combine().all().unis(search.onItem().transform(article ->
                SearchData.Item.newBuilder().setTitle(article.getTitle())
                        .setArticleId(article.getId())
                        .setTypedesc(ArticleTypeEnum.valueOf(article.getArticleType()).getDesc())
                        .setType(article.getArticleType())
                        .setTitleImage(article.getTitleImage()).build()
        ).collect().asList(), count).combinedWith(objects ->
                SearchData.Items.newBuilder()
                        .setPage(pageVO.getPage())
                        .setTotalPage(count2TotalPage((int) objects.get(1), pageVO.getPageSize()))
                        .addAllItems((List<SearchData.Item>) objects.get(0)).build());
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

    private int count2TotalPage(int integer, int pageSize){
        return integer / pageSize + (integer % pageSize == 0 ? 0 : 1);
    }

}
