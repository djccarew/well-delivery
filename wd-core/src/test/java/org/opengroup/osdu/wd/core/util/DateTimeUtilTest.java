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

package org.opengroup.osdu.wd.core.util;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DateTimeUtilTest {

    @Test
    public void should_returnLocalDateTime_when_Input_yyyyMMdd() {
        String dateTimeString = "20201007";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        assertEquals(LocalDateTime.of(2020, 10, 7, 0, 0), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_Input_yyyyMd() {
        String dateTimeString = "2020-1-7";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        assertEquals(LocalDateTime.of(2020, 1, 7, 0, 0), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_Input_HHmmss() {
        String dateTimeString = "10:09:05";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        LocalDateTime now = LocalDateTime.now();
        assertEquals(LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 10, 9, 5), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_Input_Hms() {
        String dateTimeString = "10:9:5";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        LocalDateTime now = LocalDateTime.now();
        assertEquals(LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 10, 9, 5), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_Input_yyyyMMddTHHmmss() {
        String dateTimeString = "2020-01-07T10:09:05";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        assertEquals(LocalDateTime.of(2020, 1, 7, 10, 9, 5), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_Input_yyyyMMddTHHmmssZ() {
        String dateTimeString = "2020-01-07T10:09:05Z";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        assertEquals(LocalDateTime.of(2020, 1, 7, 10, 9, 5), actual);
    }

    @Test
    public void should_returnLocalDateTime_when_InputInvalid() {
        String dateTimeString = "2020107";
        LocalDateTime actual = DateTimeUtil.parse(dateTimeString);
        assertEquals(null, actual);
    }

    @Test
    public void should_returnLocalDateTime_when_InputValid_to_parseWithException() {
        String dateTimeString = "2020-10-7";
        try {
            LocalDateTime actual = DateTimeUtil.parseWithException(dateTimeString);
            assertEquals(LocalDateTime.of(2020, 10, 07, 0, 0), actual);
        }catch (Exception ex) {
            fail("Test failed.");
        }
    }

    @Test
    public void should_returnException_when_InputInvalid_to_parseWithException() {
        String dateTimeString = "2020:10-7";
        try {
            LocalDateTime actual = DateTimeUtil.parseWithException(dateTimeString);
            fail("Test failed.");
        } catch (Exception ex) {
            assert (ex.getMessage().equals("Unknown format"));
        }
    }
}
