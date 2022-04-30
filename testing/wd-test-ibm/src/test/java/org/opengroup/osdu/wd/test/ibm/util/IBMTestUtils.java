/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.test.ibm.util;


import java.io.IOException;

import org.opengroup.osdu.wd.test.core.util.TestUtils;

import com.google.common.base.Strings;

public class IBMTestUtils extends TestUtils {
	private static String token;
	private static String noDataAccesstoken;

	@Override
	public synchronized String getToken() throws Exception {
		if (Strings.isNullOrEmpty(token)) {
			//token = IdentityClient.getTokenForUserWithAccess();
			try {
	        	String user = System.getProperty("AUTH_USER_ACCESS", System.getenv("AUTH_USER_ACCESS"));
	        	String pass = System.getProperty("AUTH_USER_ACCESS_PASSWORD",System.getenv("AUTH_USER_ACCESS_PASSWORD"));
				token= IBMServicePrincipal.getToken(user, pass);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return "Bearer " + token;
	}

	@Override
	public synchronized String getNoDataAccessToken() throws Exception {
		if (Strings.isNullOrEmpty(noDataAccesstoken)) {
			//noDataAccesstoken = IdentityClient.getTokenForUserWithNoAccess();
			try {
	        	String user = System.getProperty("AUTH_USER_NO_ACCESS",System.getenv("AUTH_USER_ACCESS"));
	        	String pass = System.getProperty("AUTH_USER_NO_ACCESS_PASSWORD",System.getenv("AUTH_USER_NO_ACCESS_PASSWORD"));
	        	noDataAccesstoken= IBMServicePrincipal.getToken(user, pass);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return "Bearer " + noDataAccesstoken;
	}
	
}
