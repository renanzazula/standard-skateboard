package com.skateboard.podcast.standard.service.container.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final String traceId = resolveTraceId(request);
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

    private static String resolveTraceId(final HttpServletRequest request) {
        final String fromTraceHeader = request.getHeader(TRACE_ID_HEADER);
        if (fromTraceHeader != null && !fromTraceHeader.isBlank()) {
            return fromTraceHeader.trim();
        }
        final String fromRequestHeader = request.getHeader(REQUEST_ID_HEADER);
        if (fromRequestHeader != null && !fromRequestHeader.isBlank()) {
            return fromRequestHeader.trim();
        }
        return UUID.randomUUID().toString();
    }
}
