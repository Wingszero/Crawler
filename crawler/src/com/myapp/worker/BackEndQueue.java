package com.myapp.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The back-end Queue for URLFrontier which are the urls for crawling now..
 * 
 * @author Haoyun 
 *
 */
public class BackEndQueue 
{
	/**
	 * key: a host
	 * value: the urls belongs to this host
	 */
	private ConcurrentHashMap<Integer, BlockingQueue<String>>indexQueue = new ConcurrentHashMap<Integer, BlockingQueue<String>>();
	
	public BackEndQueue(int activeHostNum)
	{
		for(int i = 0; i < activeHostNum; ++i)
		{
			indexQueue.put(i, new LinkedBlockingQueue<String>());
		}
	}

	public void put(Integer index, String url) 
	{
		BlockingQueue<String>q = indexQueue.get(index); 
		q.add(url);
	}
	
	public BlockingQueue<String> get(Integer index) 
	{
		return indexQueue.get(index);
	}

	public ConcurrentHashMap<Integer, BlockingQueue<String>> getQueue()
	{
		return indexQueue;
	}

	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return indexQueue.isEmpty(); 
	}

	/**
	 * not the size of mapping, but the size of all url queue's total size
	 * @return
	 */
	public int size() 
	{
		int t = 0;
		for(BlockingQueue<String>q: indexQueue.values())
		{
			t += q.size();
		}
		return t;
	}

	public String getStatString() 
	{
		return null;
	}
}
