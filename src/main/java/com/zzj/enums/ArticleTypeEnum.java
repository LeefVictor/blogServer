package com.zzj.enums;

public enum ArticleTypeEnum {
    blog("博客"),
    photo("图片");

    private String desc;

    public String getDesc() {
        return desc;
    }

    ArticleTypeEnum(String desc){
        this.desc = desc;
    }
}
