package com.zzj.sentinelfallback;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 流控等熔断类
 */

@RegisterForReflection
public class DefaultFallback extends Fallback {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultFallback.class);

    //sentinel的处理逻辑可以之间看源码 com.alibaba.csp.sentinel.annotation.cdi.interceptor.AbstractSentinelInterceptorSupport， 大体上优先级就是 blockhandler->fallback->defaultfallback
    //另外， invoke的时候是会根据注解的方法上面的参数个数来进行写入的， 大概就是 比如a(int page)， 这里的fallback的参数就会是 fallback(int page, Exception), 所以注意促销无法invoke的时候是不是参数不对。
    //且函数名不能相同 com.alibaba.csp.sentinel.annotation.cdi.interceptor.ResourceMetadataRegistry.getKey

    //因为fallbackmethod 的参数需要和注解的方法参数个数 ，类型完全一致才可以， 而defaultFallback则仅需要一个exception参数， 所以这里只用defaultFallback

    //如果是非reactive的则不需要返回Uni
    public static Uni<String> strWithEx(Throwable ex) {
        LOGGER.error("fallback", ex);
        return Uni.createFrom().item(errorMsg);
    }

    public static Uni<byte[]> bytesWithEx(Throwable ex) {
        LOGGER.error("fallback", ex);
        return Uni.createFrom().item(errorMsg.getBytes(StandardCharsets.UTF_8));
    }


}
