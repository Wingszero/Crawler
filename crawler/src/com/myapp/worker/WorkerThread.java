package com.myapp.worker;

import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.myapp.worker.db.WebPageEntity;

public class WorkerThread implements Runnable 
{
	private Worker crawl_ins; 

	private boolean running = true;

	public WorkerThread(Worker c)
	{
		crawl_ins = c;
	}

	@Override
	public void run() 
	{
		while(isRunning())
		{
			//long st = System.currentTimeMillis();
			String url_str; 
			while((url_str = getNextUrl()) == null)
			{
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{

				}
			}

			//check disallow links, may cause fetch robots.txt
			boolean ok = crawl_ins.isAllowed(url_str); 
			if(ok == false) continue;

			//crawled before
			WebPageEntity page = crawl_ins.getWebPage(url_str);
			if(page != null)
			{
				continue;
			}

			// fetch 
			String rawPage = HttpFetcher.fetchHtml(url_str, 0);

			// stat
			crawl_ins.statPage(url_str, rawPage);

			// finish fetching, update url-frontier
			crawl_ins.updateURLFrontierAfterFetch(url_str);
			
			if(rawPage.isEmpty())
			{
				continue;
			}

			// parse html to DOM
			Document doc = Jsoup.parse(rawPage, url_str);
			
			//check language
			Elements nodes = doc.select("html");
			if(nodes != null)
			{
				Element node = nodes.first();
				String lang = node.attr("lang").trim().toLowerCase();
				// drop which tag is not contains en
				if(!lang.isEmpty() && !lang.contains("en")) 
				{
					continue;
				}
			}

			// extract canonical link
			String canonical_url = getCanonicalLink(doc).trim();
			canonical_url = processCanonicalUrl(url_str, canonical_url);
			// fetched before, drop it
			if(canonical_url == null)
			{
				continue;
			}	

			// extract page content
			String content = ContentExtractor.process(doc); 

			// content seen
			if(!crawl_ins.processContentSeen(content))
			{
				continue;
			}

			// extract title
			String title = extractTitle(doc);
			// calculate is hot page
			boolean isHot = AnchorPool.isHot(url_str, title, content);			
			if(isHot)
			{
				crawl_ins.addHotPage();
			}

			// filterList: for later crawl
			HashSet<String>filterSet = new HashSet<String>();

			// allList: for PageRank
			ArrayList<String>allList = new ArrayList<String>();

			//extract external links, Malformed URL will be filtered 
			LinkExtractor.extractLinks(doc, url_str, filterSet, allList);

			//store: should store canonical_url here, so we can find a page is duplicated earlier.
			crawl_ins.storeWebPage(canonical_url, extractTitle(doc), content, allList);

			// Filter----> HostSplitter ----> DUE ----------> URL Frontier
			crawl_ins.processLinks(filterSet, isHot);
		}
		print("Thread end:"  + Thread.currentThread().getName()); 
	}

	/**
	 * If canonical_url equals to this url, just return it
	 * If fetched canonical_url before, return null
	 * If canonical_url is not equals to this url, return canonical_url 
	 * @param url
	 * @param canonical_url
	 * @return
	 */
	private String processCanonicalUrl(String url_str, String canonical_url) 
	{
		if(!canonical_url.isEmpty())
		{
			if(!canonical_url.endsWith("/"))
			{
				canonical_url += "/";
			}
			if(!canonical_url.equals(url_str) && !canonical_url.equals("/"))
			{
				// relative path
				if(canonical_url.startsWith("/") || !canonical_url.startsWith("http"))
				{
					// remove begin slash
					if(canonical_url.startsWith("/"))
					{
						canonical_url = canonical_url.substring(1);
					}
					// make absolute path
					canonical_url = url_str + canonical_url;
				}
				else if(canonical_url.equals("http:///") || canonical_url.equals("https:///")) //bad url
				{
					return url_str;
				}

				// fetched before
				if(crawl_ins.getWebPage(canonical_url) != null)
				{
					return null;
				}
				if(!url_str.equals(canonical_url))
				{
					crawl_ins.processDUE(canonical_url);
					return canonical_url;
				}
			}
		}
		return url_str;
	}

	/**
	 * extract href of the canonical tag
	 * @param doc
	 * @return
	 */
	private String getCanonicalLink(Document doc)
	{
		Elements links = doc.select("link");
		for(Element link: links)
		{
			if(link.attr("rel").equals("canonical"))
			{
				return link.attr("href");	
			}
		}
		return "";
	}

	private String extractTitle(Document doc) 
	{
		Elements e = doc.select("title");
		return e != null ? e.text() : "";
	}

	private String getNextUrl() 
	{
		return crawl_ins.getNextUrl();
	}

	private boolean isRunning() 
	{
		return running; 
	}

	public void Stop()
	{
		running = false;
	}

	public static void print(String data)
	{
		System.out.println(data);
	}
}
