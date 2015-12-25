package com.myapp.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.Robots.RobotsParser;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * 
 * @author Haoyun 
 *
 */
public class HttpFetcher 
{

	public HttpFetcher()
	{
		// set the default cookie manager.
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}


	public static void writeFile(String name, String content) throws IOException
	{
		File file = new File(name);
		if (!file.exists()) 
		{
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
	}

	/**
	 * recursively till to get OK
	 * depth is to control the redirect times
	 * @param url_ins
	 * @param timeout
	 * @param method
	 * @return
	 */
	public static HttpURLConnection getHttpURLConnection(String url, int timeout, String method, int depth) 
	{
		if(depth > Const.REDIRECT_MAX_DEPTH)
		{
			return null;
		}

		URL url_ins;
		try 
		{
			url_ins = new URL(url);
		} 
		catch (MalformedURLException e) 
		{
			print("getHttpURLConnection: new URL MalformedURLException: " + url);
			return null;
		}

		HttpURLConnection conn = null;
		try 
		{
			conn = (HttpURLConnection) url_ins.openConnection();
			if (conn instanceof HttpsURLConnection) 
			{
				conn = (HttpsURLConnection) conn;
			}
		} 
		catch (IOException | ClassCastException e) 
		{
			print("openConnection exception: " + url);
			return null;
		}
			
		conn.setConnectTimeout(timeout);
		conn.setRequestProperty("Accept", "text/html, text/*, application/xhtml+xml,application/xml"); 
		conn.setRequestProperty("Accept-Encoding",  "gzip, deflate"); 
		conn.setRequestProperty("Accept-Language", "en-US,en");
		conn.setRequestProperty(Const.STR_USER_AGENT,  Const.MY_USER_AGENT);
				
		try 
		{
			conn.setRequestMethod(method);
			if(method.equals("POST"))
			{
				conn.setDoOutput(true);
				return conn;
			}
		} 
		catch (ProtocolException e) 
		{
			e.printStackTrace();
			return null;
		}

		try 
		{
			int sc = conn.getResponseCode();
			Worker.addResponseCodeStatus(sc);
			if(sc != HttpURLConnection.HTTP_OK) 
			{
				if(!needRedirect(sc))
				{
					return null;
				}
				String new_url = conn.getHeaderField("Location");	
				//avoid bad redirect
				if(new_url != null && !new_url.equalsIgnoreCase(url))
				{
					return getHttpURLConnection(new_url, timeout, method, depth + 1); 
				}
				return null;
			}
		} 
		catch (Exception e) 
		{
			Worker.addResponseFailNum();
			return null;
		}
		return conn;
	}

	/**
	 * determine if a Content-Type is in accept types
	 * @param ct: check
	 * @param tar: target list
	 * @return
	 */
	public static boolean isValidContentType(String contentType, String[] valids)
	{
		if(contentType == null)
		{
			return false;
		}
		String[] vals = contentType.split(";");
		if(vals.length == 0)
		{
			return false;
		}
		String v = vals[0].trim().toLowerCase();
		for(String t: valids)
		{
			if(v.equalsIgnoreCase(t))
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * redirect status code
	 * @param status code 
	 * @return
	 */
	private static boolean needRedirect(int status) 
	{
		return status == HttpURLConnection.HTTP_MOVED_TEMP
			|| status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_SEE_OTHER;
	}

	public static void print(String s)
	{
		System.out.println(s);
	}


	private static int readInitialLine(BufferedReader reader) throws IOException 
	{
		String line;
		while((line = reader.readLine()) != null)
		{
			if(!line.isEmpty())
			{
				break;
			}
		}
		if(line == null)
		{
			print("line is null............");
			return -1;
		}

		//eg: HTTP/1.1 200 OK
		String res[] = line.split(" ");
		if (res.length < 3) 
		{
			print("Invalid header: initial line length != 3");
			return -1;
		}
		try
		{
			int status_code = Integer.parseInt(res[1]);
			print("status code: " + status_code);
			return status_code;
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public static Map<String, List<String>> parseHeaders(BufferedReader reader) throws IOException 
	{
		if(reader == null)
		{
			return null;
		}
		int sc = readInitialLine(reader);
		if(sc == -1)
		{
			return null;
		}

		if(sc != HttpURLConnection.HTTP_OK && !needRedirect(sc))
		{
			return null;
		}

		String line; 
		String last_header = "";
		Map<String, List<String>>headers = new HashMap<String, List<String>>();

		//print("Show headers:");
		while((line = reader.readLine()) != null)
		{
			line = line.trim();
			if(line.isEmpty()) 
			{
				continue;
			}
			//print(line);
			String parts[] = line.split(":", 2);
			if (parts.length > 1) 
			{ 
				//eg: header1: v1, v2 
				List<String> values = headers.get(parts[0]);
				if (values == null) 
				{
					values = new ArrayList<String>();
				}
				values.add(parts[1].trim());
				headers.put(parts[0], values);
				last_header = parts[0];
			} 
			else 
			{ 
				//eg: value1, value2 
				List<String> values = headers.get(last_header);
				if(values == null)
				{
					print("ERROR Header: " + line);
					return null;
				}
				values.add(parts[0].trim());
				headers.put(last_header, values);
			}
		}
		return headers;
	}

	public static Document fetchFileDOM(String url, String[] types, int max_length) 
	{
		String html = fetchTextFile(url,  max_length, 0, true);
		return Jsoup.parse(html);
	}

	/**
	 * 
	 * @param url
	 * @param ContentType
	 * @param MaxLength
	 * @return
	 */
	public static String fetchHtml(String url, long last_time)
	{
		return fetchTextFile(url, Const.HTML_MAX_LENGTH, last_time, true);
	}


	/**
	 * 
	 * @param url
	 * @param ContentType
	 * @param MaxLength
	 * @return
	 */
	public static Object fetchRobots(String url, long lastTime)
	{
		String content = fetchTextFile(url, Const.ROBOTS_MAX_LENGTH, lastTime, false);
		if(content.isEmpty())
		{
			return new Boolean(true);
		}
		return RobotsParser.parse(content); 
	}


	/**
	 * Get InputStream considering http compression
	 * @param conn
	 * @return
	 */
	public static InputStream getInputStream(URLConnection conn) 
	{
		try 
		{
			InputStream in = conn.getInputStream();
			String contentEncoding = conn.getContentEncoding(); 
			if (contentEncoding != null)
			{
				if(contentEncoding.indexOf("gzip") > -1)
				{
					try
					{
						in = new GZIPInputStream(in);
					}
					catch(IOException e)
					{
						print("getInputStream new GZIPInputStream Exception");
						return null;
					}
				}
				else if(contentEncoding.indexOf("deflate") > -1)
				{
					in = new InflaterInputStream(in, new Inflater(true));
				}
			}
			return in;
		} 
		catch (IOException e) 
		{
			print("getInputStream Exception");
		}
		return null;
	}

	/**
	 * Check ContentType and Last-Modified
	 * @param conn
	 * @param acceptTypes
	 * @param lastTime
	 * @param checkContentType
	 * @return
	 */
	public static boolean checkHeader(URLConnection conn, long lastTime, boolean checkContentType)
	{
		//content-type
		/*
		String type = conn.getContentType();
		if(checkContentType && type == null) 
		{
			return false;
		}

		//last-modified
		long lastModify = conn.getLastModified();
		if(lastModify != 0 && lastTime >= lastModify)
		{
			return false;
		}
		*/
		return true;
	}

	/**
	 * Fetch text file, include robots.txt and html files
	 * @param url
	 * @param tar_type
	 * @param maxLength
	 * @return
	 */
	public static String fetchTextFile(String url, int maxLength, long lastTime, boolean checkContentType) 
	{
		//long st = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		URLConnection conn = getHttpURLConnection(url, Const.CONNECTION_TIMEOUT, Const.GET, 0);

		if(conn == null)
		{
			return "";
		}

		if(!checkHeader(conn,  lastTime, checkContentType))
		{
			print("fetchTextFile checkHeader drop: Content-type: " + conn.getContentType() 
			+ " Last-modified: " + conn.getLastModified()
			+ " url: " + url);
			return "";
		}
			
		try 
		{
			InputStream in = getInputStream(conn);
			if(in == null)
			{
				return "";
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in)); 
			String line = "";
			int len = 0;
			while((line=reader.readLine())!=null && len < maxLength)
			{
				if(line.isEmpty())continue;

				sb.append(line);

				len += line.length();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return sb.toString().trim(); 
	}
}
