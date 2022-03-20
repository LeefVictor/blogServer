package com.zzj.entity;

import java.io.Serializable;

public class Contents extends SuperEntity<Contents> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String contentType;
    private String content;

    public String getContentType() {
        return contentType;
    }

    public Contents setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Contents setContent(String content) {
        this.content = content;
        return this;
    }
/*
    public static void main(String[] args) {
        *//*Thread thread = new Thread(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(3000);
                    System.out.println("live");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        System.out.println("xx");
        //TODO System.exit(0); //何时主进程结束了 ， 子线程也会中断？ exit会，设置了守护进程属性 .setDaemon也会
    *//*

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.out.println(cl.getParent());
        System.out.println(cl.getParent().g);
        System.out.println(cl.getParent());


    }*/
}
