package com.myapp.worker.utils.Robots;

import com.myapp.worker.utils.Const;

public class RobotsParser 
{

	/**
	 * parse robots content  into RobotsTxtInfo 
	 * @return RobotsTxtInfo 
	 */
	public static RobotsTxtInfo parse(String content) 
	{
		RobotsTxtInfo robot = new RobotsTxtInfo();
		//empty
		if(content == null || content.isEmpty())
		{
			return robot;
		}
		String[] lines = content.split("\n");
		String cur_agent = null;

		boolean checkMyAgent = false;

		for(String line: lines)
		{
			//skip comment and empty
			if(line.startsWith("#")) continue; 
		
			//use lowercase comparison
			line = line.trim().toLowerCase();

			if(line.isEmpty()) continue;
			
			if(line.startsWith(Const.STR_USER_AGENT))
			{
				String[] tokens = line.split(":");
				if(tokens.length>1)
				{
					String agent = tokens[1].trim();
					if(agent.length() > 0)
					{
						if(agent.equalsIgnoreCase(Const.MY_USER_AGENT)) 
						{
							checkMyAgent = true;
							//skip all other user agent 
							robot = new RobotsTxtInfo();
							robot.addUserAgent(agent);
							cur_agent = agent;
						}
						else if(checkMyAgent)
						{
							break;
						}
						else if(agent.equalsIgnoreCase(Const.ALL_USER_AGENT))
						{
							robot.addUserAgent(agent);
							cur_agent = agent;
						}
					}
				}
			}
			else if(cur_agent == null) continue;

			/*Disallow list*/
			else if(line.startsWith(Const.STR_DISALLOW)) 
			{
				String[] tokens = line.split(":");
				if(tokens.length>1)
				{
					String disallow = tokens[1].trim();			
					robot.addDisallowedLink(cur_agent, disallow);
				}
			}
			/*Allow list*/
			else if(line.startsWith(Const.STR_ALLOW)) 
			{
				String[] tokens = line.split(":");
				if(tokens.length>1)
				{
					String allow = tokens[1].trim();			
					robot.addAllowedLink(cur_agent, allow);
				}
			}
			else if(line.startsWith(Const.STR_CRAWL_DELAY))
			{
				String[] tokens = line.split(":");
				if(tokens.length>1)
				{
					String delay = tokens[1].trim();			
					Integer delayNum;
					try
					{
						delayNum = Integer.parseInt(delay);
						robot.addCrawlDelay(cur_agent, delayNum);
					}
					catch(NumberFormatException e)
					{
						try
						{
							delayNum = (int) Float.parseFloat(delay) + 1;
							robot.addCrawlDelay(cur_agent, delayNum);
						}
						catch(NumberFormatException e2)
						{
							robot.addCrawlDelay(cur_agent, Const.ROBOTS_DEFAULT_DELAY);
						}
					}
				}
			}
		}
		return robot;
	}	
}
