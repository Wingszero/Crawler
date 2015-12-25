package com.myapp.worker.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


public class Common
{
	public static boolean isEmptyStr(String data) 
	{
		return data == null || data.trim().isEmpty();
	}

	public static URL getURL(String url)
	{
		try 
		{
			return new URL(url);
		} 
		catch (MalformedURLException e1) 
		{
			return null;
		}
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

	/**
	 * eg:field1=value1&field2=value2&field3=value3...
	 * @param query
	 * @return key-value of query string
	 */
	public static Hashtable<String, String> parseQueryString(String query)
	{
		Hashtable<String, String>kv = new Hashtable<String, String>();
		//field=value
		String[] sp = query.split("&");
		for(String token: sp)
		{
			//System.out.println("jason token: " + token);
			String[] pair = token.split("=");
			if(pair.length < 2)
			{
				continue;
			}
			String key = pair[0].trim();
			String value = pair[1].trim().toLowerCase(); 
			kv.put(key, value);
		}
		return kv;
	}


	/**
	 * for local read files
	 * @param path
	 * @return
	 */
	public static ArrayList<String> localReadHost(String path) 
	{
		ArrayList<String>hosts = new ArrayList<String>();
		String line;
		InputStream fis;
		try {
			fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			try 
			{
				while ((line = br.readLine()) != null) 
				{
					String h = NormalizeURL.getAbsoluteUrl(line);
					if(!h.isEmpty())
					{
						hosts.add(h);
					}
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return hosts;
	}

	/**
	 * for servlet read files
	 * @param path
	 * @param application
	 * @return
	 */
	public static ArrayList<String> servletReadHosts(String path, ServletContext application)
	{
		ArrayList<String>hosts = new ArrayList<String>();
		//System.out.println("application path:"); 

		File file = new File(application.getRealPath(path));
		FileInputStream fis;
		try 
		{
			fis = new FileInputStream(file);
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
			return null;
		}
		String line;
		//System.out.println("fis: " + fis);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		try 
		{
			while ((line = br.readLine()) != null) 
			{
				String h = line.trim().toLowerCase();
				hosts.add(h);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return hosts;
	}

	/**
	 * eg: info=http%3A%2F%2Fucirvinesports.com%2Fcomposite
	 * @param query
	 * @return
	 */
	public static String parsePushUrl(String query) 
	{
		int index = query.indexOf("info=");
		if(index != -1)
		{
			return query.substring(index + 5);
		}
		return null;
	}
}