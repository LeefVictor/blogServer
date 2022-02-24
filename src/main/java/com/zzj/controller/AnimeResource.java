package com.zzj.controller;

import com.zzj.service.ConfService;
import com.zzj.service.Serv4Web;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.zzj.constants.ApplicationConst.*;

@Path("anime")
public class AnimeResource {

    private Set<Long> errorSet = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(AnimeResource.class);

    @Inject
    private Serv4Web serv4Web;

    @Inject
    private ConfService confService;

    @GET
    @Produces("application/x-protobuf")
    @Path("/search")
    public Uni<byte[]> search(@QueryParam("keyword") String keyword,
                              @QueryParam("page") int page) {
        return serv4Web.animeSearch(keyword, new PageVO().setPage(page).setPageSize(16)).onItem().transform(search -> search.toByteArray());
    }

    @GET
    @Produces("application/x-protobuf")
    @Path("/detail/{id}")
    public Uni<byte[]> detail(@PathParam("id") long id) {
        return serv4Web.animeDetail(id).onItem().transform(detail -> detail.toByteArray());
    }

    //其他站的图片不能直接加载， 所以过一下后台
    @GET
    @Path("/pic/{id}")
    public Uni<String> pic(@PathParam("id") long id, @QueryParam("origin") String url) {
        //下载图片，逻辑就和后台上传的图片一样 不过命名方式不同
        if (!url.endsWith(".jpg") && !url.endsWith(".png")) {
            return Uni.createFrom().item(url);
        }
        String suffix = url.substring(url.lastIndexOf("."));
        String name = "anime_" + id + suffix;
        return serv4Web.queryImgWithName(name).onItem().transform(s -> {
            if (errorSet.contains(id)) {
                return confService.getConf(anime_def_pic);
            }
            if (s == null) {
                //upload and save
                HttpClient client = HttpClient.newBuilder().build();
                HttpRequest.Builder builder = HttpRequest.newBuilder();
                String src = confService.getConf(anime_def_pic);
                try {
                    builder.GET().uri(new URI(url));
                    HttpResponse<InputStream> in = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
                    java.nio.file.Path savePath = Files.write(Paths.get(confService.getConf(imageSavePath) + File.separator + name), in.body().readAllBytes());
                    src = confService.getConf(imageServerUrl) + name;
                    //TODO Files.setPosixFilePermissions(savePath, PosixFilePermissions.fromString("rwxrwxrwx"));

                } catch (Exception e) {
                    errorSet.add(id);
                    logger.error("上传失败", e);
                }
                //异步保存
                serv4Web.downloadImgAndUpdate(id, name, src);
                return src;
            } else {
                logger.info("直接返回" + s);
                return s;
            }
        });

    }


/*    @GET
    @Produces("image/png")
    @Path("/img")
    public Uni<byte[]> img(@QueryParam("origin") String url) {
        //下载图片，逻辑就和后台上传的图片一样 不过命名方式不同
        return Uni.createFrom().item(() -> {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            try {
                builder.GET().uri(new URI(url));
                HttpResponse<InputStream> in = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
                return in.body().readAllBytes();
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
    }*/

}
