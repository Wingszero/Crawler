package com.myapp.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import org.junit.Test;
import com.myapp.worker.HttpFetcher;
import junit.framework.TestCase;


/*
 * test GET/HEAD and so on Network functions 
 * basically edu.upenn.cis455.crawler.HttpClient;
 */
public class TestHttpFetcher extends TestCase
{

	@Test
	public void testHEAD() throws UnknownHostException, IOException 
	{
		/*
		String url_str = "https://en.wikipedia.org/wiki/Main_Page";
		long st = System.currentTimeMillis();
		String content = HttpFetcher.fetchFileByBytes(url_str, Const.HTML_CONTENT_TYPE, Const.HTML_MAX_LENGTH);
		print("used time: " + (System.currentTimeMillis() - st));
		print("content len: " + content.getBytes().length); 
		print("content len2: " + content.length());

		st = System.currentTimeMillis();
		content = HttpFetcher.fetchTextFile(url_str, Const.HTML_CONTENT_TYPE, Const.HTML_MAX_LENGTH);
		print("used time: " + (System.currentTimeMillis() - st));
		print("content len: " + content.getBytes("UTF-8").length); 
		print("content len2: " + content.length());
		 */
	}

	@Test
	public void testRobots() throws UnknownHostException, IOException 
	{
		/*
		//String url = "http://ecommerce.answers.com/robots.txt";
		String url = "http://wikia.com";
		HttpFetcher.fetchRobotsTxt(url); 
		url = "http://emgn.com";
		HttpFetcher.fetchRobotsTxt(url); 
		 */
	}

	@SuppressWarnings("unused")
	@Test
	public void testSocket() throws UnknownHostException, IOException 
	{
		//String path = "website_list/alexa";
		//ArrayList<String>urls = CrawlerProducer.readHost(path);
		String[] urls = {"http://pbs.org"};
		long st = System.currentTimeMillis();
		for(String url: urls)
		{
			/*
			String content = "";
			content = HttpFetcher.fetchHtmlBySocket(url);
			//print("len: " + content.length()); 
			//print("Content: " + content);
			print("Time1: " + (System.currentTimeMillis() - st)/1000F);
			 */

			//content = HttpFetcher.fetchHtml(url);
			//print("len: " + content.length()); 
			long st1 = System.currentTimeMillis();
			HttpURLConnection conn = HttpFetcher.getHttpURLConnection(url, 3000, "GET", 0);
			print("Time: url " + url  + " " + (System.currentTimeMillis() - st1));
		}
		print("total: " + (System.currentTimeMillis() - st));
	}

	@Test
	public void testGET() throws UnknownHostException, IOException 
	{
		/*
		String url0 = "http://wikia.com";
		String url1 = "http://www.clas.ufl.edu/au/";
		String url2 = "http://battle.net";
		String url4 = "http://www.sina.com.cn/";
		String path = "website_list/alexa";
		int maxL = -1;
		int minL = Const.HTML_MAX_LENGTH;
		int total = 0;
		float avg;

		//ArrayList<String>urls = CrawlerProducer.readHost(path);
		ArrayList<String>urls = new ArrayList<String>(); 

		urls.add("http://fortune.com");
		urls.add("http://www.cnn.com");
		urls.add("http://www.weather.com/");
		urls.add("http://www.foxnews.com/");
		urls.add("http://abcnews.go.com/");
		urls.add("http://www.bbc.com/");
		urls.add("http://www.nba.com/");
		urls.add("http://www.nbcnews.com/");
		for(String url: urls)
		{

			String content = HttpFetcher.fetchHtml(url);
			//print("Content: " + content);
			Set<String>links = LinkExtractor.extractLinks(content, url);

		for(String l: links)
		{
			print(l);
		}

			print("links:" + links.size()); 
			if(content.length() == 0)
			{
				print("null content:=======" + url);
				continue;
			}
			maxL = Math.max(maxL, content.length());
			minL = Math.min(minL, content.length());
			total += content.length();
		}
		avg = total / urls.size();
		print("pages:" +  urls.size()); 
		print("total:" +  total); 
		print("avg:" +  avg); 
		print("max:" +  maxL); 
		print("min:" +  minL); 
		 */
	}

	@Test
	public void testUSSchool() throws UnknownHostException, IOException 
	{

	}
	@Test
	public void testUrl() throws UnknownHostException, IOException 
	{
		/*
		String url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		URL addr = new URL(url);
		print(addr.getPath());
		print(addr.getProtocol());
		print(addr.getHost());
		 */
	}

	private static void print(String s)
	{
		System.out.println(s);
	}
}


