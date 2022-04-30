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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class RequestInfoTests {
    @Mock
    private HttpServletRequest http;

    private RequestInfo sut;

    private Map<String, String> httpHeaders;


    @Before
    public void setup() {
        httpHeaders = new HashMap<>();
        httpHeaders.put("authorization", "aaa");
        httpHeaders.put("correlation-id", "cor123");
        httpHeaders.put("data-partition-id", "123");
        httpHeaders.put("content-type", "67890");
        when(http.getHeaderNames()).thenReturn(Collections.enumeration(httpHeaders.keySet()));
        httpHeaders.forEach((k,v) ->  when(http.getHeader(k)).thenReturn(v));
        sut = new RequestInfo(http);
    }

    @Test
    public void should_includeAllHeadersExcept_when_creatingHeaders() {
        Map<String,String> map = this.sut.getHeaders();
        assertEquals("cor123", map.get("correlation-id"));
        assertEquals("123", map.get("data-partition-id"));
        assertEquals("aaa", map.get("authorization"));
        assertEquals("67890", map.get("content-type"));
    }

    @Test
    public void should_includeAllHeadersExcept_when_creatingDpsHeaders() {
        DpsHeaders map = this.sut.getDpsHeaders();
        assertEquals("cor123", map.getCorrelationId());
        assertEquals("123", map.getPartitionId());
        assertEquals("aaa", map.getAuthorization());
        assertEquals("", map.getHeaders().getOrDefault("context", ""));
    }

}
