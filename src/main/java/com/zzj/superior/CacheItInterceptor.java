package com.zzj.superior;


import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

//类似aspect的做法
@CacheIt
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class CacheItInterceptor implements Serializable {

    @Inject
    ReactiveRedisClient reactiveRedisClient;

    @AroundInvoke
    Object cacheInvocation(InvocationContext context) throws Exception {
        // ... before

        Uni ret = (Uni) context.proceed();
        System.out.println(context.getMethod().getAnnotation(CacheIt.class).name());
        // ... after
        ret.subscribe().with(o -> {
            //System.out.println(o);
            //cache it ,and async
        });
        return ret;
    }

}
