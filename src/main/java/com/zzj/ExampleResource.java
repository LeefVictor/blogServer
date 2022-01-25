package com.zzj;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zzj.dao.ArticleDao;
import com.zzj.entity.Article;
import com.zzj.vo.HomeListOuterClass;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/hello")
public class ExampleResource {

    @Inject
    ArticleDao articleDao;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> hello(RoutingContext rc) {
        //MediaType.valueOf("application/x-protobuf");
        return articleDao.queryWithId(1).onItem().transform(Article::getAuthor);
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/buf")
    public Uni<List<Integer>> buf(RoutingContext rc) throws InvalidProtocolBufferException {
        return Multi.createFrom().items(1,2,3,4,5,6,7,8,9).onItem().transform(i->i+3)
                .collect().asList();
    }
}