package com.zzj.superior;


import com.zzj.superior.serv.InMemoryCache;
import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;

//类似aspect的做法
@CacheIt
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class CacheItInterceptor implements Serializable {

    private Logger logger = LoggerFactory.getLogger(CacheItInterceptor.class);

    @Inject
    InMemoryCache cache;

    @AroundInvoke
    Object cacheInvocation(InvocationContext context) {

        // ... before
        String key = context.getMethod().getAnnotation(CacheIt.class).name();
        if (StringUtil.isNullOrEmpty(key)) {
            key = "method:" + context.getMethod().getName();
        }

        return Optional.ofNullable(cache.read(key)).orElseGet(getRetInSupplier(key, context));

    }

    Supplier getRetInSupplier(String key, InvocationContext context) {
        return () -> {
            try {
                var res = context.proceed();
                Uni.createFrom().item(res).subscribe().with(obj -> {
                    cache.write(key, obj);
                });
                return res;
            } catch (Exception e) {
                logger.error("程序处理异常", e);
                return null;
            }
        };
    }

}
