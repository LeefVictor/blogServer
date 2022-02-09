package com.zzj.enums;

public enum ArticleTypeEnum {
    blog("博客"),
    share("分享"),
    zaTan("杂谈"),
    xiaoShuo("小说"),
    pingCe("评测"),
    photo("图片");

    private String desc;

    public String getDesc() {
        return desc;
    }

    ArticleTypeEnum(String desc) {
        this.desc = desc;
    }
}
