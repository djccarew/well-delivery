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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DateTimeUtil {

    private static final Map<String, DateTimeFormatter> DATE_FORMAT_REGS_STD = new HashMap<String, DateTimeFormatter>() {{
        put("^\\d{8}$", DateTimeFormatter.BASIC_ISO_DATE);  //yyyyMMdd
        put("^\\d{4}-\\d{2}-\\d{2}$", DateTimeFormatter.ISO_LOCAL_DATE);  //yyyy-MM-dd
        put("^\\d{4}-\\d{2}-\\d{2}[\\+\\-]\\d{2}:\\d{2}$", DateTimeFormatter.ISO_OFFSET_DATE);  //yyyy-MM-dd+xx:xx
    }};

    private static final Map<String, String> DATE_FORMAT_REGS_USD = new HashMap<String, String>() {{
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-M-d");
    }};

    private static final Map<String, DateTimeFormatter> TIME_FORMAT_REGS_STD = new HashMap<String, DateTimeFormatter>() {{
        put("^\\d{2}:\\d{2}:\\d{2}$", DateTimeFormatter.ISO_LOCAL_TIME);  //HH:mm:ss
        put("^\\d{2}:\\d{2}:\\d{2}[\\+\\-]\\d{2}:\\d{2}$", DateTimeFormatter.ISO_OFFSET_TIME); //HH:mm:ss+xx:xx
    }};

    private static final Map<String, String> TIME_FORMAT_REGS_USD = new HashMap<String, String>() {{
        put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "H:m:s");
    }};

    private static final Map<String, DateTimeFormatter> DATETIME_FORMAT_REGS_STD = new HashMap<String, DateTimeFormatter>() {{
        put("^\\d{4}-\\d{2}-\\d{2}t\\d{2}:\\d{2}:\\d{2}$", DateTimeFormatter.ISO_LOCAL_DATE_TIME); //yyyy-MM-ddTHH:mm:ss
        put("^\\d{4}-\\d{2}-\\d{2}t\\d{2}:\\d{2}:\\d{2}[\\+\\-]\\d{2}:\\d{2}$", DateTimeFormatter.ISO_OFFSET_DATE_TIME); //yyyy-MM-ddTHH:mm:ss+xx:xx
        put("^[a-z]{3},\\s\\d{2}\\s[a-z]{3}\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\s[A-Z]{3}$", DateTimeFormatter.RFC_1123_DATE_TIME);
    }};

    private static final Map<String, DateTimeFormatter> INSTANT_FORMAT_REGS_STD = new HashMap<String, DateTimeFormatter>() {{
        put("^\\d{4}-\\d{2}-\\d{2}t\\d{2}:\\d{2}:\\d{2}z$", DateTimeFormatter.ISO_INSTANT);
    }};

    public static LocalDateTime parse(String dateTimeString) {
        for (String key : DATETIME_FORMAT_REGS_STD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = DATETIME_FORMAT_REGS_STD.get(key);
                return LocalDateTime.parse(dateTimeString, format);
            }
        }
        for (String key : DATE_FORMAT_REGS_STD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = DATE_FORMAT_REGS_STD.get(key);
                return LocalDate.parse(dateTimeString, format).atStartOfDay();
            }
        }
        for (String key : DATE_FORMAT_REGS_USD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_FORMAT_REGS_USD.get(key));
                return LocalDate.parse(dateTimeString, format).atStartOfDay();
            }
        }
        for (String key : INSTANT_FORMAT_REGS_STD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = INSTANT_FORMAT_REGS_STD.get(key);
                Instant instant = Instant.from(format.parse(dateTimeString));
                return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
            }
        }
        for (String key : TIME_FORMAT_REGS_STD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = TIME_FORMAT_REGS_STD.get(key);
                return LocalTime.parse(dateTimeString, format).atDate(LocalDate.now());

            }
        }
        for (String key : TIME_FORMAT_REGS_USD.keySet()) {
            if (dateTimeString.toLowerCase().matches(key.toLowerCase())) {
                DateTimeFormatter format = DateTimeFormatter.ofPattern(TIME_FORMAT_REGS_USD.get(key));
                return LocalTime.parse(dateTimeString, format).atDate(LocalDate.now());
            }
        }
        return null; // Unknown format.
    }

    public static LocalDateTime parseWithException(String dateTimeString) throws Exception {
        LocalDateTime dateTime = parse(dateTimeString);
        if (dateTime != null)
            return dateTime;
        else
            throw new Exception("Unknown format"); // Unknown format.
    }
}
