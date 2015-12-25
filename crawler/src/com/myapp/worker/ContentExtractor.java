package com.myapp.worker;

import org.jsoup.nodes.Document;

/**
 * remove all the html tag and extract the actual text of the page.
 * can significantly reduce the storage and indexer workload
 * 
 * @author Haoyun 
 *
 */
public class ContentExtractor 
{
	/**
	 * 
	 * @param doc
	 * @param baseUrl
	 * @return String of content
	 */
	public static String process(Document doc) 
	{	
		doc.select("script").remove();
		doc.select("style").remove();
		doc.select(".hidden").remove();	
		return doc.text();
	}
}
