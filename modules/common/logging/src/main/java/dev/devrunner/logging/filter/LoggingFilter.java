package dev.devrunner.logging.filter;

import dev.devrunner.logging.LogContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that automatically sets logging context for API requests
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            try {
                // Set API context
                String endpoint = httpRequest.getRequestURI();
                String method = httpRequest.getMethod();
                LogContext.setApiContext(endpoint, method);

                log.debug("API request started: {} {}", method, endpoint);

                chain.doFilter(request, response);

                log.debug("API request completed: {} {}", method, endpoint);
            } finally {
                // Clear context after request completes
                LogContext.clearApiContext();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
