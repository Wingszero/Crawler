package com.myapp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.junit.Test;

import junit.framework.TestCase;

public class testLog extends TestCase 
{
	@Test
	public void test1() throws URISyntaxException, IOException 
	{
		String filename = "./logs/log_gathered";

		int pages = 0;
		int dbPages = 0;
		int robotsNum = 0;
		int urlFilterNum = 0;
		int hostSplitNum = 0;
		int contentSeen = 0;
		int totalMB = 0;
		int hotHits = 0; 
		int bigPage = 0;
		HashMap<String, Integer>statusCode = new HashMap<String, Integer>();

		//ArrayList<Integer>pageNumList = new ArrayList<Integer>();

		File file = new File(filename);
		FileInputStream fis;
		try 
		{
			fis = new FileInputStream(file);
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
			return;
		}

		String line;
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		try 
		{
			while ((line = br.readLine()) != null) 
			{
				if(line.startsWith("Pages"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					pages += Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("webpage db"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					dbPages += Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("robots cache"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					robotsNum += Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("URLFilter"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					urlFilterNum += Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("HostSplitter"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					hostSplitNum = Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("Content-Seen"))
				{
					String[] pair = line.split(":");
					String pageStr = pair[1].trim();
					contentSeen += Integer.parseInt(pageStr); 
				}
				else if(line.startsWith("Total"))
				{
					String[] pair = line.split(":");
					String sizeStr = pair[1].trim();
					int index = sizeStr.indexOf(".");
					totalMB += (Integer.parseInt(sizeStr.substring(0, index)));
				}
				else if(line.startsWith("Max"))
				{
					//Max:100557.18 KB
					String[] pair = line.split(":");
					String sizeStr = pair[1].trim();
					int index = sizeStr.indexOf(".");
					bigPage = Math.max(bigPage, Integer.parseInt(sizeStr.substring(0, index)));
					//System.out.println("big:" + bigPage);
				}
				else if(line.contains("HotPages hits"))
				{
					int index = line.indexOf("HotPages hits");
					String numStr = line.substring(index + "HotPages hits".length(), line.length());
					hotHits += Integer.parseInt(numStr);
				}
				else if(line.startsWith("-1") 
						|| line.startsWith("2")
						|| line.startsWith("3")
						|| line.startsWith("4")
						|| line.startsWith("5")
						|| line.startsWith("9")
						)
				{
					calcStausCode(line, statusCode);
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		int totalRequstPages = 0;
		for(Integer values: statusCode.values())
		{
			totalRequstPages += values; 
		}
		print("Status code: ");
		for(String code: statusCode.keySet())
		{
			int num = statusCode.get(code);
			float factor = (num * 100F)/(totalRequstPages * 1F);
			//if(factor >= 1)
			//{
				print(code + " " + statusCode.get(code) + " " +  factor + "%");
			//}
		}

		print("Total request Pages: " + totalRequstPages);
		print("Fetched Pages: " + pages);
		print("Total fetch MB: " + totalMB); 
		print("Max size/page KB: " + bigPage); 
		print("Average size/page KB: " + (totalMB * 1000F) / pages); 
		print("Hot hits: " +  hotHits); 
		print("Stored Pages: " + dbPages);
		print("Robots: " + robotsNum);
		print("HostSplitter: " + hostSplitNum);
		print("Content seen: " + contentSeen);
		print("Url filter: " + urlFilterNum);	
	}

	private void calcStausCode(String line, HashMap<String, Integer>statusCode) 
	{
		String[] pair = line.split(" ");
		String code = pair[0].trim();
		String numStr = pair[1].trim();
		int num = Integer.parseInt(numStr); 
		if(!statusCode.containsKey(code))
		{
			statusCode.put(code, num);
		}
		else
		{
			statusCode.put(code, statusCode.get(code) + num);
		}
	}

	private static void print(String s)
	{
		System.out.println(s);
	}

}
