package com.zzj.superior;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.zzj.dao.BlackListDao;
import com.zzj.exception.BlackIPException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Context;
import java.nio.charset.Charset;


//不需要自动加黑名单，一来nginx限速每s5次，基本上杜绝了
@IPValid
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class IPValidInterceptor {

    private final Logger log = LoggerFactory.getLogger(IPValidInterceptor.class);
    @Context
    HttpServerRequest request;
    //预期1w的量， 0.0001的误判率
    private BloomFilter<String> filter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()), 100 * 100, 0.0001);
    @Inject
    private BlackListDao blackListDao;

    private volatile int filterInit = 0;


    @AroundInvoke
    Object ipValidInterceptor(InvocationContext context) throws Exception {
        // ... before
        String ip = getIp();
        if (filter.mightContain(getIp())) {
            throw new BlackIPException();
        }
        if (filterInit == 0) {
            filterInit = 1;
            init();
        }
        // ... after
        return context.proceed();
    }

    private void init() {
        blackListDao.queryWithCondition(" ", Tuple.tuple(), "ip")
                .subscribe().with(blackList1 -> {
                    filter.put(blackList1.getIp());
                });
    }

    private String getIp() {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.remoteAddress().hostAddress();
        }
        return ip;
    }
}
