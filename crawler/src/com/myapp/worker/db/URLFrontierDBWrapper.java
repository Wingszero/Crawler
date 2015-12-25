package com.myapp.worker.db;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class URLFrontierDBWrapper 
{
	private Environment env;
	private File env_home;
	private EntityStore store;

	private URLQueueAccessor urlQueueEA;
	static final Logger logger = Logger.getLogger(URLFrontierDBWrapper.class);	
	
	public URLFrontierDBWrapper() 
	{
    	BasicConfigurator.configure();
		setup();
	}

	public void setup()
	{
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setCachePercent(20);

		env_home = createDir(DBConst.URLFRONTIER_ROOT);
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
		store = new EntityStore(env, DBConst.URLFRONTIER_DB_STORE, sc);
		urlQueueEA = new URLQueueAccessor(store);
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
	
	public void addNewUrl(String url,  Integer type) 
	{
		urlQueueEA.addNewUrl(url, type); 
	}
	
	public URLQueueAccessor getUrlQueueAccessor() 
	{
		return urlQueueEA;
	}
	
	public static void print(String s)
	{
		System.out.println(s);
	}
	
	public List<URLQueueEntity> getAllEntities() 
	{
		return urlQueueEA.getAllEntities();
	}

	public URLQueueEntity getUrlQueue(Integer type) 
	{
		return urlQueueEA.getUrlQueue(type);
	}

	public void updateQueue(URLQueueEntity front_queue) 
	{
		urlQueueEA.putEntity(front_queue);
	}
}

