package com.lightark.Thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadManager
{
	private Map<Object, ArrayList<ManagedThread>> threads = new HashMap<Object, ArrayList<ManagedThread>>();
	
	public ThreadManager()
	{
		
	}
	
	public void registerThread(Object key, ManagedThread thread)
	{
		if(threads.containsKey(key))
		{
			threads.get(key).add(thread);
		}
		else
		{
			ArrayList<ManagedThread> list = new ArrayList<ManagedThread>();
			list.add(thread);
			threads.put(key, list);
		}
	}
	
	public boolean terminateThreadsForKey(Object key)
	{
		if(threads.containsKey(key))
		{
			for(ManagedThread t : threads.get(key))
			{
				if(t.isActive())
				{
					if(t.interrupt())
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean terminateAllThreads()
	{
		for(ArrayList<ManagedThread> threadList : threads.values())
		{
			for(ManagedThread t : threadList)
			{
				if(t.isActive())
				{
					if(t.interrupt())
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		return true;
	}
}