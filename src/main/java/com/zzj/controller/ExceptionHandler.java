package com.zzj.controller;

import com.google.common.net.PercentEscaper;
import com.zzj.exception.BlackIPException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<BlackIPException> {

    private PercentEscaper escaper = new PercentEscaper("", false);

    @Override
    public Response toResponse(BlackIPException e) {

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("error", escaper.escape(e.getMessage()))
                .build();
    }
}
