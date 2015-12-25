package com.myapp.test;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;

import junit.framework.TestCase;

public class testCuncurrentHashMap extends TestCase 
{
	@Test
	public void test1() throws URISyntaxException, IOException 
	{
		ConcurrentHashMap<Integer, ArrayList<Integer>>map = new ConcurrentHashMap<Integer, ArrayList<Integer>>(); 
		int key = 1;
		if(!map.containsKey(key))
		{
			ArrayList<Integer>list = new ArrayList<Integer>();
			list.add(2);
			map.put(key, list);
		}
		ArrayList<Integer>list = map.get(key); 
		list.add(3);
		System.out.println(map);
	}	
}
