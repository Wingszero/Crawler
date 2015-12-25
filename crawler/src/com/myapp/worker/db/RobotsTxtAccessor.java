package com.myapp.worker.db;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import java.util.Date;



/**
 * Robots txt content indexer 
 * @author Haoyun 
 *
 */
public class RobotsTxtAccessor
{
	private PrimaryIndex<String, RobotsTxtEntity> robots;
	public RobotsTxtAccessor(EntityStore store)
	{
		robots = store.getPrimaryIndex(String.class, RobotsTxtEntity.class);
	}

	public RobotsTxtEntity createUrlEntity(String host, String content, long time)
	{
		RobotsTxtEntity robot = getEntity(host); 
		if(robot == null)
		{
			robot = new RobotsTxtEntity(host, content, time);
		}
		else
		{
			robot.setCrawlTime(time);
		}
		return robot;
	}

	public RobotsTxtEntity add(String host, String content)
	{
		long time = (new Date()).getTime();
		RobotsTxtEntity robot = createUrlEntity(host, content, time);
		putEntity(robot);
		return robot;
	}

	public boolean del(String host)
	{
		return robots.delete(host);
	}

	public boolean contains(String host)
	{
		return robots.contains(host);
	}
	
	public boolean needUpdate(String host, long date)
	{
		RobotsTxtEntity robot = robots.get(host);
		/*not downloaded or old*/
		return robot == null || robot.getLastModifiedTime() < date;
	}
	
	public PrimaryIndex<String, RobotsTxtEntity> getPrimaryIndex() 
	{
		return robots;
	}

	public String getContent(String host)
	{
		RobotsTxtEntity robot = getEntity(host);
		if(robot != null)
		{
			return robot.getContent();
		}
		return null;
	}
	
	public RobotsTxtEntity getEntity(String host)
	{
		RobotsTxtEntity robot = robots.get(host);
		return robot;
	}
	
	public void putEntity(RobotsTxtEntity robot)
	{
		robots.put(robot);
	}

	public long size() 
	{
		return robots.count(); 
	}

	public PrimaryIndex<String, RobotsTxtEntity> getAllEntity() 
	{
		return robots;
	}

	public void put(String host, String content) 
	{
		RobotsTxtEntity robot = getEntity(host);
		if(robot != null)
		{
			robot.setContent(content);
			putEntity(robot);
		}
		else
		{
			add(host, content);
		}
	}
}
