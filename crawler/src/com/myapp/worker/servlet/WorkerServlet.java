package com.myapp.worker.servlet;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import javax.servlet.*;
import javax.servlet.http.*;
import com.myapp.worker.HttpFetcher;
import com.myapp.worker.Worker;
import com.myapp.worker.utils.Common;
import com.myapp.worker.utils.Const;


public class WorkerServlet extends HttpServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/*=====Parameters from web.xml=========*/

	private String port;
	private String master_addr;
	private String WorkerStatusUrl;

	/*=====Parameters from web.xml=========*/


	/*=====Parameters from master=========*/

	private int numWorkers; 
	private int numThreads; 
	//private int numHosts; 
	private int runMinutes; 
	private int myWorkerIdx; 

	//all hosts 
	private ArrayList<String>hosts;

	/*=====Parameters from master=========*/

	private Worker crawler;

	public WorkerServlet() throws ServletException
	{
		init();
	}

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		port = config.getInitParameter("port"); 
		if(isEmpty(port))
		{
			print("worker init failed, the port init parameter is null");
			System.exit(-1);
		}

		master_addr = config.getInitParameter("master"); 
		if(isEmpty(master_addr))
		{
			print("worker init failed, the master init parameter is null");
			System.exit(-1);
		}

		(new UpStatusThread()).start();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
	{
		response.setContentType("text/html");
		try 
		{
			PrintWriter out = response.getWriter();
			String url = request.getServletPath();
			if(url.equals(Const.STATUS_URL))
			{
				out.println("<html><head><title>Worker</title></head>");
				out.println("<body>Hi, I am the worker!</body></html>");
				out.println("===========\n");
				if(crawler != null)
				{
					out.println(crawler.getStatusString() + "\n");
					out.println("<P>" + "\n" + "</P>");
	
					out.println(crawler.getTotalPageNumString() + "\n");
					out.println("<P>" + "\n" + "</P>");

					out.println("robots size:" +  crawler.getRobotCache().size() + "\n"); 
					out.println("<P>" + "\n" + "</P>");

					out.println(crawler.getSpeedString() + "\n");
					out.println("<P>" + "\n" + "</P>");

					out.println(crawler.getTotalPageSizeString() + "\n");
					out.println("<P>" + "\n" + "</P>");

					out.println(crawler.getAvgPagesString() + "\n");
					out.println("<P>" + "\n" + "</P>");

					out.println(crawler.getMinPageSizeString() + "\n");
					out.println("<P>" + "\n" + "</P>");

					out.println(crawler.getMaxPageSizeString() + "\n");
					out.println("<P>" + "\n" + "</P>");
				}
				else
				{
					out.println("Create a crawler here.");
					out.println("<a href=\"/test_crawler\" class=\"button\">Run a crawler</a>");
				}
			}

			else if(url.equals(Const.TEST_CRAWLER))
			{
				int numThreads = 200; 
				String path = "./seeds";
				ArrayList<String>hosts = Common.servletReadHosts(path, this.getServletContext());
				crawler = new Worker(numThreads, hosts,  1, 0, 60);
				crawler.run();
				/*
				out.println("<form action=\"" + Const.TEST_POST_URL +"\"method=\"post\">");
				out.println("<input type=\"submit\" />");
				out.println("</form>");
				*/
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
	{
		String url = request.getServletPath();
		if(url.equals(Const.RUN_CRAWL_URL))
		{
			try 
			{
				runCrawl(request, response);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
		}
		else if(url.equals(Const.PUSH_URL_BATCH)) 
		{
			if(crawler == null) return;
			parsePushUrlBatch(request);
		}
	}
	
	/**
	 * 
	 * @param url_str
	 * @throws UnsupportedEncodingException
	 */
	public void parsePushUrlBatch(HttpServletRequest request) 
	{
		String content;
		try 
		{
			content = extractPostRequestBody(request);
			content = URLDecoder.decode(content, "UTF-8");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return;
		}
		
		String[] urls  = content.split(";");
		
		
		crawler.processPushedUrl(urls);
	}

	/**
	 * @param url_str
	 * @throws UnsupportedEncodingException 
	 */
	public void sendStatus(String url_str) throws UnsupportedEncodingException
	{
		String charset = "UTF-8";
		String query = ""; 
		if(crawler != null)
		{
			query += String.format(Const.STATUS_STR + "=%s&", 
					URLEncoder.encode(crawler.getStatusString(), charset));

			query += String.format(Const.TOTAL_PAGE_NUM_STR + "=%s&", 
					URLEncoder.encode(crawler.getTotalPageNumString(), charset));

			query += String.format(Const.TOTAL_HOST_NUM_STR + "=%s&", 
					URLEncoder.encode(crawler.getUrlFrontierString(), charset));

			query += String.format(Const.DUE_NUM + "=%s&", 
					URLEncoder.encode(crawler.getDUEString(), charset));

			query += String.format(Const.CONTENT_SEEN_NUM + "=%s&", 
					URLEncoder.encode(crawler.getContentSeenString(), charset));

			query += String.format(Const.SPEED_STR+ "=%s&", 
					URLEncoder.encode(crawler.getSpeedString(), charset));

			query += String.format(Const.SPEED_STR+ "=%s&", 
					URLEncoder.encode(crawler.getSpeedString(), charset));

			query += String.format(Const.TOTAL_PAGE_SIZE_STR + "=%s&", 
					URLEncoder.encode(crawler.getTotalPageSizeString(), charset));

			query += String.format(Const.MIN_PAGE_SIZE_STR + "=%s&", 
					URLEncoder.encode(crawler.getMinPageSizeString(), charset));

			query += String.format(Const.MAX_PAGE_SIZE_STR + "=%s&", 
					URLEncoder.encode(crawler.getMaxPageSizeString(), charset));

			query += String.format(Const.AVG_PAGE_SIZE + "=%s", 
					URLEncoder.encode(crawler.getAvgPagesString(), charset));
		}
		else
		{
			query = String.format(Const.STATUS_STR + "=%s", 
					URLEncoder.encode(Const.IDLE_STATUS, charset));
		}

		url_str = url_str + "?" + query; 

		//print("send status: " + url_str);

		HttpFetcher.getHttpURLConnection(url_str, Const.CONNECTION_TIMEOUT, Const.GET, 0);
	}

	/**
	 * send GET to master to update status * every 10 secs 
	 */
	public class UpStatusThread extends Thread  
	{
		public void run() 
		{
			WorkerStatusUrl = Const.HTTP_PREFIX + master_addr + Const.UP_STATUS_URL; 
			print("Thread run: master url: " + WorkerStatusUrl);
			while(true)
			{
				try 
				{
					sendStatus(WorkerStatusUrl);
				} 
				catch (UnsupportedEncodingException e1) 
				{
					e1.printStackTrace();
				}
				try 
				{
					sleep(Const.STATUS_UP_TIME_INTERVAL);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * append a string to a file
	 * @throws IOException 
	 */
	public synchronized void writeFile(String filename, String content, boolean append) 
	{
		print("write File: filename: " + filename);
		File file = new File(filename);
		if(!file.exists())
		{
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(filename, append);
				PrintWriter output = new PrintWriter(new BufferedWriter(fw));
				output.println(content);
				output.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

	}

	public String extractPostRequestBody(HttpServletRequest request) throws IOException 
	{
		if ("POST".equalsIgnoreCase(request.getMethod())) 
		{
			@SuppressWarnings("resource")
			Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
		return "";
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void runCrawl(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String content = extractPostRequestBody(request);
		if(!parseRunContent(content))
		{
			return;
		}

		long start = System.currentTimeMillis();

		print("==============runCrawl info:");
		print("numThreads: " + numThreads);
		print("numWorkers info:" + numWorkers);
		print("myWorkerIdx info:" + myWorkerIdx);
		print("runMinutes info:" + runMinutes);
		print("==============runCrawl info end:");

		crawler = new Worker(numThreads, hosts,  numWorkers, myWorkerIdx, runMinutes);
		crawler.run();

		float used = (System.currentTimeMillis() - start)/1000F;
		print("runCrawl used time: " + used + " secs");

		endCrawl();
	}

	private void endCrawl() throws UnsupportedEncodingException 
	{
		print("end crawling!");
		sendStatus(WorkerStatusUrl);
	}

	/*
	 * parse the POST body like:  job="WordCount";numThreads=3;
	 */
	private boolean parseRunContent(String content) 
	{
		try 
		{
			content = URLDecoder.decode(content, "UTF-8");
		} 
		catch (UnsupportedEncodingException e1) 
		{
			e1.printStackTrace();
		}

		hosts = new ArrayList<String>();
		Hashtable<String, String>kvs = new Hashtable<String, String>();

		String[] pairs = content.split(";");
		for(String pair: pairs)
		{
			if(pair.isEmpty())continue;
			String[] tmp = pair.split("=");
			if(tmp.length < 2) continue;
			
			if(tmp[0].trim().equals("h"))
			{
				hosts.add(tmp[1].trim());
				//print("parsing content add host: " + tmp[1]);
			}
			else
			{
				kvs.put(tmp[0].trim(), tmp[1].trim());
			}
		}
		print("hosts size: " + hosts.size()); 

		try
		{
			numThreads = Integer.parseInt(kvs.get(Const.NUMTHREADS_STR));
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}

		/*init workers*/
		String nw = kvs.get(Const.NUMWORKERS_STR);
		try
		{
			numWorkers = Integer.parseInt(nw);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}

		/*init run minutes*/
		String runmin = kvs.get(Const.RUNMINS_STR);
		try
		{
			runMinutes = Integer.parseInt(runmin);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}

		/*init myidx*/
		String myidx = kvs.get(Const.MY_WORKER_INDEX);
		try
		{
			myWorkerIdx = Integer.parseInt(myidx);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}
		
		print("myWorkerIdx: " + myWorkerIdx);

		/*
		String file = String.valueOf(myidx);
		String path = "/txt/" + file + ".txt";
		print("jason=======================path: " + path);
		hosts = Common.servletReadHosts(path, this.getServletContext());

		System.out.println("readhosts host size: " + hosts.size());
		*/
		return true;
	}

	private boolean isEmpty(String s)
	{
		return s == null || s.trim().isEmpty();
	}

	@SuppressWarnings("unused")
	private boolean isEmpty(ArrayList<String>array)
	{
		return array == null || array.isEmpty();
	}

	public void print(String info)
	{
		System.out.println(info);
	}
}

