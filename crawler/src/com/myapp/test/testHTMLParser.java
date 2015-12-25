package com.myapp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.google.common.net.InternetDomainName;
import com.myapp.worker.ContentExtractor;
import com.myapp.worker.HttpFetcher;
import com.myapp.worker.utils.Const;

import junit.framework.TestCase;

public class testHTMLParser extends TestCase 
{
	@SuppressWarnings("unused")
	@Test
	public void test1() throws UnknownHostException, IOException 
	{
		Document doc;
		String url = "https://en.wikipedia.org/wiki/Main_Page";
		url = "http://wikipedia.org";
		//url = "http://www.alexa.com/topsites/countries;0/US";

		//long st = System.currentTimeMillis();
		doc = HttpFetcher.fetchFileDOM(url, Const.ALL_HTML_TYPES, Const.HTML_MAX_LENGTH); 
		//long end = System.currentTimeMillis();
		//print("used time: " + (end - st)/1000F);

		Elements links = doc.select("a");
		print("href nums: " + links.size());
		HashSet<String>link_set = new HashSet<String>();
		HashSet<String>domain_set = new HashSet<String>();
		for(Element  link: links)
		{
			String ref = link.attr("abs:href"); 
			print("URL: " + ref); 
			String host;
			try 
			{
				host = new URI(ref).getHost();
				if(host == null || host.trim().isEmpty())
				{
					continue;
				}
				InternetDomainName d = InternetDomainName.from(host);
				if(d == null)
				{
					continue;
				}
				String domain = d.topPrivateDomain().toString();
				domain_set.add(domain);
				//print("HOST: " + domain);
			} 
			catch (URISyntaxException e) 
			{
				e.printStackTrace();
			}
		}
		for(String domain: domain_set) 
		{
			print("domain: " + domain);
		}
	}

	@Test
	public void testHTMLCleaner() throws UnknownHostException, IOException 
	{
		String url = "http://wikipedia.org";
		url = "http://www.nytimes.com/";
		url = "http://www.nba.com/";
		url = "http://www.nba.com/games/20151205/DENPHI/gameinfo.html?ls=iref:nbahpts";
		url = "https://en.wikipedia.org/wiki/Monty_Newborn";
		//url = "http://angelfire.com/stars/enqvist/";
		//url = "http://stackoverflow.com/questions/12943734/jsoup-strip-all-formatting-and-link-tags-keep-text-only";
		url = "http://www.nytimes.com/2015/12/10/opinion/guns-and-thunder-on-the-supreme-courts-right.html?action=click&pgtype=Homepage&clickSource=story-heading&module=opinion-c-col-left-region&region=opinion-c-col-left-region&WT.nav=opinion-c-col-left-region&_r=0";
		//url = "http://jsoup.org/cookbook/extracting-data/dom-navigation";
		//url = "http://www.nba.com/";
		url = "http://www.nba.com/2015/news/12/10/bridging-the-gap-paul-george-regaining-top-form/index.html?ls=nbahpfull2";
		url = "https://www.google.com/";
		url = "http://usnews.com";
		//url = "http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/mon-scripts.html";
		//url = "http://www.sneakerfreaker.com/sneakers/reebok-kamikaze-ii-overcast/";
		//url = "https://online.citi.com/US/JPS/portal/Index.do";
		//url = "http://german.alibaba.com/catalogs/pid100004944";

			
		CookieHandler.setDefault(new CookieManager());	
		
		String content = HttpFetcher.fetchHtml(url, 0).trim(); 
		print("before len: " + content.length());
		
		//before clean
		File f = new File("./before");
		if(!f.exists())
		{
			f.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(f));
		output.write(content);
		output.close();
	
		//after clean
		long st = System.currentTimeMillis();

		Document doc = Jsoup.parse(content, url);
		String title = doc.select("title").text();
		print("title: " + title);

		content = ContentExtractor.process(doc);
		print("after len: " + content.length()); 
		
		Element node = doc.select("html").first();
		if(node != null)
		{
			String lang = node.attr("lang");
			print("html lang: " + lang); 
		}
		
		
		Elements links = doc.select("a");
		for(Element  link: links)
		{
			String subLink = link.attr("href").trim();		
			String abssubLink = link.attr("abs:href").trim();		
			print("sublink: " + subLink);
			print("abs sublink: " + abssubLink); 
			print("=============");
		}

		
		long end = System.currentTimeMillis();
		print("clean used time:" + (end - st));

		File f2 = new File("./after");
		if(!f2.exists())
		{
			f2.createNewFile();
		}
		BufferedWriter output2 = new BufferedWriter(new FileWriter(f2));
		output2.write(content);
		output2.close();
	}

	private static void print(String s)
	{
		System.out.println(s);
	}
}
