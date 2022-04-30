/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.test.ibm.entity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opengroup.osdu.wd.test.ibm.util.IBMTestUtils;
import org.opengroup.osdu.wd.test.core.entity.EntityTest;

public class TestEntity extends EntityTest {
    private static final IBMTestUtils ibmTestUtils = new IBMTestUtils();

    @BeforeClass
    public static void classSetup() throws Exception {
        EntityTest.classSetup(ibmTestUtils.getToken());
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        EntityTest.classTearDown(ibmTestUtils.getToken());
    }

    @Before
    @Override
    public void setup() throws Exception {
        this.testUtils = new IBMTestUtils();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        this.testUtils = null;
    }
}
