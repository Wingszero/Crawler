package com.myapp.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.myapp.worker.utils.Const;

public class seedsFetcher 
{
	public static void print(String s)
	{
		System.out.println(s);
	}

	public static HashSet<String> getHosts(String filename)
	{
		HashSet<String>hosts = new HashSet<String>();
		File file = new File(filename);
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
				String h = line.trim().toLowerCase();
				hosts.add(h);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return hosts;
	}

	public static void processCategory() throws IOException
	{
		HashSet<String>hosts = new HashSet<String>(); 
		File f = new File("./TOP_CATE");
		if(!f.exists())
		{
			f.createNewFile();
		}

		BufferedWriter output = new BufferedWriter(new FileWriter(f));

		String url_pre = "http://www.alexa.com/topsites/category;";
		String url_post = "/Top/";

		ArrayList<String>category = new ArrayList<String>();
		category.add("Sports");
		category.add("Sports/Football");
		category.add("Sports/Basketball");
		category.add("Sports/Soccer");
		category.add("News");
		category.add("Kids_and_Teens");
		category.add("Recreation");
		category.add("Games");
		category.add("Science");
		category.add("Computers/Computer_Science");
		category.add("Arts/Entertainment");
		category.add("Arts/Movies");
		category.add("Arts/Music");
		category.add("Arts/Design");
		category.add("Arts/Photography");

		int num = 0;
		int pages = 3;
		for(String cate: category)
		{
			print("cate: " + cate);
			for(int i = 0; i < pages; ++i)
			{
				String url_str = url_pre + String.valueOf(i) + url_post +  cate;
				Document doc = HttpFetcher.fetchFileDOM(url_str, Const.ALL_HTML_TYPES, Const.HTML_MAX_LENGTH); 
				Elements links = doc.select("a");
				for(Element  link: links)
				{
					String ref = link.attr("href"); 
					int idx  = ref.indexOf("siteinfo/"); 
					if(idx == -1)
					{
						continue;
					}
					ref = ref.substring(idx + 9);
					if(!hosts.contains(ref))
					{
						hosts.add(ref);
						output.write(ref);
						output.write("\n");
						++num;
					}
				}
			}
		}	
		print("Result: " + num);
		output.close();
	}

	public static void processUSTop() throws IOException
	{
		HashSet<String>hosts = new HashSet<String>(); 
		File f = new File("./US_TOP");
		if(!f.exists())
		{
			f.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(f));
	
		String url_pre = "http://www.alexa.com/topsites/countries;";
		String url_post = "/US/";
		int pages = 20;

		for(int i = 0; i < pages; ++i)
		{
			String url_str = url_pre + String.valueOf(i) + url_post;
			//print("URL: " + url_str); 
			Document doc = HttpFetcher.fetchFileDOM(url_str, Const.ALL_HTML_TYPES, Const.HTML_MAX_LENGTH); 
			Elements links = doc.select("a");
			//print("links: " + links.size());
			for(Element  link: links)
			{
				String ref = link.attr("href"); 
				int idx  = ref.indexOf("siteinfo/"); 
				if(idx == -1)
				{
					continue;
				}
				ref = ref.substring(idx + 9);
				if(!hosts.contains(ref))
				{
					hosts.add(ref);
					output.write(ref);
					output.write("\n");
				}
			}
		}	
		print("size: " + hosts.size());
		output.close();
	}

	public static void combine(String file1, String file2) throws IOException
	{
		HashSet<String>hosts = getHosts(file1);
		HashSet<String>hosts2 = getHosts(file2);
		hosts.addAll(hosts2);
		print("size: " + hosts.size());
		
		File f = new File("./TOP_COMBINE");
		if(!f.exists())
		{
			f.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(f));
		for(String h: hosts)
		{
			output.write(h);
			output.write("\n");
		}
		output.close();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		//processUSTop();
		//processCategory();
		combine("US_TOP_BACKUP", "TOP_LIST");
	}
}
