package com.zzj.entity;

import java.io.Serializable;

/**
 * anime_base anime 视图
 *
 * @author
 */
public class Anime extends SuperEntity<Anime> implements Serializable {

    private String name;

    private String imageUrl;

    /**
     * 共多少集
     */
    private Integer total;
    private Integer episode;

    private Integer isFinish;

    private String siteName;
    private String url;


    private static final long serialVersionUID = 1L;


    public String getName() {
        return name;
    }

    public Anime setName(String name) {
        this.name = name;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Anime setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Integer getEpisode() {
        return episode;
    }

    public Anime setEpisode(Integer episode) {
        this.episode = episode;
        return this;
    }

    public Integer getTotal() {
        return total;
    }

    public Anime setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public String getSiteName() {
        return siteName;
    }

    public Anime setSiteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Anime setUrl(String url) {
        this.url = url;
        return this;
    }

    public Integer getIsFinish() {
        return isFinish;
    }

    public Anime setIsFinish(Integer isFinish) {
        this.isFinish = isFinish;
        return this;
    }
}