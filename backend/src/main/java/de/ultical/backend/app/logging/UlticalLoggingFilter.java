package de.ultical.backend.app.logging;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

import org.slf4j.MDC;

public class UlticalLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String MDC_KEY_REMOTE_IP = "remoteIp";
    @Context
    HttpServletRequest servletReq;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MDC.put(MDC_KEY_REMOTE_IP, this.servletReq.getRemoteAddr());

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        MDC.remove(MDC_KEY_REMOTE_IP);

    }

}
