package com.myapp.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.junit.Test;

import junit.framework.TestCase;

public class testDomain extends TestCase 
{
	@Test
	public void test1() throws URISyntaxException 
	{
		//String url = "http://forums.bbc.co.uk/";
		ArrayList<String>urls = new ArrayList<String>();
		urls.add("www.reddit.com");
		urls.add("reddit.com");
		for(String url: urls)
		{
			if(!url.startsWith("http"))
			{
				url = "http://" + url;
			}
			System.out.println(getDomainName(url));
		}
	}

	public static String getDomainName(String url) throws URISyntaxException 
	{
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
}
