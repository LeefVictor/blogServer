package com.zzj.controller;

import com.zzj.service.Serv4Web;
import com.zzj.vo.request.PageVO;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;

@Path("anime")
public class AnimeResource {

    private final Logger logger = LoggerFactory.getLogger(AnimeResource.class);

    @Inject
    private Serv4Web serv4Web;


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
