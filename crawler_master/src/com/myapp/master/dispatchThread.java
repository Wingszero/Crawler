package com.myapp.master;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

/**
 * For master node to dispatch job to worker node. 
 * send a HTTP POST to worker node 
 * @author Haoyun 
 *
 */
public class dispatchThread implements Runnable 
{
	private String url_str;
	private String send_info;

	public dispatchThread(String url, String info)
	{
		url_str = url; 
		send_info = info;
	}

	@Override
	public void run() 
	{	
		OutputStreamWriter osw;
		HttpURLConnection connection = Common.getHttpURLConnection(url_str, 10000,  "POST");
		try 
		{
			osw = new OutputStreamWriter(connection.getOutputStream());
			osw.write(send_info);
			osw.close();
			connection.getResponseCode();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
}


