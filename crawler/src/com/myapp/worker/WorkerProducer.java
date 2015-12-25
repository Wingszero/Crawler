package com.myapp.worker;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;

import com.myapp.worker.utils.Common;

public class WorkerProducer 
{
	static int numThreads = 150; 
	ArrayList<String>hosts;
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		int numThreads = 150; 
		
		//debug
		//numThreads = 2;

		String path = "./src/seeds";
		
		//debug
		//path = "./src/seeds2";

		int myIdx = 0;

		int totalWorkers = 1;
	
		//debug
		//totalWorkers = 10;

		int minutes = 120;

		Worker c = new Worker(numThreads, Common.localReadHost(path),  totalWorkers, myIdx, minutes);
		c.run();
	}
}
