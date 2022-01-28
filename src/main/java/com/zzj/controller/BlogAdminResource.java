package com.zzj.controller;

import com.zzj.service.Serv4Admin;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
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

@Path("admin")
public class BlogAdminResource {

    @Inject
    private Serv4Admin serv4Admin;

    @GET
    @Produces("application/x-protobuf")
    @Path("/home/{page}")
    public Uni<byte[]> homeList(@PathParam("page") int page) {
        return serv4Admin.homeList(new PageVO().setPageSize(10).setPage(page)).onItem().transform(homeList -> homeList.toByteArray());
    }


    //https://quarkus.io/guides/resteasy-reactive#handling-multipart-form-data
    @Path("uploadImage")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFile(@HeaderParam("token") String token,
                               @MultipartForm FormData formData) throws IOException { //TODO 这里的踩坑点 formData不能是内部类

        String suffix = formData.file.fileName().substring(formData.file.fileName().lastIndexOf("."));

        String fileName = UUID.randomUUID().toString().replaceAll("-", "")
                .substring(0, 10) + suffix;

        File save = new File("D:\\self-soft\\nginx-1.18.0\\html\\img\\" + fileName);


        Files.copy(formData.file.uploadedFile(), Paths.get(save.getAbsolutePath()));

        String src = "http://localhost/img/" + fileName;
        //constructs upload file path


        return Response.status(200)
                .entity(src).build();

    }


}
