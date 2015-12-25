package com.myapp.worker.db;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 * Url DB entity:
 * url addr: name
 * last modified: to check if a url need to be re-downloaded 
 */
@Entity
public class WebPageEntity
{
	@PrimaryKey
	private String url = "";
	private String title = "";
	private String content = "";
	private ArrayList<String>links;
	private long last_modified; 

	public WebPageEntity()
	{
		
	}

	public WebPageEntity(String addr, String title, String content, ArrayList<String>links, long date)
	{
		this.url = addr;
		this.title = title;
		this.content = content;
		this.last_modified = date;
		this.links = links;
	}
	
	/*get funcs*/
	public String getUrl()
	{
		return url;
	}

	public String getName()
	{
		return url;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getContent()
	{
		return content;
	}

	public long getLastModifiedTime()
	{
		return last_modified;
	}
	
	public ArrayList<String >getLinks()
	{
		return links;
	}

	/*set funcs*/
	public void setName(String data)
	{
		url = data;
	}
		
	public void setContent(String data)
	{
		content = data;
	}

	public void setCrawlTime(long data)
	{
		last_modified = data;
	}	
}
