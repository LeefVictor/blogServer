package com.zzj.utils;

public class ContentUtils {

    //过长的文本裁减
    public static String subString(String content, int length){
        if (content.length() <= length) {
            return content;
        }
        return content.substring(0, length) + "....";
    }
}
