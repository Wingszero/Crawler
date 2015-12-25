package com.myapp.test;


import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import com.myapp.worker.seedsFetcher;

import junit.framework.TestCase;

public class testAlexa extends TestCase 
{
	@Test
	public void test1() throws URISyntaxException, IOException 
	{
		seedsFetcher.processUSTop();
	}	
}
