package com.myapp.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Test;
import junit.framework.TestCase;

public class testOther extends TestCase 
{
	public static void print(String x)
	{
		System.out.println(x);
	}

	@Test
	public void testHost() throws URISyntaxException, IOException 
	{
		/*
		InetAddress addr = InetAddress.getByName("52.5.89.38");
		String host = addr.getHostName();
		System.out.println(host);
		String url = "info=http%3A%2F%2Fimdb.com%2Ftitle%2Ftt518=1568%2F%3Fref_%3Dnm_flmg_dr_1&";
		String query_string = URLDecoder.decode(url, "UTF-8"); 
		print("query_string: " + query_string);
		String value = Common.parsePushUrl(query_string);
		print("value : " + value);
		URL url_ins = Common.getURL(value);
		if(url_ins == null) return;
		print("jason be pushed new url: " + value);
		String host = HttpFetcher.getDomainName(url_ins);
		print("host: " + host);
		 */
	}



	@Test
	public void testStringBuilder() 
	{
		/*
		int cnt = 10000;
		String line = "jason";

		long st = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i< cnt; ++i)
		{
			sb.append(line);
		}
		String res = sb.toString();
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - st));


		st = System.currentTimeMillis();
		res = "";
		for(int i = 0; i< cnt; ++i)
		{
			res  = res.concat(line);
		}
		end = System.currentTimeMillis();
		System.out.println("Time: " + (end - st));
		 */
	}

	@Test
	public void testURL() 
	{
		/*
		String url = "https://en.wikipedia.org/wiki/Canonical_link_element";
		int cnt = 1000;
		long st = System.currentTimeMillis();
		for(int i = 0; i < cnt; ++i)
		{
			URL url_ins;
			try 
			{
				url_ins = new URL(url);
			} 
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println(System.currentTimeMillis() - st);
		*/
	}
		
	@Test
	public void testRAM() 
	{
		String url = "https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=alexa%201%20million";
		System.out.println(url.length());
		Integer cnt = 2000000;
		BlockingQueue<String>frontQueue = new LinkedBlockingQueue<String>(cnt);

		for(int i = 0; i < cnt; ++i)
		{
			try 
			{
				frontQueue.put(url);
			} 
			catch (InterruptedException e) 
			{
				//e.printStackTrace();
			}
		}
		System.out.println("finish");
	}
}
