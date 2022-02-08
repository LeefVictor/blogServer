package com.zzj.constants;

public class ApplicationConst {

    public final static String t_article = "articles";
    public final static String t_article2content = "article2contents";
    public final static String t_article2tags = "article2tags";
    public final static String t_comments = "comments";
    public final static String t_contents = "contents";
    public final static String t_system_conf = "system_conf";
    public final static String t_tags = "tags";
    public final static String t_uploadPhoto = "upload_image";
    public final static String t_blackList = "black_list";

    public final static String original = "原创";
    public final static String reprinted = "转载";

    public final static String author_desc_conf = "author_name";

    public final static String tokenConfName = "admin_token";
    public final static String imageSavePath = "image_save_path";
    public final static String imageServerUrl = "image_server_url";

    public static String getMainTag(int mainTag) {
        return mainTag == 1 ? original : reprinted;
    }

}
