package com.myapp.worker.db;


import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * BDB accessor to the WebPageEntity
 * @author Haoyun 
 *
 */
public class WebPageAccessor
{
	private PrimaryIndex<String, WebPageEntity> all_pages;

	public WebPageAccessor(EntityStore store)
	{
		//key: url
		all_pages = store.getPrimaryIndex(String.class, WebPageEntity.class);
	}

	public WebPageEntity createUrlEntity(String url, String title, String content, ArrayList<String>links, long time)
	{
		WebPageEntity page = getEntity(url); 
		if(page == null)
		{
			page = new WebPageEntity(url, title, content, links, time);
		}
		else
		{
			page.setCrawlTime(time);
		}
		return page;
	}

	public WebPageEntity add(String url, String title, String content, ArrayList<String>links)
	{
		long time = (new Date()).getTime();
		WebPageEntity page = createUrlEntity(url, title, content, links, time);
		putEntity(page);
		return page;
	}

	public boolean del(String url)
	{
		return all_pages.delete(url);
	}

	public boolean contains(String url)
	{
		return all_pages.contains(url);
	}

	public boolean needUpdate(String url, long date)
	{
		WebPageEntity page = all_pages.get(url);
		/*not downloaded or old*/
		return page == null || page.getLastModifiedTime() < date;
	}

	public PrimaryIndex<String, WebPageEntity> getPrimaryIndex() 
	{
		return all_pages;
	}

	public PrimaryIndex<String, WebPageEntity> getAllPages() 
	{
		return all_pages;
	}

	public List<WebPageEntity> getAllEntities()
	{
		List<WebPageEntity> pageList = new ArrayList<WebPageEntity>();
		EntityCursor<WebPageEntity> cursors = all_pages.entities();

		try
		{
			Iterator<WebPageEntity> iter = cursors.iterator();
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


	public String getContent(String url)
	{
		WebPageEntity page = getEntity(url);
		if(page != null)
		{
			return page.getContent();
		}
		return null;
	}

	public WebPageEntity getEntity(String url)
	{
		WebPageEntity page = all_pages.get(url);
		return page;
	}

	public void putEntity(WebPageEntity page)
	{
		all_pages.put(page);
	}

	public long size() 
	{
		return all_pages.count(); 
	}
}
