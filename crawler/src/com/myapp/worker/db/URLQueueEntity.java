package com.myapp.worker.db;

import java.util.ArrayList;

import com.myapp.worker.utils.Const;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 * UrlFrontier Queue 
 * url addr: name
 * last modified: to check if a url need to be re-downloaded 
 */
@Entity
public class URLQueueEntity
{
	@PrimaryKey
	private Integer type; 

	private ArrayList<String>urls;

	public URLQueueEntity()
	{
		
	}
	
	public URLQueueEntity(Integer type)
	{
		this.type = type;
		this.urls = new ArrayList<String>(Const.MAX_FRONTQUEUE_SIZE);
	}
	
	/*get funcs*/
	public ArrayList<String >getLinks()
	{
		return urls;
	}

	public void put(String url) 
	{
		urls.add(url);
	}	
	
	public String poll()
	{
		return urls.remove(0);
	}

	public boolean isEmpty() 
	{
		return urls.isEmpty();
	}

	public int size() 
	{
		return urls.size();
	}
}
