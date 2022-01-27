package com.zzj.superior;


import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@CacheInvalid
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class CacheInvalidInterceptor implements Serializable {

    //@Inject
    //ReactiveRedisClient reactiveRedisClient;

    @AroundInvoke
    Object cacheInvalidInterceptor(InvocationContext context) throws Exception {
        // ... before

        Object ret = context.proceed();
        System.out.println("invalid");
        // ... after
        return ret;
    }

}
