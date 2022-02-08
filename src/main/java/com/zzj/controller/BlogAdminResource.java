package com.zzj.controller;

import com.zzj.service.ConfService;
import com.zzj.service.Serv4Admin;
import com.zzj.service.Serv4Web;
import com.zzj.superior.TokenInvalid;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jboss.resteasy.reactive.MultipartForm;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static com.zzj.constants.ApplicationConst.imageSavePath;
import static com.zzj.constants.ApplicationConst.imageServerUrl;

@Path("admin")
public class BlogAdminResource {

    @Inject
    private Serv4Admin serv4Admin;

    @Inject
    private Serv4Web serv4Web;

    @Inject
    private ConfService confService;

    @POST
    @Path("validate")
    public Uni<Boolean> validate(@HeaderParam("authToken") String token) {
        return serv4Admin.validateToken(token);
    }

    @GET
    @Path("conf/list")
    @Produces(MediaType.APPLICATION_JSON)
    @TokenInvalid
    public Uni<JsonArray> confList() {
        return confService.allConf();
    }

    @POST
    @Path("conf/save")
    @TokenInvalid
    public Uni<Boolean> confSave(@FormParam("name") String name, @FormParam("value") String value, @FormParam("type") String type) {
        return confService.save(name, value, type);
    }

    @GET
    @Path("comments/list")
    @Produces("application/x-protobuf")
    @TokenInvalid
    public Uni<byte[]> commentsList() {
        return serv4Admin.queryComments().onItem().transform(comments -> comments.toByteArray());
    }

    @POST
    @Path("comments/reply")
    @TokenInvalid
    public Uni<Boolean> commentsReply(@FormParam("id") long id, @FormParam("reply") String reply, @FormParam("sharp") boolean sharp) {
        return serv4Admin.commentReply(id, reply, sharp);
    }

    @GET
    @Produces("application/x-protobuf")
    @Path("/home/{page}")
    @TokenInvalid
    public Uni<byte[]> homeList(@PathParam("page") int page) {
        return serv4Admin.homeList(new PageVO().setPageSize(10).setPage(page)).onItem().transform(homeList -> homeList.toByteArray());
    }


    @GET
    @Produces("application/x-protobuf")
    @Path("/detail/{id}")
    @TokenInvalid
    public Uni<byte[]> detail(@PathParam("id") long articleId) {
        return serv4Web.detail(articleId).onItem().transform(detail -> detail.toByteArray());
    }


    @Path("/save")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @TokenInvalid
    public Uni save(JsonObject jsonObject) {
        return serv4Admin.save(jsonObject);
    }


    //https://quarkus.io/guides/resteasy-reactive#handling-multipart-form-data
    @Path("uploadImage")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @TokenInvalid
    public Response uploadFile(@MultipartForm FormData formData) throws IOException { //TODO 这里的踩坑点 formData不能是内部类
        String suffix = formData.file.fileName().substring(formData.file.fileName().lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replaceAll("-", "")
                .substring(0, 10) + suffix;
        File save = new File(confService.getConf(imageSavePath) + File.separator + fileName);
        Files.copy(formData.file.uploadedFile(), Paths.get(save.getAbsolutePath()));
        String src = confService.getConf(imageServerUrl) + fileName;

        //异步保存
        serv4Admin.saveUploadRecord(fileName, src);
        return Response.status(200)
                .entity(src).build();

    }


}
