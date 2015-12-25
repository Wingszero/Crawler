package com.myapp.test;

import org.junit.Test;
import com.myapp.worker.utils.NormalizeURL;

import junit.framework.TestCase;

public class testNormalizeURL extends TestCase 
{
	@Test
	public void test() 
	{
		String[] urls = {
				"htTp://google.com                       ",
				"HTTPS://www.GOogle.coM",
				"http://www.google.com?",
				"http://www.google.com/",
				"http://www.google.com#",
		};
		
		for(String url: urls)
		{
			System.out.println(NormalizeURL.normalize(url));
		}
	}
}
