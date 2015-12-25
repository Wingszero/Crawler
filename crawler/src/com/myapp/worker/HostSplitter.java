package com.myapp.worker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.SHA1HashFunction;

/**
 * Hash the url to a index of a worker
 * if the index is not this crawler, cache it
 * if cache size is max, send it to that worker.
 * 
 * @author Haoyun 
 *
 */
public class HostSplitter 
{
	private int myIndex;

	// hash function for each host into different worker index
	private SHA1HashFunction hashFunction;

	//caching urls for other workers
	private ConcurrentHashMap<Integer, HashSet<String>>pushUrlCache;

	public HostSplitter(int totalWorkers, int idx, Worker worker)
	{
		hashFunction = new SHA1HashFunction(totalWorkers);
		myIndex  = idx;

		pushUrlCache = new ConcurrentHashMap<Integer, HashSet<String>>();
		for(int i = 0; i < totalWorkers; ++i)
		{
			pushUrlCache.put(i, new HashSet<String>());
		}
	}

	/**
	 * hash the host of url 
	 * and cache it for other workers
	 * @param url
	 * @param host 
	 * @return
	 */
	public boolean process(String url, String host) 
	{
		// hash the host
		int idx = hashFunction.getHashIndex(host);
		if(idx == myIndex)
		{
			return true;
		}

		//System.out.println("host split ====" + url + " " + idx);

		synchronized(pushUrlCache)
		{
			HashSet<String>cache = pushUrlCache.get(idx);
			cache.add(url);
			if(cache.size() > Const.PUSHURL_CACHE_SIZE)
			{
				//send url to worker
				pushUrlListToWorker(cache, idx); 

				//clear cache
				pushUrlCache.put(idx, new HashSet<String>());
			}
		}
		return false;
	}

	/**
	 *  http post to the worker
	 * @param cache
	 * @param idx
	 */
	public void pushUrlListToWorker(HashSet<String> cache, int idx) 
	{
		System.out.println("push to worker" + idx); 

		//worker's address
		String url_str = Const.WORKER_DNS_PRE + String.valueOf(idx) + 
				Const.WORKER_DNS_POST + Const.PUSH_URL_BATCH;

		StringBuilder sb = new StringBuilder();
		String mySendInfo = "";

		for(String url: cache)
		{
			try 
			{
				url = URLEncoder.encode(url, "UTF-8");
				sb.append(url.concat(";"));
			} 
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
		}

		mySendInfo = sb.toString(); 
		Thread thread = new Thread(new pushThread(url_str, mySendInfo)); 
		thread.start();
	}	
}
