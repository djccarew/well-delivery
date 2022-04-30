/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm.dataaccess;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.annotation.PostConstruct;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.http.interceptors.Replay429Interceptor;
import com.google.gson.GsonBuilder;
@Component
public class CloudantdbInit {
	
	
	@Value("${ibm.db.url}") 
	private String dbUrl;
	@Value("${ibm.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNameEnv;
	@Value("${ibm.cloudantDb.database}")
	private String dbNamePrefix;
	private static final Logger logger = LoggerFactory.getLogger(CloudantdbInit.class);
	
	private GsonBuilder gsonBuilder = new GsonBuilder();
	
	private Database db;
	private CloudantClient cloudantClient;
	

    @PostConstruct
    public void init() throws MalformedURLException {
    	
    	getClient();
    }
    
    public Database getEntityDB(String entityType)  {
      String dbName = "opendes"+"-"+ dbNamePrefix +"-"+entityType;
      
		try {
			db =getDatabase(dbNameEnv, dbName);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
       return db;
    }

   
    public void getClient() throws MalformedURLException {

		String url = null;
		
		try {
			url =dbUrl;
		} catch (IllegalArgumentException e) {
			
		}
		
		ClientBuilder builder = ClientBuilder.url(new URL(url));
		
		logger.info("Connecting to cloudant at {}", url);
		
		// prefer IAM authentication
		if (apiKey != null) {
			logger.debug("Using IAM auth to cloudant");
			builder = builder.iamApiKey(apiKey);
		} else {
			logger.debug("Using username/password auth to cloudant");
			builder = builder.username(dbUser)
        	                 .password(dbPassword);
		}
		
		// Configure proxy via curl compatible http_proxy environment variable
		
		final String proxy_var = Optional.ofNullable(System.getenv("https_proxy"))
								.orElse(System.getenv("https_proxy"));
		
		if (proxy_var != null) {
			final URL proxyURL = new URL(proxy_var);
			if (proxyURL.getUserInfo() != null) {
				final String[] userInfo = proxyURL.getUserInfo().split(":");
				builder = builder.proxyUser(userInfo[0]);
				if (userInfo.length > 1) {
					builder = builder.proxyPassword(userInfo[1]);	
				}
			}
			
			final String protocol = proxyURL.getProtocol();
			
			int port = proxyURL.getPort();
			if (port == -1) {
				port = ("https".equals(protocol)) ? 443 : 80;
			}
			
			final String rewritten = new StringBuffer()
					.append(protocol).append("://")
					.append(proxyURL.getHost()).append(":")
					.append(port)
					.toString();
			
			logger.info("Using proxy {} to connect to cloudant", rewritten);

			builder = builder.proxyURL(new URL(rewritten)).interceptors(new Replay429Interceptor(20, 250l));
		}

		cloudantClient= builder.gsonBuilder(gsonBuilder).build();
	}  
    public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}
	
	public static String dbNameRule(final String dbNamePrefix, final String dbName) {
		
		final String normPrefix = (dbNamePrefix.charAt(dbNamePrefix.length() - 1) == '-')
				? dbNamePrefix.substring(0, dbNamePrefix.length()-1)
				: dbNamePrefix;
		
		return normPrefix.toLowerCase() + "-" + dbName.toLowerCase();
	}
	
	public Database getDatabase(final String dbNamePrefix, final String dbName) throws MalformedURLException {
		
		if (cloudantClient==null) {
			getClient();
		}
		return getDatabase(cloudantClient, dbNamePrefix, dbName);		
	}
	
	public Database getDatabase(final CloudantClient cloudant, final String dbNamePrefix, final String dbName) throws MalformedURLException {
					
		// Show the server version
		logger.debug("Server Version: " + cloudant.serverVersion());
		
		logger.debug("dbNamePrefix: " + dbNamePrefix);
		String dbFullName = dbNameRule(dbNamePrefix, dbName);
		logger.debug("dbFullName: " + dbFullName);
		
		logger.info("Database: " + dbFullName);

		return cloudant.database(dbFullName, true);
		
	}
}
