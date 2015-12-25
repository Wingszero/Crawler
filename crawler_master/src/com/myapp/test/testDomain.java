package com.myapp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.Test;

import com.google.common.net.InternetDomainName;

import junit.framework.TestCase;

public class testDomain extends TestCase 
{
	@Test
	public void test1() throws URISyntaxException 
	{
		/*
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
		*/
	}

	public static String getDomainName(String url) throws URISyntaxException 
	{
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
	
	
	@Test
	public void testStoreWeb() 
	{
		/*
		File f = new File("alexa.txt");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
			return;
		}
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		DBWrapper db = new DBWrapper();
		try 
		{
			while ((line = br.readLine()) != null) 
			{
				String h = line.trim().toLowerCase();
				db.storeWebPage(h);
			}
		}
		catch(IOException e)
		{
			
		}
		db.close();
		*/
	}
}
