package com.myapp.worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.Robots.RobotsTxtInfo;

/**
 * Manual Filter 
 * @author Haoyun 
 *
 */
public class URLFilter 
{
	public static HashSet<String> DownLoadType; 

	//forbidden urls
	//TODO: avoid robot trap?
	public static Set<String> forbid; 
	
	public URLFilter()
	{
		DownLoadType = new HashSet<String>();
		DownLoadType.add("text/html");
		forbid = new HashSet<String>();
	}

	/**
	 *  filter urls you want
	 * @param url
	 * @param host 
	 * @return
	 */
	public boolean process(String url, String host) 
	{
		if(host == null || host.isEmpty())
		{
			return false;
		}
		/*
		if(!filterMailTo(url))
		{
			return false;
		}
		*/
		//check wikipedia host
		if(!filterNotEnWiki(host))
		{
			//System.out.println("URLFilter drop: " + url);
			return false;
		}
		//check wikipedia host
		if(!filterCgiBin(url))
		{
			//System.out.println("URLFilter drop: " + url);
			return false;
		}
		return !forbid.contains(url);
	}

	private boolean filterCgiBin(String url) 
	{
		return !url.contains("/cgi-bin");
	}

	/**
	 * eg: mailto:%20?subject=Welcome Blue Canary &body=
	 * @param url
	 * @return
	 */
	public static boolean filterMailTo(String url) 
	{
		if(url.contains("mailto"))
		{
			return false;
		}
		return true;
	}

	/**
	 *  filter all sub links except en.wikipedia.org
	 * @return
	 */
	public boolean filterNotEnWiki(String url)
	{
		if(url.endsWith("wikipedia.org")) 
		{
			int index = url.length() - 16; 
			if(index < 0)
			{
				//print("wiki host: " + url);
				return false;
			}
			String zone = url.substring(index, index + 2);
			if(!zone.equals("en"))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param url
	 * @return if allow to access by robots.txt
	 */
	public boolean isAllow(String url, Object robots) 
	{
		if(robots == null)
		{
			return false;
		}
		if(robots instanceof Boolean)
		{
			return true;
		}

		URL u;
		try 
		{
			u = new URL(url);
			String path = u.getPath();
			for(String agent: Const.USER_AGENTS)
			{
				if(((RobotsTxtInfo) robots).containsUserAgent(agent))
				{
					ArrayList<String> alinks = ((RobotsTxtInfo) robots).getAllowedLinks(agent);
					if(matchLinks(path, alinks))
					{
						return true;
					}
					ArrayList<String> dlinks = ((RobotsTxtInfo) robots).getDisallowedLinks(agent);
					return !matchLinks(path, dlinks); 
				}
			}
			return true;
		} 
		catch (MalformedURLException e) 
		{
			System.out.println("URLFilter isAllow MalformedURLException " + url );
		}
		return false;
	}

	/** 
	 * Check if the path match one of the links 
	 * @return boolean
	 */
	private static boolean matchLinks(String path, ArrayList<String>links)
	{
		if(links == null)
		{
			return false;
		}
		for(String link:links)
		{
			if(path.startsWith(link))
			{
				return true;
			}
		}
		return false;
	}

	public static void print(String s)
	{
		System.out.println(s);
	}
}
