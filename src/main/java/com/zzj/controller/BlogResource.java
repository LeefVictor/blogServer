package com.zzj.controller;

import com.zzj.service.Serv4Web;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("blog")
public class BlogResource {

    @Inject
    private Serv4Web serv4Web;

    @GET
    @Produces("application/x-protobuf")
    @Path("/home/{page}")
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
    @Path("/detail/{id}/comments")
    public Uni<byte[]> comments(@PathParam("id") long articleId) {
        return serv4Web.queryComments(articleId).onItem().transform(comments -> comments.toByteArray());
    }
}
