package com.zzj.timer;

import com.zzj.dao.AnimeDao;
import com.zzj.dao.UploadImageDao;
import com.zzj.entity.UploadImage;
import com.zzj.service.ConfService;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zzj.constants.ApplicationConst.*;

@ApplicationScoped
public class AnimeImgDownloadTimer {
    private Logger logger = LoggerFactory.getLogger(AnimeImgDownloadTimer.class);

    private final String defalutPicName = "default_anime_img";

    private SSLContext context;

    private AtomicInteger running = new AtomicInteger(0);

    @Inject
    private ConfService confService;

    @Inject
    private AnimeDao animeDao;

    @Inject
    private UploadImageDao uploadImageDao;


    @Scheduled(every = "60s", delay = 20, delayUnit = TimeUnit.SECONDS)
    public void timer() {
        if (context == null) {
            initSSLContext();
        }
        //配置上面禁止操作了
        if (confService.getBooleanConf(disable_download_timer)) {
            return;
        }
        if (!running.compareAndSet(0, 1)) {
            return;
        }

        animeDao.findUnDownloadImgData().onItem().transform(anime -> {
            //这里即使是异步任务，也会执行完这个transform才会继续下去， 不需要担心异步问题
            download(anime.getId(), anime.getImageUrl(), src -> {
                anime.setImageUrl(src).setName("anime_" + anime.getId());
                if (src.equals(confService.getConf(anime_def_pic))) {
                    anime.setName(defalutPicName); //因为下载失败的都是同一个默认图片，避免插入太多的相同冗余记录了
                }
                logger.info("download完毕" + anime.getId());
            });
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

    private void download(long id, String url, Consumer<String> onItemCallback) {

        String defSrc = confService.getConf(anime_def_pic);
        String suffix = url.substring(url.lastIndexOf("."));

        String name = "anime/anime_" + id + suffix;
        Path path = Paths.get(confService.getConf(imageSavePath) + File.separator + name);

        Uni.createFrom().item(() -> {
            if (url == null) {
                return null;
            }
            try {
                HttpClient client = HttpClient.newBuilder().sslContext(context).build();
                HttpRequest.Builder builder = HttpRequest.newBuilder();
                builder.GET().uri(new URI(url));
                return client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).onItem().transform(in -> {
            if (in == null) {
                return defSrc;
            }
            if (!Files.exists(path)) {
                try {
                    java.nio.file.Path savePath = Files.write(path,
                            in.body().readAllBytes(), StandardOpenOption.CREATE);
                    Files.setPosixFilePermissions(savePath, PosixFilePermissions.fromString("rwxrwxrwx"));
                } catch (IOException e) {
                    logger.error("文件写入操作失败，", e);
                    return defSrc;
                }
            }
            return confService.getConf(imageServerUrl) + name;
        }).subscribe().with(onItemCallback);
    }

    private void initSSLContext() {
        try {
            context = SSLContext.getInstance("TLS");
            context.init(
                    null,
                    new TrustManager[]
                            {
                                    new X509ExtendedTrustManager() {
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return null;
                                        }

                                        public void checkClientTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type) {
                                        }

                                        public void checkServerTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type) {
                                        }

                                        public void checkClientTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type,
                                                final Socket a_socket) {
                                        }

                                        public void checkServerTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type,
                                                final Socket a_socket) {
                                        }

                                        public void checkClientTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type,
                                                final SSLEngine a_engine) {
                                        }

                                        public void checkServerTrusted(
                                                final X509Certificate[] a_certificates,
                                                final String a_auth_type,
                                                final SSLEngine a_engine) {
                                        }
                                    }
                            },
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
