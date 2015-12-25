package com.myapp.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.servlet.ServletContext;

public class WebsitesLoader 
{
	/*
	private ArrayList<String>hosts; 
	public void init()
	{
		hosts = new ArrayList<String>();
	}
	*/
	public ArrayList<String> getHosts(String path, ServletContext application)
	{
		ArrayList<String>hosts = new ArrayList<String>();
		//System.out.println("application path:"); 

		File file = new File(application.getRealPath(path));
		FileInputStream fis;
		try 
		{
			fis = new FileInputStream(file);
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
			return null;
		}
		String line;
		//System.out.println("fis: " + fis);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		try 
		{
			while ((line = br.readLine()) != null) 
			{
				String h = NormalizeURL.getAbsoluteUrl(line);
				hosts.add(h);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return hosts;
	}
}
