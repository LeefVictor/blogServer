package com.zzj.controller;

import com.zzj.enums.ConfType;
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


    @GET
    @Produces("application/x-protobuf")
    @Path("/search")
    public Uni<byte[]> search(@QueryParam("keyword") String keyword,
                              @QueryParam("page") int page) {
        return serv4Web.search(keyword, new PageVO().setPage(page).setPageSize(3)).onItem().transform(search -> search.toByteArray());
    }


    @GET
    @Produces("application/x-protobuf")
    @Path("/detail/{id}/comments")
    public Uni<byte[]> comments(@PathParam("id") long articleId) {
        return serv4Web.queryComments(articleId).onItem().transform(comments -> comments.toByteArray());
    }

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
}
