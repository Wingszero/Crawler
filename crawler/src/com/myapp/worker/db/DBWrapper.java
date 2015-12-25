package com.myapp.worker.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.persist.StoreConfig;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class DBWrapper 
{
	private Environment env;
	private File env_home;
	private EntityStore store;

	private WebPageAccessor webpageEA;
	private RobotsTxtAccessor robotsEA;
	static final Logger logger = Logger.getLogger(DBWrapper.class);	
	
	public DBWrapper() 
	{
		//init log4j
    	BasicConfigurator.configure();
		setup();
	}

	public void setup()
	{
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setCachePercent(20);

		env_home = createDir(DBConst.ROOT);
		try
		{
			env = new Environment(env_home, envConfig);
		}
		catch(EnvironmentFailureException e)
		{
			logger.error(e.getCause());
			return;
		}

		/*store config*/
		StoreConfig sc = new StoreConfig();
		sc.setAllowCreate(true);
		sc.setReadOnly(false);
		sc.setDeferredWrite(true);
		store = new EntityStore(env, DBConst.DB_STORE_NAME, sc);
		webpageEA = new WebPageAccessor(store);
		robotsEA = new RobotsTxtAccessor(store);
	}

	public void sync()
	{
		if(store != null)
		{
			try
			{
				store.sync();
			}
			catch(DatabaseException e)
			{
				print("db sync DatabaseException!");
			}
		}
	}

	// Return a handle to the environment
	public Environment getEnv() 
	{
		return env;
	}

	// Close the store and environment.
	public void close() 
	{		
		if (store != null) 
		{
			try 
			{
				store.close();
				//print(name + " store close");
			} 
			catch(DatabaseException dbe) 
			{
				System.err.println("Error closing store: " + dbe.toString());
			}
		}
		
		if (env != null) 
		{
			try 
			{
				env.close();
			} 
			catch(DatabaseException dbe) 
			{
				System.err.println("Error closing MyDbEnv: " + dbe.toString());
			}
		}	
	}	

	private static File createDir(String dir_name)
	{
		File dir = new File(dir_name);
		if (!dir.exists()) 
		{
			logger.info("creating directory: " + dir_name);
			boolean res = false;
			try
			{
				dir.mkdir();
				res = true;
			} 
			catch(SecurityException se)
			{
				logger.error("Create dir " + dir_name + " failed");
			}        
			if(res) 
			{    
				logger.info("Create dir " + dir_name + " successed");
			}
		}
		return dir;
	}

	@SuppressWarnings("unused")
	private static File createFile(String root, String name) throws IOException
	{
		print("DBWrapper createFile: root: " + root + ", file: " + name);
		File file = new File(root, name);
		if(!file.exists())
		{
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}
	
	public void storeWebPage(String url, String title, String content, ArrayList<String>links) 
	{
		webpageEA.add(url, title, content, links);
	}
	
	public String getWebPageContent(String url) 
	{
		return webpageEA.getContent(url);
	}
	
	public WebPageEntity getWebPageEntity(String url)
	{
		return webpageEA.getEntity(url);
	}

	public static void print(String s)
	{
		System.out.println(s);
	}

	public boolean needUpdateUrl(String url, long time) 
	{
		if(!webpageEA.contains(url))
		{
			return true;
		}
		return webpageEA.needUpdate(url, time) ;
	}

	public long getWebPageNum() 
	{
		return webpageEA.size();
	}

	public void storeRobotsTxt(String url, String content) 
	{
		robotsEA.put(url, content);
	}
	
	public String getRobotsTxt(String url) 
	{
		return robotsEA.getContent(url);
	}

	public long getRobotsTxtNum() 
	{
		return robotsEA.size();
	}

	public PrimaryIndex<String, RobotsTxtEntity> getRobots() 
	{
		return robotsEA.getAllEntity();
	}

	public WebPageEntity getWebPage(String url_str) 
	{
		return webpageEA.getEntity(url_str);
	}
	
	public PrimaryIndex<String, WebPageEntity>  getAllPages() 
	{
		return webpageEA.getAllPages();
	}
	
	public List<WebPageEntity> getAllPageEntities() 
	{
		return webpageEA.getAllEntities();
	}
}

