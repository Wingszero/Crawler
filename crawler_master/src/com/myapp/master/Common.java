package com.myapp.master;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public class Common
{
	public static final String LAST_QUERY = "last_query";

	public static final String GET = "GET";
	public static final String POST = "POST";

	//a time interval for an active worker submit status  
	public static final long ACTIVE_TIME_INTERVAL = 30000;

	//request fields
	public static final String PORT_STR = "port";
	public static final String STATUS_STR = "status";
	public static final String NUMTHREADS_STR = "numThreads"; 
	public static final String NUMWORKERS_STR = "numWorkers"; 
	public static final String NUMHOSTS_STR = "numHosts"; 
	public static final String RUNMINS_STR = "runMinutes";
	public static final String MY_WORKER_INDEX = "myWorkerIdx";

	//use for caching the active workers for a job
	public static final String ACTIVE_WORKERS_STR = "activeWorkers";
	

	/*worker status*/
	public static final String RUNNING_STATUS 		= "running";
	public static final String IDLE_STATUS 				= "idle";

	/*worker stat*/
	
	public static final String TOTAL_PAGE_NUM_STR = "total_page_num";

	public static final String TOTAL_HOST_NUM_STR = "total_host";

	public static final String SPEED_STR = "speed";
	
	public static final String TOTAL_PAGE_SIZE_STR = "total_page_size";

	public static final String MIN_PAGE_SIZE_STR = "min_page";

	public static final String MAX_PAGE_SIZE_STR = "max_page";

	public static final String AVG_PAGE_SIZE = "avg_page";
	
	public static final String DUE_NUM = "due_num";

	public static final String CONTENT_SEEN_NUM = "content_seen_num";


	
	/*URL*/
	//master->worker
	public static final String RUN_CRAWL_URL = "/runcrawl"; 
	
	//worker->master
	public static final String WORKER_STATUS_URL = "/workerstatus";

	//master->worker
	public static final Object TEST_POST_URL = "/test_post";
	
	//admin check master
	public static final String STATUS_URL = "/status";

	public static final String HTTP_PREFIX = "http://"; 
	
	public static final String WORKER_DNS_PRE = "http://jasoncrawlerworker"; 
	public static final String WORKER_DNS_POST = ".elasticbeanstalk.com";

	/**
	 * recursively till to get OK
	 * @param url_ins
	 * @param timeout
	 * @param method
	 * @return
	 */
	public static HttpURLConnection getHttpURLConnection(String url, int timeout, String method) 
	{
		URL url_ins;
		try 
		{
			url_ins = new URL(url);
		} 
		catch (MalformedURLException e) 
		{
			return null;
		}
		HttpURLConnection conn = null;
		try 
		{
			conn = (HttpURLConnection) url_ins.openConnection();
		} 
		catch (IOException | ClassCastException e) 
		{
			return null;
		}

		conn.setConnectTimeout(timeout);
		try 
		{
			conn.setRequestMethod(method);
		} 
		catch (ProtocolException e) 
		{
			return null;
		}
		if(method.equals("POST"))
		{
			conn.setDoOutput(true);
		}
		/*
		try
		{
			int sc = conn.getResponseCode();
		} 
		catch (IOException e) 
		{
			//print("getHttpURLConnection IOException: " + url);
			return null;
		}
		*/
		return conn;
	}

	private static final String[] IP_HEADERS = { 
			"X-Forwarded-For",
			"Proxy-Client-IP",
			"WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR",
			"HTTP_X_FORWARDED",
			"HTTP_X_CLUSTER_CLIENT_IP",
			"HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR",
			"HTTP_FORWARDED",
			"HTTP_VIA",
			"REMOTE_ADDR" 
	};


	public static String getClientIpAddress(HttpServletRequest request) 
	{
		for (String header : IP_HEADERS) 
		{
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) 
			{
				return ip;
			}
		}
		return request.getRemoteAddr();
	}
}
