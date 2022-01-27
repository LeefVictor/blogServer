package com.zzj.superior;


import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

//类似aspect的做法
@CacheIt
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class CacheItInterceptor implements Serializable {


    @AroundInvoke
    Object cacheInvocation(InvocationContext context) throws Exception {

        //TODO 目前的数据量不需要缓存，搁置
        // ... before
        //System.out.println(context.getMethod().getAnnotation(CacheIt.class).name());
        // ... after
        //ret.subscribe().with(o -> {
        //System.out.println(o);
        //cache it ,and async
        //});
        return context.proceed();
    }

}
