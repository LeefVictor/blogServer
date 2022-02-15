package com.zzj.superior;


import com.zzj.constants.ApplicationConst;
import com.zzj.exception.CcException;
import com.zzj.service.ConfService;
import io.vertx.core.http.HttpServerRequest;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Context;
import java.io.Serializable;
import java.util.Optional;

@TokenInvalid
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class TokenInvalidInterceptor implements Serializable {

    @Context
    HttpServerRequest request;

    @Inject
    private ConfService confService;

    @AroundInvoke
    Object tokenInvalidInterceptor(InvocationContext context) throws Exception {
        // ... before
        //TODO NGINX header 不支持下划线的， 要注意，踩了坑了, 同时 413状态码时优先看下是不是nginx限制了， 加上client_max_body_size 4M;
        Optional<String> optional = Optional.ofNullable(request.getHeader("authToken"));
        if (optional.isEmpty() || !confService.getConf(ApplicationConst.tokenConfName).equals(optional.get())) {
            throw new CcException("口令有误，请重新登录");
        }
        // ... after
        return context.proceed();

    }

}
