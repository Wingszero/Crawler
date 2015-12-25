package com.myapp.worker.db;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 * robots.txt DB entity:
 * host addr: name
 * last modified: to check if a host need to be re-downloaded 
 */

@Entity
public class RobotsTxtEntity
{
	@PrimaryKey
	private String host = "";
	private String content = "";
	private long last_modified; 

	public RobotsTxtEntity()
	{
		
	}

	public RobotsTxtEntity(String addr, String content, long date)
	{
		this.host = addr;
		this.content = content;
		this.last_modified = date;
	}
	
	/*get funcs*/
	public String getName()
	{
		return host;
	}
	
	public String getContent()
	{
		return content;
	}

	public long getLastModifiedTime()
	{
		return last_modified;
	}

	/*set funcs*/
	public void setName(String data)
	{
		host = data;
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
