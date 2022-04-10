package com.zzj.controller;

import com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding;
import com.zzj.enums.ConfType;
import com.zzj.sentinelfallback.DefaultFallback;
import com.zzj.sentinelfallback.Fallback;
import com.zzj.service.Serv4Web;
import com.zzj.superior.IPValid;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("blog")
public class BlogResource {

    @Inject
    private Serv4Web serv4Web;

    @SentinelResourceBinding(value = "homeList", defaultFallback = Fallback.bytesMethod, fallbackClass = DefaultFallback.class)
    @GET
    @Produces("application/x-protobuf")
    @Path("/home/{page}")
    @IPValid
    public Uni<byte[]> homeList(@PathParam("page") int page) {
        return serv4Web.homeList(new PageVO().setPage(page)).onItem().transform(homeList -> homeList.toByteArray());
    }


    @GET
    @Produces("application/x-protobuf")
    @Path("/rightSide")
    @SentinelResourceBinding(value = "rightSide", defaultFallback = Fallback.bytesMethod, fallbackClass = DefaultFallback.class)
    public Uni<byte[]> rightSide() {
        return serv4Web.rightSideList().onItem().transform(rightSideList -> rightSideList.toByteArray());
    }


    @GET
    @Produces("application/x-protobuf")
    @Path("/detail/{id}")
    public Uni<byte[]> detail(@PathParam("id") long articleId) {
        return serv4Web.detail(articleId).onItem().transform(detail -> detail.toByteArray());
    }

    @GET
    @Produces("application/x-protobuf")
    @Path("/footer/sharps")
    public Uni<byte[]> getSharpComments() {
        return serv4Web.getSharpComments().onItem().transform(detail -> detail.toByteArray());
    }


    @SentinelResourceBinding(value = "blogSearch", defaultFallback = Fallback.bytesMethod, fallbackClass = DefaultFallback.class)
    @GET
    @Produces("application/x-protobuf")
    @Path("/search")
    public Uni<byte[]> search(@QueryParam("keyword") String keyword,
                              @QueryParam("page") int page) {
        return serv4Web.search(keyword, new PageVO().setPage(page).setPageSize(9)).onItem().transform(search -> search.toByteArray());
    }


    @GET
    @Produces("application/x-protobuf")
    @Path("/detail/{id}/comments")
    public Uni<byte[]> comments(@PathParam("id") long articleId) {
        return serv4Web.queryComments(articleId).onItem().transform(comments -> comments.toByteArray());
    }

    @SentinelResourceBinding(value = "writeComments", defaultFallback = Fallback.strMethod, fallbackClass = DefaultFallback.class)
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/detail/{id}/writeComment")
    @IPValid
    public Uni<String> sendComment(@PathParam("id") long articleId,
                                   @FormParam("nick") String nick,
                                   @FormParam("email") String email,
                                   RoutingContext rc,
                                   @FormParam("content") String content) {
        if (content.length() > 300) {
            return Uni.createFrom().item("太长了，不接受");
        }
        return serv4Web.writeComment(articleId, nick, email, content);
    }


    @GET
    @Produces("application/json")
    @Path("/frontConf")
    public Uni<JsonObject> comments() {
        return serv4Web.getDefaultConf(ConfType.front);
    }

    @SentinelResourceBinding(value = "pictures", defaultFallback = Fallback.bytesMethod, fallbackClass = DefaultFallback.class)
    @GET
    @Produces("application/x-protobuf")
    @Path("/pics/{page}")
    public Uni<byte[]> pics(@PathParam("page") int page) {
        return serv4Web.queryUploadImage(page).onItem().transform(pics -> pics.toByteArray());
    }

    @GET
    @Produces("application/x-protobuf")
    @Path("/hollow")
    public Uni<byte[]> hollow() {
        return serv4Web.queryHollowData().onItem().transform(pics -> pics.toByteArray());
    }
}
