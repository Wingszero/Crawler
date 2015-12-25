package com.myapp.worker.utils;

public class Const 
{
	//in ms
	public final static int CONNECTION_TIMEOUT = 5000;

	/**===========ROBOTS===============**/
	//robots.txt content type
	public static final String ROBOTS_PLAIN_TYPE = "text/plain";
	public static final String	ROBOTS_HTML_TYPE = "text/html";
	//public static final String[] ALL_ROBOTS_TYPES = {ROBOTS_HTML_TYPE, ROBOTS_PLAIN_TYPE}; 
	
	//no limit for the content type: some websites do not have content-type for robots.txt
	public static final String[] ALL_ROBOTS_TYPES = {};

	//robots.txt content len: 500KB 
	public static final int ROBOTS_MAX_LENGTH = 500000;

	//in secs 
	public static final Integer ROBOTS_DEFAULT_DELAY = 5;

	//use lowercase compare
	public static final String STR_DISALLOW="disallow";
	public static final String STR_ALLOW="allow";
	public static final String STR_USER_AGENT="user-agent";
	public static final String STR_SITEMAP="sitemap";
	public static final String STR_CRAWL_DELAY="crawl-delay";
	public static final String MY_USER_AGENT = "cis455crawler";
	public static final String ALL_USER_AGENT = "*";
	
	public static final String[] USER_AGENTS = {MY_USER_AGENT, ALL_USER_AGENT};
	/**===========ROBOTS end===============**/


	public static final int URL_QUEUE_MAXNUM = 100000;

	/**===========Fetch html ===============**/
	//original html max size: 800KB 
	public static final int HTML_MAX_LENGTH = 800000;

	//accept content type
	public static final String HTML_CONTENT_TYPE = "text/html";
	public static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
	public static final String XML_CONTENT_TYPE = "application/xml";
	public static final String[] ALL_HTML_TYPES = {HTML_CONTENT_TYPE, 
			XHTML_CONTENT_TYPE, XML_CONTENT_TYPE}; 

	//recursive depth of redirect status code of 3XX 
	public static final int REDIRECT_MAX_DEPTH = 2; 
	/**===========Fetch html end===============**/
	

	/**=================Cluster node config========================**/
	//10 secs
	public static final int STATUS_UP_TIME_INTERVAL = 10000; 
	//10 secs
	public static final long STORE_INTERVAL = 10000;
	//10 secs
	public static final long STAT_INTERVAL = 10000;


	public static final String GET = "GET";
	public static final String POST = "POST";

	//request fields
	public static final String PORT_STR = "port";
	public static final String STATUS_STR = "status";
	public static final String NUMTHREADS_STR = "numThreads"; 
	public static final String NUMWORKERS_STR = "numWorkers"; 
	public static final String NUMHOSTS_STR = "numHosts"; 
	public static final String RUNMINS_STR = "runMinutes";
	public static final String MY_WORKER_INDEX = "myWorkerIdx";

	//use for caching the active workers for a job
	public static final String ACTIVE_WORKERS_STR = "activeWorkers";

	public static final String WORKER_STR = "Worker"; 
	
	/*URL*/
	//worker->master
	public static final String UP_STATUS_URL 	= "/workerstatus"; 
	//master->worker
	public static final String RUN_CRAWL_URL 	= "/runcrawl"; 
	//worker->worker
	public static final String PUSH_URL = "/pushurl"; 
	//admin->worker
	public static final String STATUS_URL = "/status";
	//admin->worker
	public static final String TEST_CRAWLER = "/test_crawler";
	//master->worker
	public static final Object PUSH_URL_BATCH = "/push_url_batch";

	public static final String WORKER_DNS_PRE = "http://jasoncrawlerworker"; 
	public static final String WORKER_DNS_POST = ".elasticbeanstalk.com";
	
	/*worker status */
	public static final String RUNNING_STATUS 		= "running";
	public static final String WAITING_STATUS 		= "waiting";
	public static final String IDLE_STATUS 				= "idle";
	
	//cache size of push worker's url size
	public static final int PUSHURL_CACHE_SIZE = 2000;
	
	/**=================Cluster node config end========================**/
	
	
	/**=================worker node stat========================**/
	
	public static final String TOTAL_PAGE_NUM_STR = "total_page_num";
	
	public static final String TOTAL_HOST_NUM_STR = "total_host";

	public static final String SPEED_STR = "speed";
	
	public static final String TOTAL_PAGE_SIZE_STR = "total_page_size";

	public static final String MIN_PAGE_SIZE_STR = "min_page";

	public static final String MAX_PAGE_SIZE_STR = "max_page";

	public static final String AVG_PAGE_SIZE = "avg_page";
	
	public static final String DUE_NUM = "due_num";

	public static final String CONTENT_SEEN_NUM = "content_seen_num";
	
	/**=================worker node stat========================**/

	
	public static final String HTTP_PREFIX = "http://";

	public static final int MAX_FRONTQUEUE_SIZE = 1000000;

	// the factor between the numbers of active hosts and threadsNum
	public static final int ACTIVE_HOST_FACTOR = 30;

	public static final int MAX_DUE_SIZE = 10000000;

	// 20% chance to make a sub link of a hot page is also a hot page.
	public static final int HOT_SUBLINKS_CHANCE = 19;

	// 60 secs
	public static final long URL_FRONTIER_STORE_INTERVAL = 60000;
}
