package com.myapp.worker.db;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * BDB accessor to the URLQueueEntity
 * @author Haoyun 
 *
 */
public class URLQueueAccessor
{
	private PrimaryIndex<Integer, URLQueueEntity> queues;

	public URLQueueAccessor(EntityStore store)
	{
		//key: url
		queues = store.getPrimaryIndex(Integer.class, URLQueueEntity.class);
	}

	public URLQueueEntity getUrlQueue(Integer type) 
	{
		URLQueueEntity queue = getEntity(type);
		if(queue == null)
		{
			queue = new URLQueueEntity(type);
			queues.put(queue);
		}
		return queue;
	}

	public PrimaryIndex<Integer, URLQueueEntity> getPrimaryIndex() 
	{
		return queues;
	}

	public PrimaryIndex<Integer, URLQueueEntity> getAllPages() 
	{
		return queues;
	}

	public List<URLQueueEntity> getAllEntities()
	{
		List<URLQueueEntity> pageList = new ArrayList<URLQueueEntity>();
		EntityCursor<URLQueueEntity> cursors = queues.entities();

		try
		{
			Iterator<URLQueueEntity> iter = cursors.iterator();
			while(iter.hasNext())
			{
				pageList.add(iter.next());
			}
		}
		catch(DatabaseException dbe) 
		{
			dbe.printStackTrace();
		}
		finally
		{
			cursors.close();
		}
		return pageList;
	}

	public URLQueueEntity getEntity(Integer type)
	{
		URLQueueEntity queue = queues.get(type);
		return queue;
	}

	public void putEntity(URLQueueEntity queue)
	{
		queues.put(queue);
	}

	public long size() 
	{
		return queues.count(); 
	}

	public void addNewUrl(String url, Integer type) 
	{
		URLQueueEntity queue = getUrlQueue(type);
		queue.put(url);
		putEntity(queue);
	}

	public void put(String url, Integer type) 
	{
		addNewUrl(url, type);
	}

	public boolean isEmpty(int type) 
	{
		URLQueueEntity queue = getUrlQueue(type);
		if(queue == null)
		{
			return true;
		}
		return queue.isEmpty();
	}

	public String poll(Integer type) 
	{
		URLQueueEntity queue = getUrlQueue(type);
		if(queue != null)
		{
			return queue.poll(); 
		}
		return null;
	}
}
