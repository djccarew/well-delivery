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

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CommonTest {

    @Test
    public void should_returnPrettyString_when_ObjectIsJsonValid() {
        Gson gson = new Gson();
        String str = "{'Name': 4324}";
        Object MyObject = gson.fromJson(str , Map.class);
        String actual = Common.toPrettyString(MyObject);
        assertEquals("{\n  \"Name\": 4324.0\n}", actual);
    }

    @Test
    public void should_returnId_when_EntityIdVersionValid() {
        String entityId = "fdsfhdjsfds";
        long version = 432984732;
        String actual = Common.buildId(entityId, version);
        assertEquals(entityId + ":" + version, actual);
    }

}
