package com.myapp.worker;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * A priority queue to hold active host and its next crawl time
 * earliest host will be pop
 * 
 * maintain politeness
 * 
 * @author Haoyun 
 *
 */
public class HostTimeHeap 
{
	private PriorityBlockingQueue<HostTime> hostHeap; 
		
	public HostTimeHeap(int size) 
	{
		hostHeap = new PriorityBlockingQueue<HostTime>(size, cmp);
	}

	public class HostTime
	{
		String host;
		Long next_time;
		public HostTime(String h, long t)
		{
			host = h;
			next_time = t;
		}
	}

	Comparator<HostTime> cmp = new Comparator<HostTime>()
	{
		public int compare(HostTime x, HostTime y)
		{
			return (int) (x.next_time - y.next_time); 
		}
	};

	public void add(HostTime ht) 
	{
		hostHeap.add(ht);
	}
	
	public HostTime peek()
	{
		return hostHeap.peek();
	}

	public HostTime poll() 
	{
		HostTime ht = hostHeap.poll();
		return ht;
	}

	public void add(String h, long cur) 
	{
		hostHeap.add(new HostTime(h, cur));
	}
	
	public int size()
	{
		return hostHeap.size();
	}
}
