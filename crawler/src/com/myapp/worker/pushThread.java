package com.myapp.worker;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

/**
 *For worker node to push urls to other workers 
 * 
 * @author Haoyun 
 *
 */
public class pushThread implements Runnable 
{
	private String url_str;
	private String send_info;

	public pushThread(String url, String info)
	{
		url_str = url; 
		send_info = info;
	}

	@Override
	public void run() 
	{
		OutputStreamWriter osw;
		HttpURLConnection connection = HttpFetcher.getHttpURLConnection(url_str, 10000,  "POST", 0);
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


