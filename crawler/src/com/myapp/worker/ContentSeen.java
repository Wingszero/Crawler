package com.myapp.worker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.myapp.worker.utils.RabinHashFunction;

/**
 * Use RabinHash for hashing the webpage content
 * to determine the similarity of webpages
 * @author Haoyun 
 *
 */
public class ContentSeen 
{
	private Set<Long> url_content = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
	private RabinHashFunction rabin_hash = new RabinHashFunction();
	private int filterNum = 0;

	public boolean contains(long key)
	{
		return url_content.contains(key);
	}

	/**
	 * store the hash of url content for saving memory
	 * @param url
	 * @return
	 */
	public long hash(String content)
	{
		return rabin_hash.hash(content);
	}

	synchronized public void add(long hash_key)
	{
		url_content.add(hash_key);
	}

	/**
	 * if content is new, return true
	 * else return false
	 * @param content
	 * @return
	 */
	public boolean process(String content)
	{
		if(content == null || content.isEmpty())
		{
			addFilterNum();
			return false;
		}
		long key = hash(content);
		if(url_content.contains(key))
		{
			addFilterNum();
			return false;
		}
		url_content.add(key);
		return true;
	}
	
	synchronized private void addFilterNum() 
	{
		filterNum ++;
	}

	public int size()
	{
		return url_content.size();
	}

	public int getFilterNums() 
	{
		return filterNum;
	}
}
