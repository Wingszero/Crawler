package com.myapp.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


import junit.framework.TestCase;

public class testCrawler extends TestCase 
{
	int numThreads = 30; 
	ArrayList<String>hosts;

	@SuppressWarnings({ "unused", "resource" })
	private ArrayList<String> readHost() throws FileNotFoundException, IOException
	{
		ArrayList<String>hosts = new ArrayList<String>();
		String line;
		InputStream fis = new FileInputStream("./alexa");
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		while ((line = br.readLine()) != null) 
		{
			hosts.add(line);
		}
		return hosts;
	}

	public void test1() throws FileNotFoundException, IOException, InterruptedException
	{
		//Worker c = new Worker(numThreads, readHost(), 1, 1,  5);
		//c.run();

		/*
		hosts = readHost();
		String prefix = "http://www.";
		for(int i = 0; i < 10; ++i)
		{
			String h = hosts.get(i);
			if(!h.startsWith(prefix))
			{
				h = prefix + h;
			}
			RobotsTxtInfo robots = HttpFetcher.fetchRobotsTxt(h);
			if(robots == null)
			{
				print("fetch Robot fail: " + h);
			}
		}
		*/
	}
	
	public static void print(String data)
	{
		System.out.println(data);
	}
}
