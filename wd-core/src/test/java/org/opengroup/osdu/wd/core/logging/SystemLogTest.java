package org.opengroup.osdu.wd.core.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SystemLog.class, Logger.class})
public class SystemLogTest {
    @Mock
    Logger logger;
    @Mock
    RequestInfo requestInfo;
    @Mock
    DpsHeaders headers;

    SystemLog sut;

    @Before
    public void setup() {
        Mockito.when(headers.getCorrelationId()).thenReturn("correlation-id");
        Mockito.when(headers.getAuthorization()).thenReturn("Bearer 123456");
        Mockito.when(requestInfo.getDpsHeaders()).thenReturn(headers);

        Logger logger = mock(Logger.class);
        mockStatic(Logger.class);
        when(Logger.getLogger(SystemLog.class.getName())).thenReturn(logger);

        sut = new SystemLog();
        sut.requestInfo = requestInfo;
    }

    @Test
    public void should_returnSuccess_when_info(){
        doNothing().when(logger).log(eq(Level.INFO), anyString());
        sut.info("test");
        //verify(logger).log(any(Level.class), anyString());
    }

    @Test
    public void should_returnSuccess_when_warning(){
        doNothing().when(logger).log(eq(Level.WARNING), anyString());
        sut.warning("test", new Exception());
        //verify(logger).log(any(Level.class), anyString());
    }

    @Test
    public void should_returnSuccess_when_error(){
        doNothing().when(logger).log(eq(Level.SEVERE), anyString());
        sut.error("test", new Exception());
        //verify(logger).log(any(Level.class), anyString());
    }
}
