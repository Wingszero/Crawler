package com.myapp.master;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * - Covert the scheme and host to lowercase 
 * - Normalize the path (done by java.net.URI)
 * - Add port number.
 * - Remove fragment (the part after the #).
 * - Remove trailing slash.
 * - Sort the query string params.
 * - Remove some query string params like "utm_*" and "*session*".
 */
public class NormalizeURL
{
	public static String getAbsoluteUrl(String u) 
	{
		String lu = u.toLowerCase();
		if(!lu.startsWith("http"))
		{
			return "http://" + u;
		}
		return u;
	}	

	/**
	 * @param url
	 * @return
	 */
	public static String getDomainName(String url) 
	{
		url = getAbsoluteUrl(url);
		URL url_ins;
		try 
		{
			url_ins = new URL(url);
		} 
		catch (MalformedURLException e)
		{
			System.out.println("URLNomalize getDomainName MalformedURLException: " + url);
			return null;
		}
		String domain = url_ins.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}	
	
	/**
	 * @param url
	 * @return
	 */
	public static String getDomainName(URL url) 
	{
		if(url == null)
		{
			return null;
		}
		String domain = url.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}	

	public static String normalize(String orig_url) throws MalformedURLException
	{
		if(orig_url.trim().isEmpty()) return "";
		
		orig_url = getAbsoluteUrl(orig_url).trim();
		
		if(orig_url.endsWith("/"))
		{
			orig_url = orig_url.substring(0, orig_url.length() - 1);
		}
		
		URL url;
		try 
		{
			url = new URL(orig_url); 
		} 
		catch (MalformedURLException e) 
		{
			System.out.println("URLNomalize URISyntaxException: " + orig_url);
			return "";
		}
		
		String path = url.getPath().replace("/$", "");

		SortedMap<String, String> params = createParameterMap(url.getQuery());

		int port = url.getPort();

		String queryString;

		if (params != null)
		{
			// Some params are only relevant for user tracking, so remove the most commons ones.
			for (Iterator<String> i = params.keySet().iterator(); i.hasNext();)
			{
				String key = i.next();
				if (key.startsWith("utm_") || key.contains("session"))
				{
					i.remove();
				}
			}
			queryString = "?" + canonicalize(params);
		}
		else
		{
			queryString = "";
		}

		return url.getProtocol().toLowerCase() + "://" + url.getHost().toLowerCase()
		+ (port != -1 && port != 80 ? ":" + port : "")
		+ path + queryString;
	}

	/**
	 * Takes a query string, separates the constituent name-value pairs, and
	 * stores them in a SortedMap ordered by lexicographical order.
	 * @return Null if there is no query string.
	 */
	private static SortedMap<String, String> createParameterMap(String queryString)
	{
		if (queryString == null || queryString.isEmpty())
		{
			return null;
		}

		String[] pairs = queryString.split("&");
		Map<String, String> params = new HashMap<String, String>(pairs.length);

		for (String pair : pairs)
		{
			if (pair.length() < 1)
			{
				continue;
			}

			String[] tokens = pair.split("=", 2);
			for (int j = 0; j < tokens.length; j++)
			{
				try
				{
					tokens[j] = tokens[j].replaceAll("%(?![0-9a-fA-F]{2})", "%25");
					tokens[j] = tokens[j].replaceAll("\\+", "%2B");
					tokens[j] = URLDecoder.decode(tokens[j], "UTF-8");
				}
				catch (UnsupportedEncodingException ex)
				{
					ex.printStackTrace();
				}
			}
			switch (tokens.length)
			{
			case 1:
			{
				if (pair.charAt(0) == '=')
				{
					params.put("", tokens[0]);
				}
				else
				{
					params.put(tokens[0], "");
				}
				break;
			}
			case 2:
			{
				params.put(tokens[0], tokens[1]);
				break;
			}
			}
		}

		return new TreeMap<String, String>(params);
	}

	/**
	 * Canonicalize the query string.
	 *
	 * @param sortedParamMap Parameter name-value pairs in lexicographical order.
	 * @return Canonical form of query string.
	 */
	private static String canonicalize(SortedMap<String, String> sortedParamMap)
	{
		if (sortedParamMap == null || sortedParamMap.isEmpty())
		{
			return "";
		}

		StringBuffer sb = new StringBuffer(350);
		Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

		while (iter.hasNext())
		{
			Map.Entry<String, String> pair = iter.next();
			sb.append(percentEncodeRfc3986(pair.getKey()));
			sb.append('=');
			sb.append(percentEncodeRfc3986(pair.getValue()));
			if (iter.hasNext())
			{
				sb.append('&');
			}
		}
		return sb.toString();
	}

	/**
	 * Percent-encode values according the RFC 3986. The built-in Java URLEncoder does not encode
	 * according to the RFC, so we make the extra replacements.
	 *
	 * @param string Decoded string.
	 * @return Encoded string per RFC 3986.
	 */
	private static String percentEncodeRfc3986(String string)
	{
		try
		{
			return URLEncoder.encode(string, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		}
		catch (UnsupportedEncodingException e)
		{
			return string;
		}
	}
}
