/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.test.ibm.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;



import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class IBMServicePrincipal {
	  private static String url;
	    private static String realm;
		private static String client_id;
		private static String client_secret;
		private static String grant_type = "password";

		static {
		    url = System.getProperty("KEYCLOAK_URL", System.getenv("KEYCLOAK_URL"));
		    realm = System.getProperty("KEYCLOAK_REALM", System.getenv("KEYCLOAK_REALM"));
		    client_id = System.getProperty("KEYCLOAK_CLIENT_ID",System.getenv("KEYCLOAK_CLIENT_ID"));
		    client_secret = System.getProperty("KEYCLOAK_CLIENT_SECRET",System.getenv("KEYCLOAK_CLIENT_SECRET"));
		}

		public static String getToken(String user, String pwd) throws IOException {
			String token_endpoint = String.format("https://%s/auth/realms/%s/protocol/openid-connect/token", url, realm);
	        URL url = new URL(token_endpoint);
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

	        Map<String, String> parameters = new HashMap<>();
	        parameters.put("grant_type", grant_type);
	        parameters.put("client_id", client_id);
	        parameters.put("client_secret", client_secret);
	        parameters.put("username", user);
	        parameters.put("password", pwd);

	        con.setDoOutput(true);
	        DataOutputStream out = new DataOutputStream(con.getOutputStream());
	        out.writeBytes(getParamsString(parameters));
	        out.flush();
	        out.close();

	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(con.getInputStream()));
	        String inputLine;
	        StringBuffer content = new StringBuffer();
	        while ((inputLine = in.readLine()) != null) {
	            content.append(inputLine);
	        }
	        in.close();

	        con.disconnect();

	        Gson gson = new Gson();
	        JsonObject jobj = gson.fromJson(content.toString(), JsonObject.class);
	        String token = jobj.get("access_token").getAsString();
	        return token;
		}

		private static String getParamsString(Map<String, String> params)
	            throws UnsupportedEncodingException {
	        StringBuilder result = new StringBuilder();

	        for (Map.Entry<String, String> entry : params.entrySet()) {
	            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
	            result.append("=");
	            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	            result.append("&");
	        }

	        String resultString = result.toString();
	        return resultString.length() > 0
	                ? resultString.substring(0, resultString.length() - 1)
	                : resultString;
	    }
}
