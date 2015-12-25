package com.myapp.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
	  try 
	  {
		  Class[]  testClasses = {
				  Class.forName("server.test.TestDB"),
		  };   
		  return new TestSuite(testClasses);
	  } 
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  } 
	  return null;
  }
}
