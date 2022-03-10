package com.zzj.timer;

import com.zzj.dao.AnimeDao;
import com.zzj.dao.UploadImageDao;
import com.zzj.entity.UploadImage;
import com.zzj.service.ConfService;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zzj.constants.ApplicationConst.*;

@ApplicationScoped
public class AnimeImgDownloadTimer {
    private Logger logger = LoggerFactory.getLogger(AnimeImgDownloadTimer.class);

    private final String defalutPicName = "default_anime_img";

    private AtomicInteger running = new AtomicInteger(0);

    @Inject
    private ConfService confService;

    @Inject
    private AnimeDao animeDao;

    @Inject
    private UploadImageDao uploadImageDao;


    @Scheduled(every = "60s", delay = 20, delayUnit = TimeUnit.SECONDS)
    public void timer() {
        //配置上面禁止操作了
        if (confService.getBooleanConf(disable_download_timer)) {
            return;
        }
        if (!running.compareAndSet(0, 1)) {
            return;
        }

        animeDao.findUnDownloadImgData().onItem().transform(anime -> {
            String src = download(anime.getId(), anime.getImageUrl());
            anime.setImageUrl(src).setName("anime_" + anime.getId());
            if (src.equals(confService.getConf(anime_def_pic))) {
                anime.setName(defalutPicName); //因为下载失败的都是同一个默认图片，避免插入太多的相同冗余记录了
            }
            return anime;
        }).collect().asList().subscribe().with(list -> {
            if (list != null && !list.isEmpty()) {
                List<UploadImage> uploadImageList = list.stream()
                        .filter(f -> !defalutPicName.equals(f.getName()))
                        .map(m -> new UploadImage().setName(m.getName()).setWholeUrl(m.getImageUrl())).collect(Collectors.toList());

                if (!uploadImageList.isEmpty()) {
                    uploadImageDao.saveRecords(uploadImageList)
                            .subscribe().with(o -> {
                                logger.info("更改图片记录成功," + o);
                            });
                }

                animeDao.updateImgUrls(list).subscribe().with(o -> {
                    logger.info("更改图片记录成功," + o);
                });
                logger.info("done!!!");
            }
            running.set(0);
        });
    }

    private String download(long id, String url) {
        String src = confService.getConf(anime_def_pic);

        if (url == null) {
            return src;
        }

        String suffix = url.substring(url.lastIndexOf("."));
        if (suffix != null && !suffix.trim().isEmpty()) {
            String name = "anime/anime_" + id + suffix;
            Path path = Paths.get(confService.getConf(imageSavePath) + File.separator + name);
            if (!Files.exists(path)) {
                HttpClient client = HttpClient.newBuilder().build();
                HttpRequest.Builder builder = HttpRequest.newBuilder();
                try {
                    builder.GET().uri(new URI(url));
                    HttpResponse<InputStream> in = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
                    java.nio.file.Path savePath = Files.write(path,
                            in.body().readAllBytes(), StandardOpenOption.CREATE);
                    src = confService.getConf(imageServerUrl) + name;
                    try {
                        Files.setPosixFilePermissions(savePath, PosixFilePermissions.fromString("rwxrwxrwx"));
                    } catch (IOException e) {
                        logger.error("更改权限操作失败，忽略", e);
                    }

                } catch (Exception e) {
                    logger.error("下载失败", e);
                }
            } else {
                src = confService.getConf(imageServerUrl) + name;
            }

        }

        return src;
    }

}
