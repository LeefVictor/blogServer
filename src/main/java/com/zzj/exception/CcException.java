package com.zzj.exception;

public class CcException extends Exception {

    public CcException() {
        super("请求异常");
    }

    public CcException(String message) {
        super(message);
    }
}
