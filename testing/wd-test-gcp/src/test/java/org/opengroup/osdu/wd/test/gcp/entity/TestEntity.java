/*
 *   Copyright 2020-2021 Google LLC
 *   Copyright 2020-2021 EPAM Systems, Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.opengroup.osdu.wd.test.gcp.entity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opengroup.osdu.wd.test.gcp.util.GoogleTestUtils;
import org.opengroup.osdu.wd.test.core.entity.EntityTest;

public class TestEntity extends EntityTest {
    private static final GoogleTestUtils GOOGLE_TEST_UTILS = new GoogleTestUtils();

    @BeforeClass
    public static void classSetup() throws Exception {
        EntityTest.classSetup(GOOGLE_TEST_UTILS.getToken());
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        EntityTest.classTearDown(GOOGLE_TEST_UTILS.getToken());
    }

    @Before
    @Override
    public void setup() throws Exception {
        this.testUtils = new GoogleTestUtils();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        this.testUtils = null;
    }
}
