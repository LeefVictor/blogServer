package com.zzj.superior;

import io.smallrye.mutiny.Uni;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@IPValid
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class IPValidInterceptor {


    @AroundInvoke
    Object cacheInvocation(InvocationContext context) throws Exception {
        // ... before
        // 检测拦截ip
        Uni ret = (Uni) context.proceed();
        // ... after

        return ret;
    }
}
