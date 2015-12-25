package com.myapp.worker;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import com.myapp.worker.aws.MessageReceiver;
//import com.myapp.worker.aws.MessageSender;
import com.myapp.worker.db.DBWrapper;
import com.myapp.worker.db.WebPageEntity;
import com.myapp.worker.utils.Common;
import com.myapp.worker.utils.Const;
import com.myapp.worker.utils.NormalizeURL;
import com.myapp.worker.utils.Robots.RobotsTxtInfo;

public class Worker 
{
	Random rand = new Random();

	private int numThreads;

	private DUE due = new DUE(); 

	private HostSplitter hostSplitter; 

	private URLFrontier url_frontier; 

	private URLFilter url_filter; 

	private ContentSeen content_seen = new ContentSeen(); 

	//TODO:
	//check threads' s status
	//private ArrayList<WorkerThread>threads;

	/*for multiple instances*/
	private long runMinutes;

	private DBWrapper mydb; 

	/*robots.txt for each host*/
	//key: host url
	//value: Boolean or RobotsTxtInfo. Boolean for no hosts has no robots.txt
	private ConcurrentHashMap<String, Object>robotsCache = new ConcurrentHashMap<String, Object>();

	/*Thread workerThreadPool for crawling*/
	ExecutorService workerThreadPool; 

	/*for statistics*/
	private static String status;
	private static long startTime;
	private static int crawlNums = 0;

	// url nums cannot get response code 
	//private static int responseFailNums = 0;

	// url nums filtered by DUE
	private static int dueFilterNums = 0;
	// url nums filtered by HostSplitter 
	private static int hostSplitNum = 0;
	// url nums filtered by URLFilter 
	private static int urlFilterNum = 0; 
	// hot page num
	private static int hotPageNum = 0; 

	private static ConcurrentHashMap<Integer, Integer>responseCodeStat = new ConcurrentHashMap<Integer, Integer>(); 

	private static statRunner stat_run; 
	private static Thread statThread; 

	private static storeRunner store_run; 
	private static Thread storeThread; 

	//private static  

	private int maxPageSize = -1;
	private int minPageSize = Const.HTML_MAX_LENGTH;
	private double totalPageSize = 0;
	private double avgPageSize;

	//private int dropPages = 0;

	/**
	 * For multiple workers
	 * @param numThreads
	 * @param hosts
	 * @param workerAddress: each worker's ip address, for host split
	 * @param myWorkerIdx: my index in workerAddress, so the url belongs to me won't be drop
	 */
	public Worker(int numThreads, ArrayList<String>hosts, int totalWorkers, int myWorkerIdx,  long runMinutes)
	{
		startTime = System.currentTimeMillis();

		this.setNumThreads(numThreads);
		this.runMinutes = runMinutes;
		
		/*URL Frontier*/
		url_frontier = new URLFrontier(hosts, this);

		robotsCache = new ConcurrentHashMap<String, Object>();

		/*CookieManager */
		CookieHandler.setDefault(new CookieManager());

		/*DB*/
		mydb = new DBWrapper();

		/*Thread Pool*/
		workerThreadPool = Executors.newFixedThreadPool(numThreads);

		/*URL Filter*/
		url_filter = new URLFilter(); 

		/*Host Splitter*/
		hostSplitter = new HostSplitter(totalWorkers, myWorkerIdx, this);
	}

	public void run() 
	{
		transStatus(Const.RUNNING_STATUS);
		beginStat();
		beginStore();

		for(int i = 0; i < numThreads; ++i)
		{
			workerThreadPool.execute(new WorkerThread(this));
		}
		workerThreadPool.shutdown();
		try 
		{
			workerThreadPool.awaitTermination(runMinutes, TimeUnit.MINUTES);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}

		System.err.println("Pool terminate!!!!");

		stopStat();
		stopStore();

		transStatus(Const.IDLE_STATUS);

		print("mydb is closing...");
		mydb.close();
	}

	private void stopStat() 
	{
		displayStat();

		if (statThread != null) 
		{
			stat_run.terminate();
			try 
			{
				statThread.join();
			} 
			catch (InterruptedException e) 
			{
			}
		}
	}

	private void stopStore() 
	{
		if (storeThread != null) 
		{
			store_run.terminate();
			try 
			{
				storeThread.join();
			} 
			catch (InterruptedException e) 
			{
			}
		}
	}

	private void beginStat()
	{
		stat_run = new statRunner();
		statThread = new Thread(stat_run);
		statThread.start();
	}

	private void beginStore()
	{
		store_run = new storeRunner();
		storeThread = new Thread(store_run);
		storeThread.start();
	}

	public String getStatusString() 
	{
		return ("Status: " + status); 
	}

	public String getTotalPageNumString()
	{
		return ("Pages: " + crawlNums); 
	}

	public String getSpeedString()
	{
		return ("Speed: " + crawlNums/((System.currentTimeMillis() - startTime)/1000F) + " page /s"); 
	}

	public String getTotalPageSizeString()
	{
		return ("Total size:" +  totalPageSize/1000000F + " MB"); 
	}

	public String getAvgPagesString()
	{
		avgPageSize = totalPageSize/ crawlNums;
		return ("Avg:" +  avgPageSize/1000F + " KB"); 
	}

	public String getMaxPageSizeString()
	{
		return ("Max:" +  maxPageSize/1000F +  " KB"); 
	}

	public String getMinPageSizeString()
	{
		return ("Min:" +  minPageSize/1000F + " KB"); 
	}

	public String getDBWebPageNumString()
	{
		return "webpage db size:" +  mydb.getWebPageNum(); 
	}

	private void displayStat() 
	{
		print("===========");
		print(getTotalPageNumString());
		print(getUrlFrontierString());
		print(getDUEString());
		print(getURLFilterString());
		print(getHostSplitterString());
		print(getContentSeenString());
		print(getResponseCodeString());
		print(getRobotsCacheString());
		print(getStatusString());
		print(getSpeedString());
		print(getTotalPageSizeString());
		print(getAvgPagesString());
		print(getMinPageSizeString());
		print(getMaxPageSizeString());
		print(getDBWebPageNumString());
		print("===========");
	}

	private String getHostSplitterString() 
	{
		return "HostSplitter filter size:" +  hostSplitNum; 
	}

	private String getURLFilterString() 
	{
		return "URLFilter filter size:" +  urlFilterNum; 
	}

	private String getRobotsCacheString() 
	{
		return "robots cache size:" +  robotsCache.size(); 
	}

	private String getResponseCodeString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Response code status:\n");
		for(Entry<Integer, Integer> entry : responseCodeStat.entrySet())
		{
			sb.append(entry.getKey() + " " + entry.getValue() + "\n");
		}
		return sb.toString();
	}

	public String getContentSeenString() 
	{
		return "Content-Seen filter nums: " + content_seen.getFilterNums();
	}

	public String getUrlFrontierString() 
	{
		String s = "URLFrontier: " + url_frontier.getStatString();
		s += (" HotPages hits"  + hotPageNum);
		return s;
	}

	public String getDUEString() 
	{
		return "DUE filter urls: " + dueFilterNums; 
	}

	public class statRunner implements Runnable 
	{
		private volatile boolean running = true;
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
					Thread.sleep(Const.STAT_INTERVAL);
					displayStat();
				} 
				catch (InterruptedException e) 
				{
				}
			}
		}
	}

	/*
	 * Use for write pages to disk
	 */
	private class storeRunner implements Runnable 
	{
		private volatile boolean running = true;
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
					Thread.sleep(Const.STORE_INTERVAL);
					long st = System.currentTimeMillis();
					mydb.sync();
					print("db sync finish: used time" + (System.currentTimeMillis() - st));
				} 
				catch (InterruptedException e) 
				{
				}
			}
		}
	}

	public int getNumThreads() 
	{
		return numThreads;
	}

	public void setNumThreads(int numThreads) 
	{
		this.numThreads = numThreads;
	}

	public synchronized void statPage(String url, String content)
	{
		crawlNums ++;
		int len = content.getBytes().length;
		maxPageSize = Math.max(maxPageSize, len); 
		minPageSize = Math.min(minPageSize, len); 
		totalPageSize += len;
	}

	/**
	 *  store url (key), title, content and out-bound links of the page
	 * @param url
	 * @param title
	 * @param content
	 * @param links
	 */
	public void storeWebPage(String url, String title, String content, ArrayList<String>links )
	{
		mydb.storeWebPage(url, title, content, links);
	}

	public static void print(String data)
	{
		System.out.println(data);
	}

	public boolean processDUE(String url) 
	{
		return due.process(url);
	}

	public boolean containsRobotsHost(String host) 
	{
		return robotsCache.containsKey(host);
	}

	public void cacheRobotsTxt(String h, Object robots) 
	{
		robotsCache.put(h, robots);
	}

	public Object getRobots(String host) 
	{
		return robotsCache.get(host);
	}

	public ConcurrentHashMap<String, Object> getRobotCache() 
	{
		return robotsCache;
	}

	/*
	public synchronized void storeRobotsContent(String host, String content) 
	{
		//print("storeRobotsContent: " + host);
		mydb.storeRobotsTxt(host, content);
	}
	 */

	public WebPageEntity getWebPage(String url_str) 
	{
		return mydb.getWebPage(url_str);
	}

	public boolean processHostSplit(String url, String host) 
	{
		return hostSplitter.process(url, host);
	}

	/**
	 * in millisecond
	 * @param host
	 * @return
	 */
	public Integer getHostDelay(String host) 
	{
		Object robots = getRobots(host);
		if(robots != null && robots instanceof RobotsTxtInfo)
		{
			return ((RobotsTxtInfo) robots).getCrawlDelay(Const.MY_USER_AGENT); 
		}
		return Const.ROBOTS_DEFAULT_DELAY  * 1000; 
	}

	public String getNextUrl() 
	{
		return url_frontier.getNextURL();
	}

	/**
	 * 
	 * @param url
	 * @param host
	 */
	public void addNewUrl(String url, Boolean isHot) 
	{
		url_frontier.addNewUrl(url, isHot);
	}

	public void transStatus(String sc) 
	{
		status = sc;
	}

	/**
	 * From Host Splitter: push url to worker 
	 * @param message
	 * @param idx
	 */
	/*
	@Deprecated
	public void pushUrlToWorker(String newurl, int idx) 
	{
		if(idx != myWorkerIdx)
		{
			String url = Const.WORKER_DNS_PRE + String.valueOf(idx) + Const.WORKER_DNS_POST; 
			url += Const.PUSH_URL;

			String charset = "UTF-8";
			String query;
			try 
			{
				query = String.format("info"+ "=%s", URLEncoder.encode(newurl, charset));
				url = url + "?" + query;
				print("send url to worker" + idx + " : " + url);
				HttpFetcher.getHttpURLConnection(url, Const.CONNECTION_TIMEOUT, Const.GET, 0);
			} 
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
		}
	}
	 */

	public void processPushedUrl(String newUrl) 
	{

	}

	/**
	 * process Url from other instances
	 * @param urls
	 */
	public void processPushedUrl(String[] urls) 
	{
		print("processPushedUrl size: " + urls.length);
		HashSet<String>urlSet = new HashSet<String>();
		for(String url: urls)
		{
			urlSet.add(url);
		}
		processLinks(urlSet, false);
	}

	public boolean processUrlFilter(String url, String host) 
	{
		return url_filter.process(url, host);
	}

	/**
	 * fetch robots.txt to check if this url is allowed.
	 * @param url_str
	 * @return
	 */
	public boolean isAllowed(String url_str) 
	{
		Object robo = fetchRobotsTxt(url_str); 
		return url_filter.isAllow(url_str, robo);
	}

	public boolean processContentSeen(String content) 
	{
		return content_seen.process(content);
	}

	/**
	 * 
	 * @param list
	 */
	public void processLinks(HashSet<String> links, Boolean isHot)
	{
		if(!isHot)
		{
			for(String url: links)
			{
				processLink(url, false);
			}
		}
		else
		{
			for(String url: links)
			{	
				processLink(url, rand.nextInt(100)  <= Const.HOT_SUBLINKS_CHANCE);
			}
		}
	}	

	public void processLink(String url, Boolean isHot) 
	{
		String host = NormalizeURL.getDomainName(url);
		//URLFilter
		boolean ok = processUrlFilter(url, host); 
		if(!ok) 
		{
			addURLFilterNums();
			return;
		}

		//HostSplitter
		ok = processHostSplit(url, host);
		if(!ok) 
		{
			addHostSplitNums();
			return;
		}

		//DUE
		ok = processDUE(url);
		if(!ok)
		{
			addDUEFilterNums();
			return;
		}

		//add to url frontier
		addNewUrl(url, isHot); 
	}	

	synchronized private void addDUEFilterNums() 
	{
		dueFilterNums++;
	}

	synchronized private void addHostSplitNums() 
	{
		hostSplitNum ++;
	}

	synchronized private void addURLFilterNums() 
	{
		urlFilterNum ++;
	}

	synchronized public void addHotPage() 
	{
		hotPageNum ++;
	}

	public Object fetchRobotsTxt(String url) 
	{
		URL url_ins = Common.getURL(url);
		return fetchRobotsTxt(url_ins);
	}

	/**
	 * get the robots.txt by the url
	 * check the host robots.txt cache first
	 * if not exists, fetch it 
	 * if bad happen, drop this url
	 * @param url_str
	 * @return
	 */
	public Object fetchRobotsTxt(URL url_ins) 
	{
		if(url_ins == null)
		{
			return null;
		}

		String host = NormalizeURL.getDomainName(url_ins); 
		if(host.isEmpty()) 
		{
			return null;
		}

		if(containsRobotsHost(host))
		{
			return getRobots(host);
		}

		String protocol = url_ins.getProtocol();
		if(!protocol.equals("http") && !protocol.equals("https"))
		{
			print("fetchRobotsTxt bad protocol: " + protocol);
			return null;
		}

		//fetch robots.txt content
		String robot_url = url_ins.getProtocol() + "://" + host + "/robots.txt";

		Object robo = HttpFetcher.fetchRobots(robot_url, 0);
		return cacheRobotsObj(host, robo);
	}

	/**
	 * 
	 * @param h
	 * @param robots
	 */
	private Object cacheRobotsObj(String h, Object robots) 
	{
		if(robots == null)
		{
			robots = new Boolean(true); 
		}
		cacheRobotsTxt(h, robots);
		return robots;
	}

	public void updateURLFrontierAfterFetch(String url_str) 
	{
		url_frontier.updateAfterFetch(url_str);
	}

	synchronized public static void addResponseFailNum() 
	{
		if(!responseCodeStat.containsKey(-1))
		{
			responseCodeStat.put(-1, 1);
		}
		else
		{
			responseCodeStat.put(-1, responseCodeStat.get(-1) + 1);
		}
	}

	public static void addResponseCodeStatus(int sc) 
	{
		if(!responseCodeStat.containsKey(sc))
		{
			responseCodeStat.put(sc, 1);
		}
		else
		{
			responseCodeStat.put(sc, responseCodeStat.get(sc) + 1);
		}
	}


}
