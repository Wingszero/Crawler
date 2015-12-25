package com.myapp.worker;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Hot query keywords for crawling priority 
 * @author Haoyun 
 *
 */
public class AnchorPool 
{
	private static Set<String> anchors = new HashSet<String>();

	static 
	{
		//sports
		anchors.add("sports");
		anchors.add("basketball");
		anchors.add("nba");
		anchors.add("soccer");
		anchors.add("football");
		anchors.add("ball");
		anchors.add("league");

		//entertainment
		anchors.add("game");
		anchors.add("entertainment");
		anchors.add("song");
		anchors.add("music");
		anchors.add("television");
		anchors.add("video");
		anchors.add("movie");
		anchors.add("film");

		//arts
		anchors.add("art");
		anchors.add("photo");
		anchors.add("draw");
		anchors.add("design");
		anchors.add("craft");	
	}	

	public static boolean isHot(String url, String title, String content)
	{
		String low_url = url.toLowerCase();
		for(String key: anchors)
		{
			if(low_url.contains(key)) 
			{
				return true;
			}
		}

		if(!title.isEmpty())
		{ 
			String low_title = title.toLowerCase();
			for(String key: anchors)
			{
				if(low_title.contains(key))
				{
					return true;
				}
			}
		}

		if(!content.isEmpty())
		{
			String low_content = content.toLowerCase();
			for(String key: anchors)
			{
				int cnt = StringUtils.countMatches(low_content, key);
				if(cnt > 10)
				{
					return true;
				}
			}
		}
		return false;
	}
}
