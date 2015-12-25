package com.myapp.worker;

import java.util.HashSet;
import java.util.Set;

import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.RabinHashFunction;

/**
 * DUE: duplicate URL eliminator
 * remove the visited new urls
 * @author Haoyun 
 *
 */
public class DUE 
{
	// 8 bytes for each url, only 80MB for 10 million urls.
	Set<Long> urls = new HashSet<Long>(Const.MAX_DUE_SIZE); 
	RabinHashFunction rabin_hash = new RabinHashFunction();

	/**
	 * new url will add to set
	 * visited url will return null
	 * over capacity will also be dropped
	 * @param url
	 * @return
	 */
	public boolean process(String url)
	{
		if(urls.size() >= Const.MAX_FRONTQUEUE_SIZE)
		{
			return false;
		}

		long key = hash(url); 

		synchronized(urls)
		{
			if(urls.contains(key))
			{
				return false;
			}
			urls.add(key);
			return true; 
		}
	}

	/**
	 * store the encode of url for saving memory
	 * @param url
	 * @return
	 */
	long hash(String url)
	{
		return rabin_hash.hash(url);
	}
	
	public int size()
	{
		return urls.size();
	}
}
