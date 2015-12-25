package com.myapp.test;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.myapp.worker.db.DBWrapper;
import com.myapp.worker.db.WebPageEntity;
import junit.framework.TestCase;

public class testDB extends TestCase 
{
	@Test
	public void test() 
	{
		DBWrapper mydb = new DBWrapper();
		
		List<WebPageEntity>all_pages = mydb.getAllPageEntities();
	
		System.out.println("size: " + all_pages.size());
	
		for(WebPageEntity page: all_pages) 
		{
			String url = page.getUrl(); 
			String title = page.getTitle();
			String content = page.getContent();
			ArrayList<String>links = page.getLinks(); 	
			System.out.println(url);
			System.out.println(title);
			System.out.println(content);
			System.out.println("links: " + links.size());
		}	
	}
}
