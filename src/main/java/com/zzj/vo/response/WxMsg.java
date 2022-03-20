package com.zzj.vo.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WxMsg {

    private static final String str_ToUser = "ToUserName";
    private static final String str_FromUser = "FromUserName";
    private static final String str_CT = "CreateTime";
    private static final String str_MsgType = "MsgType";
    private static final String str_Content = "Content";
    private static final String str_MsgId = "MsgId";

    private static final String regTemplate = "%s>(.*?)\\</%s";
    /**
     * 接收人
     */
    private String ToUserName;

    /**
     * 发送人
     */
    private String FromUserName;

    private long CreateTime;

    private String MsgType;

    private String Content;

    /**
     * 唯一消息id
     */
    private String MsgId;

    public String getToUserName() {
        return ToUserName;
    }

    public WxMsg setToUserName(String toUserName) {
        ToUserName = toUserName;
        return this;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public WxMsg setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
        return this;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public WxMsg setCreateTime(long createTime) {
        CreateTime = createTime;
        return this;
    }

    public String getMsgType() {
        return MsgType;
    }

    public WxMsg setMsgType(String msgType) {
        MsgType = msgType;
        return this;
    }

    public String getContent() {
        return Content;
    }

    public WxMsg setContent(String content) {
        Content = content;
        return this;
    }

    public String getMsgId() {
        return MsgId;
    }

    public WxMsg setMsgId(String msgId) {
        MsgId = msgId;
        return this;
    }

    public static WxMsg parse(String xml) {
        WxMsg msg = new WxMsg();

        msg.setMsgId(match(xml, str_MsgId))
                .setContent(match(xml, str_Content))
                .setMsgType(match(xml, str_MsgType))
                .setFromUserName(match(xml, str_FromUser))
                .setToUserName(match(xml, str_ToUser));

        String ct = match(xml, str_CT);
        if (ct != null) {
            msg.setCreateTime(Long.parseLong(ct));
        }

        return msg;
    }

    private static String match(String xml, String field) {
        Pattern pattern = Pattern.compile(String.format(regTemplate, field, field));
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            String matchStr = matcher.group(1);//0则是包含了field
            //<![CDATA[ 是8长度
            if (matchStr.startsWith("<![CDATA["))
                return matchStr.substring(9, matchStr.lastIndexOf("]]"));
            else
                return matchStr;
        }
        return null;
    }

    public String toXmlString() {
        return null;
    }
}
