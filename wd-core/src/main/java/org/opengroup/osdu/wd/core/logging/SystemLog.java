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

package org.opengroup.osdu.wd.core.logging;

import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SystemLog implements ILogger {
    private static final Logger logger = Logger.getLogger(SystemLog.class.getName());

    @Autowired
    RequestInfo requestInfo;

    @Override
    public void info(String msg) {
        logger.log(Level.INFO, generateLogMessage(msg, null));
    }

    @Override
    public void warning(String msg, Exception e) {
        logger.log(Level.WARNING, generateLogMessage(msg, e));
    }

    @Override
    public void error(String msg, Exception e) {
        logger.log(Level.SEVERE, generateLogMessage(msg, e));
    }

    String generateLogMessage(String msg, Exception e){
        String corId = requestInfo.getDpsHeaders().getCorrelationId();
        return String.format("CorrelationId: %s   Message: %s    Error: %s", corId, msg, e == null ? "" : e.toString());
    }

    @Override
    public void close() throws Exception {

    }
}
