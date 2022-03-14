package com.zzj.controller;

import com.google.common.net.PercentEscaper;
import com.zzj.exception.CcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<CcException> {

    private Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private PercentEscaper escaper = new PercentEscaper("", false);

    @Override
    public Response toResponse(CcException e) {
        logger.error("处理异常", e);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("error", escaper.escape(e.getMessage()))
                .build();
    }
}
