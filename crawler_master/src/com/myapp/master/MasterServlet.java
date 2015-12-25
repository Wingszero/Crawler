package com.myapp.master;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class MasterServlet extends HttpServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * key: worker's address 
	 * values: status, crawl stat 
	 */
	private Hashtable<String, Hashtable<String, String>>workerStatus 
	= new Hashtable<String, Hashtable<String, String>>();

	private String initialHostPath;
	private ArrayList<String>hosts;

	public MasterServlet() throws ServletException
	{
		init();
	}


	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		initialHostPath = config.getInitParameter("host_path"); 
		print("initialHostPath: " + initialHostPath);
		if(isEmpty(initialHostPath))
		{
			print("master init failed, the initialHostPath init parameter is null");
			System.exit(-1);
		}
		readHost();
	}

	/**
	 * eg:field1=value1&field2=value2&field3=value3...
	 * @param query
	 * @return key-value of query string
	 */
	public Hashtable<String, String> parseQueryString(String query)
	{
		Hashtable<String, String>kv = new Hashtable<String, String>();
		//field=value
		String[] sp = query.split("&");
		for(int i = 0; i < sp.length; ++i)
		{
			String[] pair = sp[i].split("=");
			if(pair == null)
			{
				continue;
			}
			String key = pair[0].trim();
			String value = ""; 
			if(pair.length == 2)
			{
				value = pair[1].trim(); 
			}
			kv.put(key, value);
		}
		return kv;
	}

	/*
	 * mainly deal with two process:
	 *  from URL: /workerstatus
	 *  from URL: /status
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		response.setContentType("text/html");
		PrintWriter out; 
		try
		{
			out = response.getWriter();
		}
		catch(IOException e)
		{
			return;
		}
		out.println("<html><head><title>Master</title></head>");
		out.println("<body>Hi, I am the master!</body></html>");

		String url = request.getServletPath();

		if(url.equals(Common.WORKER_STATUS_URL))
		{
			processUpdateStatus(request, response);
		}
		else if(url.equals(Common.STATUS_URL))
		{
			processCheckStatus(request, response);
		}
		else if(url.equals(Common.TEST_POST_URL))
		{
			try
			{
				out = response.getWriter();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return;
			}
			out.println("<form action=\"" + Common.TEST_POST_URL +"\"method=\"post\">");
			out.println("<input type=\"submit\" />");
			out.println("</form>");
		}
	}

	/**
	 *From the worker: request to update its status 
	 *in parameters: a query string
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void processUpdateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String query_string = request.getQueryString();
		//print(" processWorkerStatus: query is : " + query_string);
		if(isEmpty(query_string))
		{
			print("ERROR: processWorkerStatus: query is null: " + query_string);
			return;
		}
		String charset = "UTF-8";
		query_string = URLDecoder.decode(query_string, charset);
		Hashtable<String, String>worker_state = parseQueryString(query_string);
		if(worker_state == null)
		{
			print("ERROR: processWorkerStatus: query is bad: " + query_string);
			return;
		}

		//update time stamp
		worker_state.put(Common.LAST_QUERY, String.valueOf(System.currentTimeMillis()));
		String ip = request.getRemoteAddr();

		//worker key: ip:port
		workerStatus.put(ip, worker_state);
		print("Master: get worker update from: " + ip + " query: " + query_string); 
	}

	/**
	 * Let admin to check all status of workers
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	private void processCheckStatus(HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("text/html");
		showTaskForm(request, response);
		showStatus(request, response);
	}

	/**
	 * Show status of all workers:
	 * IP:port		/status		/job	/keys_read		/keys_written
	 * @param request
	 * @param response
	 */
	private void showStatus(HttpServletRequest request, HttpServletResponse response) 
	{
		PrintWriter out; 
		try
		{
			out = response.getWriter();
		}
		catch(IOException e)
		{
			print("ERROR: response.getWriter IOException!");
			return;
		}

		//worker key: IP:PORT
		ArrayList<String>active_workers = getActiveWorkers(false);

		out.println("Status	Pages	Hosts	Speed	Min	Max	Avg </P>");
		for(String workerAddr: active_workers)
		{
			Hashtable<String, String>values = workerStatus.get(workerAddr);
			String info = "<P>" + workerAddr + "	";
			info += (values.get(Common.STATUS_STR) + "	"); 
			info += (values.get(Common.TOTAL_PAGE_NUM_STR) + "	"); 
			info += (values.get(Common.TOTAL_HOST_NUM_STR) + "	"); 
			info += (values.get(Common.DUE_NUM) + "	"); 
			info += (values.get(Common.CONTENT_SEEN_NUM) + "	"); 
			info += (values.get(Common.SPEED_STR) + "	"); 
			info += (values.get(Common.TOTAL_PAGE_SIZE_STR) + "	"); 
			info += (values.get(Common.MIN_PAGE_SIZE_STR) + "	"); 
			info += (values.get(Common.MAX_PAGE_SIZE_STR) + "	"); 
			info += (values.get(Common.AVG_PAGE_SIZE) + "	"); 
			info +=  "	</P>";
			out.println(info);
		}
	}

	/**
	 * return all the active workers, 
	 * mean its last update is within 30 secs
	 * @return
	 */
	private ArrayList<String> getActiveWorkers(boolean needIdle) 
	{
		ArrayList<String>res = new ArrayList<String>();
		long cur = System.currentTimeMillis();
		for(String workerAddr: workerStatus.keySet())
		{
			long last; 
			String last_str  = workerStatus.get(workerAddr).get(Common.LAST_QUERY);
			try
			{
				last = Long.parseLong(last_str); 
			}
			catch(NumberFormatException e)
			{
				print("ERROR: NumberFormatException in parse last query, worker from: " 
						+ workerAddr + " ; Last query: " + last_str); 
				continue;
			}
			if(cur - last > Common.ACTIVE_TIME_INTERVAL) 
			{
				continue;
			}
			if(needIdle)
			{
				String s = workerStatus.get(workerAddr).get(Common.STATUS_STR);
				if(!isEmpty(s) && s.equals(Common.IDLE_STATUS))
				{
					res.add(workerAddr);
				}
			}
			else
			{
				res.add(workerAddr);
			}
		}
		return res;
	}

	/**
	 * apply a job to all idle workers 
	 * @param request
	 * @param response
	 */
	private void showTaskForm(HttpServletRequest request, HttpServletResponse response)
	{
		PrintWriter out; 
		try
		{
			out = response.getWriter();
		}
		catch(IOException e)
		{
			print("ERROR: response.getWriter IOException!");
			return;
		}
		out.println("<form action=\"" + Common.STATUS_URL +"\"method=\"post\">");

		out.println("<P>" + "crawl threads:" + "</P>");
		out.println("<input type=\"text\" name=\"" + Common.NUMTHREADS_STR + "\" value=\"\" />");
		out.println("<P>" + "default: 150" + "</P>");

		out.println("<P>" + "run minutes:" + "</P>");
		out.println("<input type=\"text\" name=\"" + Common.RUNMINS_STR  + "\" value=\"\" />");
		out.println("<P>" + "default: 1 minute" + "</P>");

		out.println("<input type=\"submit\" />");
		out.println("</form>");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String url = request.getServletPath();
		if(url.equals(Common.STATUS_URL))
		{
			response.setContentType("text/html");
			PrintWriter out; 
			try
			{
				out = response.getWriter();
			}
			catch(IOException e)
			{
				return;
			}

			dispatchCrawl(request, response);
			out.println("<html><head><title>Master</title></head>");
			out.println("<body>submittted.</body></html>");
			out.flush();
			out.close();
		}
		/*
		else if(url.equals(Common.TEST_POST_URL))
		{
			response.setContentType("text/html");
			PrintWriter out; 
			try
			{
				out = response.getWriter();
			}
			catch(IOException e)
			{
				return;
			}
			dispatchTestPost(request, response);
			out.println("<html><head><title>Master</title></head>");
			out.flush();
			out.close();
		}
		*/
	}

//	/**
//	 * From request: a form contains the info of a job 
//	 * send POST to each active workers
//	 * @param request
//	 * @param response
//	 * @throws IOException 
//	 */
//	public void dispatchTestPost(HttpServletRequest request, HttpServletResponse response) 
//	{
//		String numThreads = "200"; 
//		String runMinutes = "10"; 
//		int numOfWorkers = 1; 
//		String numWorkers = String.valueOf(numOfWorkers); 
//		print("dispatchJobtoWorkers: numWorkers is " + numWorkers);
//
//		String term = ";";
//		String send_info = Common.NUMTHREADS_STR + "=" + numThreads + term; 
//		send_info = send_info + Common.NUMWORKERS_STR + "=" +  numWorkers + term; 
//		send_info = send_info + Common.RUNMINS_STR + "=" +  runMinutes + term; 
//
//		/*determine which host belongs to which worker*/
//		SHA1HashFunction hashFunc = new SHA1HashFunction(numOfWorkers);
//		Hashtable<Integer, HashSet<String>>hash_host = hashHost(hashFunc, hosts); 
//
//		/*new threadpool*/
//		ExecutorService executor = Executors.newFixedThreadPool(numOfWorkers);
//		String charset = "UTF-8";
//		for(int i = 0; i < numOfWorkers; ++i)
//		{
//			//determine worker index
//			String mySendInfo = send_info + Common.MY_WORKER_INDEX + "=" +  String.valueOf(i) + term;
//
//			//send url
//			String workerAddr = Common.WORKER_DNS_PRE + String.valueOf(i) + Common.WORKER_DNS_POST; 
//			String url_str = workerAddr + Common.TEST_POST_URL; 
//			print("dispatch TestPost to worker: " + i + " url: " + url_str);
//
//			StringBuilder sb = new StringBuilder();
//			HashSet<String>thisHosts = hash_host.get(i);
//			for(String h: thisHosts)
//			{
//				try 
//				{
//					String url = URLEncoder.encode(h, charset);
//					sb.append("h=" + url + ";");
//				} 
//				catch (UnsupportedEncodingException e) 
//				{
//					e.printStackTrace();
//				}
//			}
//			mySendInfo += (sb.toString());
//			//System.out.println("send_info:" + send_info);
//			executor.execute(new dispatchThread(url_str, mySendInfo)); 
//		}
//		executor.shutdown();
//	}

	/**
	 * From request: a form contains the info of a job 
	 * send POST to each active workers
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public void dispatchCrawl(HttpServletRequest request, HttpServletResponse response)
	{
		ArrayList<String>active_workers = getActiveWorkers(true);
		if(isEmpty(active_workers))
		{
			print("dispatchJobtoWorkers: active_workers is empty!");
			return;
		}

		String numThreads = request.getParameter(Common.NUMTHREADS_STR);
		if(isEmpty(numThreads))
		{
			numThreads = "150";
		}

		String runMinutes = request.getParameter(Common.RUNMINS_STR);
		if(isEmpty(runMinutes))
		{
			runMinutes = "600";
		}

		String numWorkers = String.valueOf(active_workers.size());
		if(isEmpty(numWorkers))
		{
			print("dispatchJobtoWorkers: numWorkers is empty!");
			return;
		}
		print("dispatchJobtoWorkers: numWorkers is " + numWorkers);

		String term = ";";
		String send_info = Common.NUMTHREADS_STR + "=" + numThreads + term; 
		send_info = send_info + Common.NUMWORKERS_STR + "=" +  numWorkers + term; 
		send_info = send_info + Common.RUNMINS_STR + "=" +  runMinutes + term; 

		/*determine which host belongs to which worker*/
		SHA1HashFunction hashFunc = new SHA1HashFunction(active_workers.size());

		Hashtable<Integer, HashSet<String>>hash_host = hashHost(hashFunc, hosts);

		/*new threadpool*/
		ExecutorService executor = Executors.newFixedThreadPool(active_workers.size());
		String charset = "UTF-8";

		HashMap<String, String>sendInfo = new HashMap<String, String>();

		for(int i = 0; i < active_workers.size(); ++i)
		{
			//determine worker index
			String mySendInfo = send_info + Common.MY_WORKER_INDEX + "=" +  String.valueOf(i) + term;

			//send url
			String workerAddr = Common.WORKER_DNS_PRE + String.valueOf(i) + Common.WORKER_DNS_POST; 
			String url_str = workerAddr + Common.RUN_CRAWL_URL; 
			
			print("dispatch crawl to worker: " + i + " url: " + url_str + " send_info: " + mySendInfo);

			StringBuilder sb = new StringBuilder();
			HashSet<String>thisHosts = hash_host.get(i);
			for(String h: thisHosts)
			{
				try 
				{
					String url = URLEncoder.encode(h, charset);
					sb.append("h=" + url + ";");
				} 
				catch (UnsupportedEncodingException e) 
				{
					e.printStackTrace();
				}
			}
			mySendInfo += sb.toString();
			sendInfo.put(url_str, mySendInfo);
		}

		for (Map.Entry<String, String> entry : sendInfo.entrySet())
		{
			executor.execute(new dispatchThread(entry.getKey(), entry.getValue())); 
		}
		executor.shutdown();
	}

	/**
	 * read the host file from disk
	 */
	public void readHost() 
	{
		WebsitesLoader loader = new WebsitesLoader();
		hosts = loader.getHosts(initialHostPath, this.getServletContext()); 
		print("read hosts: " + hosts.size());
	}

	private boolean isEmpty(String s)
	{
		return s == null || s.trim().isEmpty();
	}

	private boolean isEmpty(ArrayList<String>array)
	{
		return array == null || array.isEmpty();
	}

	/**
	 *  Hash each host into each indexs for each worker instances 
	 * @return
	 */
	public Hashtable<Integer, HashSet<String>> hashHost(SHA1HashFunction hashFunc, ArrayList<String>hosts)
	{
		Hashtable<Integer, HashSet<String>>hash_host = new Hashtable<Integer, HashSet<String>>();
		for(String url: hosts)
		{
			String host = NormalizeURL.getDomainName(url);
			int idx = hashFunc.getHashIndex(host);

			HashSet<String>hs = hash_host.get(idx);
			if(hs == null)
			{
				hs = new HashSet<String>();
				hs.add(url);
				hash_host.put(idx, hs);
			}
			else
			{
				hs.add(url);
			}
		}
		System.out.println("hash_host: " + hash_host);
		return hash_host;
	}

	public void print(String info)
	{
		System.out.println(info);
	}
}

