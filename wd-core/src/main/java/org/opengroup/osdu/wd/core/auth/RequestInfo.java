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

import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequestScope
public class RequestInfo {

    @Autowired
    private HttpServletRequest httpRequest;

    private Map<String, String> headers;

    private DpsHeaders dpsHeaders;

    private static final String expectedHeaderValue = "true";

    @Autowired
    public RequestInfo(HttpServletRequest request) {
        this.httpRequest = request;
        buildHeaders();
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    public DpsHeaders getDpsHeaders(){
        if(this.dpsHeaders == null && this.httpRequest != null) {
            buildHeaders();
        }
        return this.dpsHeaders;
    }

    private  void buildHeaders() {
        this.headers = Collections.list(httpRequest
                .getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
        this.dpsHeaders = DpsHeaders.createFromMap(headers);
        this.dpsHeaders.addCorrelationIdIfMissing();
    }

    public void setHeaders(DpsHeaders headers) {
        dpsHeaders = headers;
    }

    public boolean isHttps() {
        return getUri().startsWith("https") ||
                "https".equalsIgnoreCase(httpRequest.getHeader("x-forwarded-proto"));
    }

    public String getUri() {
        StringBuilder requestURL = new StringBuilder(httpRequest.getRequestURL().toString());
        String queryString = httpRequest.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public String getUserIp() {
        return httpRequest.getRemoteAddr();
    }

    public String getUser() {
        return getDpsHeaders().getUserEmail();
    }
}
