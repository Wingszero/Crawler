package com.myapp.worker;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import com.myapp.worker.HostTimeHeap.HostTime;
import com.myapp.worker.db.DBConst;
import com.myapp.worker.db.URLFrontierDBWrapper;
import com.myapp.worker.db.URLQueueEntity;
import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.NormalizeURL;

/**
Contain URLs to be crawled, handle politeness and url priority. Include:

HostHeap: 
	a priority queue based on each active host's next can-crawl timestamp, when a thread ready to get the next url,     
	it will first pop the head of HostHeap (may waiting depends on timestamp) to locate the url queue belong to this host.

Frontend queue: 
	Similarity based url queue. Maintain two queues, a hot queue for hot pages and another normal-queue when     
	some backend queue becomes empty, it will first look for new url from hot queue, then look for it in normal queue 
	when hot queue is empty.
	A hot page: when url and its title contains anchor words, or its content contains over 10 anchor words, then we define it
	a hot page. Then all its sub-links will have a 20% chance to become a hot page and enqueue to hot queue.

 Reference : 
 	Junghoo Cho, Hector Garcia-Molina, Lawrence Page, Efficient Crawling Through URL Ordering

 Backend queue: Each active host map a backend queue, every thread will retrieve the next url from it.

 Host-Queue Map: A mapping between a host and its backend queue.

 * @author Haoyun 
 *
 */
public class URLFrontier 
{
	/* db */
	private URLFrontierDBWrapper  mydb; 
	private static storeRunner store_run; 
	private static Thread storeThread; 


	/*
	 * Priority queue: 
	 * head: the earlist host can crawl 
	 */
	private HostTimeHeap  hostHeap; 

	/*
	 * extact links will be added to frontQueue first (not hot pages)
	 */
	//private BlockingQueue<String>frontQueue;

	private URLQueueEntity frontQueue;

	/*
	 * extact links will be added to hotQueue first (hot pages)
	 */
	private URLQueueEntity hotQueue;

	/*
	 * key: host
	 * value: url queue belongs to this host 
	 */
	private BackEndQueue backQueue;

	/*
	 * host -> back-end queue index
	 */
	private ConcurrentHashMap<String, Integer>hostIndexMap = new ConcurrentHashMap<String, Integer>(); 

	private Worker crawl_ins;

	private int activeHostNum;
	private int curAssignBackQueueIdx;

	public URLFrontier(ArrayList<String> hosts, Worker crawler) 
	{
		long cur = System.currentTimeMillis();

		mydb = new URLFrontierDBWrapper();

		activeHostNum = crawler.getNumThreads() * Const.ACTIVE_HOST_FACTOR;

		//frontQueue = new LinkedBlockingQueue<String>(Const.MAX_FRONTQUEUE_SIZE);

		frontQueue = mydb.getUrlQueue(DBConst.FRONTEND_QUEUE_TYPE);
		print("frontQueue size: " + frontQueue.size());

		hotQueue = mydb.getUrlQueue(DBConst.HOT_QUEUE_TYPE);
		print("hotQueue size: " + hotQueue.size());

		backQueue = new BackEndQueue(activeHostNum); 

		hostHeap = new HostTimeHeap(activeHostNum);

		crawl_ins = crawler;

		curAssignBackQueueIdx = 0;

		beginStore();

		for(String url: hosts)
		{
			String h = NormalizeURL.getDomainName(url); 
			//has host
			if(hostIndexMap.containsKey(h))
			{
				// find the back-queue index
				int queueIndex = hostIndexMap.get(h); 
				// enqueue
				backQueue.put(queueIndex, url);
			}
			else if(curAssignBackQueueIdx < activeHostNum)
			{
				hostHeap.add(h, cur);
				hostIndexMap.put(h, curAssignBackQueueIdx);
				backQueue.put(curAssignBackQueueIdx, url);
				curAssignBackQueueIdx ++;
			}
			else
			{
				frontQueue.put(url);
			}
		}
		while(curAssignBackQueueIdx < activeHostNum && !frontQueue.isEmpty())
		{
			refillHostHeap(frontQueue.poll());
		}
		System.out.println("hostHeap size: " + hostHeap.size());
	}

	private void beginStore()
	{
		store_run = new storeRunner();
		storeThread = new Thread(store_run);
		storeThread.start();
	}

	public void printAll()
	{
		System.out.println("hostTimeQ: " + hostHeap.size());
		//System.out.println(hostSet);

		System.out.println("urlQueue:" + backQueue.size());
		//System.out.println(urlQueue.getQueue());

		System.out.println();
	}

	public int getHostIndex(String host)
	{
		if(host == null || host.isEmpty())
		{
			return -1;
		}
		if(hostIndexMap == null)
		{
			print("hostIndexMap is null:" + hostIndexMap);
			return -1;
		}
		return hostIndexMap.get(host);
	}

	/**
	 * 
	 * @return the earliest url can be crawled
	 */
	public String getNextURL()
	{
		HostTime ht = null; 
		ht = hostHeap.poll();
		if(ht == null)
		{
			//System.out.println("getNextUrl hostHeap is null !"); 
			return null;
		}
		long cur = System.currentTimeMillis();
		long next_time = ht.next_time; 
		long wait = next_time - cur;
		String host = ht.host;
		if(wait > 0)
		{
			try
			{
				System.out.println("crawl-delay: " + host + " " + wait);
				Thread.sleep(wait);
			}
			catch(Exception e)
			{
				//System.out.println("getNextUrl Thread sleep Exception: " + host);
				return null;
			}
		}

		int index = getHostIndex(host); 
		if(index == -1)
		{
			System.out.println("[ERROR] getHostIndex is -1 host: " + host);
			return null;
		}
		BlockingQueue<String> urlList = backQueue.get(index); 
		if(urlList == null || urlList.isEmpty())
		{
			System.out.println("[ERROR]  getNextUrl null urlList isEmpty(): host " + host);
			return null;
		}	
		return urlList.peek();
	}

	/**
	 * When curAssignBackQueueIdx < activeHostNum
	 * 
	 * @param url
	 */
	public void refillHostHeap(String url)
	{
		String h = NormalizeURL.getDomainName(url); 
		if(hostIndexMap.containsKey(h))
		{
			// find the back-queue index
			int queueIndex = hostIndexMap.get(h); 
			backQueue.put(queueIndex, url);
		}
		else
		{
			hostHeap.add(h, System.currentTimeMillis());
			hostIndexMap.put(h, curAssignBackQueueIdx);
			backQueue.put(curAssignBackQueueIdx, url);
			curAssignBackQueueIdx ++;
		}
	}

	/**
	 * add a new url to queue by host
	 * @param url
	 */
	public void addNewUrl(String url, Boolean isHot) 
	{
		if(isHot && hotQueue.size() < Const.MAX_FRONTQUEUE_SIZE)
		{
			try
			{
				hotQueue.put(url);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(frontQueue.size() < Const.MAX_FRONTQUEUE_SIZE)
		{
			try
			{
				frontQueue.put(url);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return backQueue.isEmpty();
	}

	/**
	 * in secs
	 * @param host
	 * @return
	 */
	public Integer getHostDelay(String host) 
	{
		Integer d = crawl_ins.getHostDelay(host);
		return d == null ? Const.ROBOTS_DEFAULT_DELAY * 1000: d;
	}

	public static void print(String data)
	{
		System.out.println(data);
	}

	public int size() 
	{
		return backQueue.size();
	}

	public String getStatString() 
	{
		String status = "hostHeap size: " + hostHeap.size() + " ";
		status += ("hot queue size: " + hotQueue.size() + " "); 
		status += ("normal queue size: " + frontQueue.size() + " "); 
		/*
		status += ("back-end queue: "); 
		for(int i = 0; i < activeHostNum; ++i)
		{
			System.out.println(backQueue.get(i).size());
		}
		 */
		return status;
	}

	/**
	 *  refill an empty urlQueue using front_queue
	 * @param front_queue
	 * @param back_queue
	 * @param index: the index of this urlQueue 
	 * @param host: the last host belongs to this urlQueue
	 * @return
	 */
	public boolean refillUrlQueue(URLQueueEntity front_queue, BlockingQueue<String>back_queue, int index, String host)
	{
		synchronized(front_queue)
		{
			while(!front_queue.isEmpty())
			{
				String newUrl = front_queue.poll();
				String newHost = NormalizeURL.getDomainName(newUrl); 
				if(newHost ==null) continue;

				// new host
				if(!hostIndexMap.containsKey(newHost))
				{
					hostIndexMap.put(newHost, index);
					try 
					{
						back_queue.put(newUrl);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					//just fetched, need a delay
					if(newHost.equals(host)) 
					{
						hostHeap.add(newHost, System.currentTimeMillis() + getHostDelay(host));
					}
					else
					{
						hostHeap.add(newHost, System.currentTimeMillis()); 
					}
					return true;
				}
				else //existed host
				{
					int index2 = getHostIndex(newHost);
					if(index2 == -1)
					{
						print("[ERROR] getHostIndex == null");
						return false;
					}
					BlockingQueue<String>urlQueue2 = backQueue.get(index2);
					try 
					{
						urlQueue2.put(newUrl);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 *  After a thread finish fetching a page
	 * @param url_str
	 */
	public void updateAfterFetch(String url_str) 
	{
		String host = NormalizeURL.getDomainName(url_str); 
		int index = getHostIndex(host);
		if(index == -1)
		{
			System.out.println("[ERROR] getHostIndex is -1 host: " + host);
			return;
		}

		BlockingQueue<String>urlQueue = backQueue.get(index);
		if(urlQueue.isEmpty())
		{
			System.out.println("[ERROR] updateAfterFetch urlQueue.isEmpty host: " + host + " " + "index: " + index);
		}
		else
		{
			urlQueue.poll();
		}

		if(!urlQueue.isEmpty())
		{
			synchronized(hostHeap)
			{
				hostHeap.add(host, System.currentTimeMillis() + getHostDelay(host));
			}
		}
		else // refill the empty Queue from front-end Queue
		{
			hostIndexMap.remove(host);
			boolean ok = refillUrlQueue(hotQueue, urlQueue, index, host);
			if(!ok)
			{
				refillUrlQueue(frontQueue, urlQueue, index, host);
			}
		}
	}

	/*
	 * Use for write urls to disk
	 */
	private class storeRunner implements Runnable 
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
					synchronized(frontQueue)
					{
						Thread.sleep(Const.URL_FRONTIER_STORE_INTERVAL);
						long st = System.currentTimeMillis();

						mydb.updateQueue(frontQueue);
						mydb.updateQueue(hotQueue);
						mydb.sync();
						frontQueue = mydb.getUrlQueue(DBConst.FRONTEND_QUEUE_TYPE);
						hotQueue = mydb.getUrlQueue(DBConst.HOT_QUEUE_TYPE);

						print("url frontier db sync: used time" + (System.currentTimeMillis() - st));
						print("frontQueue size" + frontQueue.size());
						print("hotQueue size" + hotQueue.size());
					}
				} 
				catch (InterruptedException e) 
				{
				}
			}
		}
	}
}
