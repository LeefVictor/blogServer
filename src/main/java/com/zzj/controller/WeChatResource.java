package com.zzj.controller;

import com.google.common.hash.Hashing;
import com.zzj.constants.ApplicationConst;
import com.zzj.service.ConfService;
import com.zzj.service.ServWechat;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("wx")
public class WeChatResource {

    private final Logger logger = LoggerFactory.getLogger(WeChatResource.class);

    @Inject
    private ConfService confService;
    @Inject
    private ServWechat servWechat;

    @Produces(MediaType.TEXT_PLAIN)//必须配置这个content-type
    @GET
    @Path("accept")
    public Uni<String> validate(@QueryParam("signature") String signature,
                                @QueryParam("timestamp") String timestamp,
                                @QueryParam("nonce") String nonce,
                                @QueryParam("echostr") String echostr) {

        List<String> array = new ArrayList<>(3);
        array.add(confService.getConf(ApplicationConst.wxToken));
        array.add(timestamp);
        array.add(nonce);

        Collections.sort(array);
        String afterSort = String.join("", array.toArray(new String[0]));
        String afterSha1 = Hashing.sha1().newHasher()
                .putString(afterSort, StandardCharsets.UTF_8).hash().toString();
        return Uni.createFrom().item(() ->
                //确认是来自微信
                afterSha1.equals(signature) ? echostr : "Error");
    }

    @POST
    @Path("accept")
    public Uni<String> accept(String body) {
        servWechat.toDb(body);
        logger.info("return ");
        return Uni.createFrom().item("");
    }
}
