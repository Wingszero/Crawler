package com.myapp.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;


import junit.framework.TestCase;

public class testThreads extends TestCase 
{
	private static statRunner stat_run; 
	private ConcurrentHashMap<Integer, ArrayList<Integer>>queue = new ConcurrentHashMap<Integer, ArrayList<Integer>>();

	@Test
	public void test1() throws URISyntaxException, IOException 
	{
		int cnt = 10;
		for(int i = 0; i < cnt; ++i)
		{
			ArrayList<Integer>q = queue.get(i);
			if(q == null)
			{
				q = new ArrayList<Integer>();
			}
			q.add(1);
		}
		int numThreads = 2;
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);
		pool.execute(new Thread(stat_run));
	}

	private class statRunner implements Runnable 
	{
		private volatile boolean running = true;
		@SuppressWarnings("unused")
		public void terminate() 
		{
			running = false;
		}

		@Override
		public void run()
		{
			while(running)
			{
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
				}
			}
		}
	}
}
