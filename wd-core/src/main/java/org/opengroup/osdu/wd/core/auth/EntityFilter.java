// Copyright 2020 Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.wd.core.auth;

import org.opengroup.osdu.core.client.http.ResponseHeaders;
import org.opengroup.osdu.wd.core.logging.ILogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class EntityFilter implements Filter {

    @Autowired
    private RequestInfo requestInfo;

    @Autowired
    ILogger log;

    @Value("${ACCEPT_HTTP:false}")
    private boolean acceptHttp;

    @Override
    public void init(FilterConfig filterConfig) {
        //do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        requestInfo.getDpsHeaders().addCorrelationIdIfMissing();

        Map<String, List<Object>> standardHeaders = ResponseHeaders.STANDARD_RESPONSE_HEADERS;
        for (Map.Entry<String, List<Object>> header : standardHeaders.entrySet()) {
            httpServletResponse.addHeader(header.getKey(), header.getValue().toString());
        }

        long startTime = System.currentTimeMillis();
        try {
            if (!validateIsHttps(httpServletResponse)) {
                //do nothing
            } else if (httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
                httpServletResponse.setStatus(200);
            } else {
                chain.doFilter(servletRequest, servletResponse);
            }
        } finally {
            logRequest(httpServletRequest, httpServletResponse, startTime);
        }
    }

    @Override
    public void destroy() { }

    private boolean validateIsHttps( HttpServletResponse httpServletResponse) {
        String uri = requestInfo.getUri();
        if (isSwagger(uri))
            return true;

        if(uri.endsWith("/warmup"))
            return true;

        if (!hasJwt()) {
            httpServletResponse.setStatus(401);
            return false;
        }

        if (!isLocalHost(uri) && !requestInfo.isHttps() && !isAcceptHttp()) {
            String location = uri.replaceFirst("http", "https");
            httpServletResponse.setStatus(307);
            httpServletResponse.addHeader("location", location);
            return false;
        }

        return  true;
    }

    private boolean hasJwt() {
        String authorization = requestInfo.getDpsHeaders().getAuthorization();
        return (authorization != null) && (authorization.length() > 0);
    }

    private boolean isLocalHost(String uri) {
        return (uri.contains("//localhost") || uri.contains("//127.0.0.1"));
    }

    private boolean isSwagger(String uri) {
        return uri.contains("/swagger") || uri.contains("/v2/api-docs") || uri.contains("/configuration/ui") || uri.contains("/webjars/");
    }

    private void logRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse, long startTime) {
        String uri = requestInfo.getUri();
        if(!uri.endsWith("/warmup")) {
            String info = String.format("requestMethod=%s latency=%s requestUrl=%s Status=%s ip=%s",
                    servletRequest.getMethod(),
                    Duration.ofMillis(System.currentTimeMillis() - startTime),
                    uri,
                    servletResponse.getStatus(),
                    servletRequest.getRemoteAddr());
            log.info(info);
        }
    }

    public boolean isAcceptHttp() {
        return acceptHttp;
    }
}
