package com.lightark.Thread;

import java.util.concurrent.CopyOnWriteArraySet;

interface ManagedRunnable
{
	public void doWork() throws InterruptedException;
	public void preTerminateCleanup();
	public void finalCleanup();
	public void notifyStatusListeners();
	public void notifyCompleteListeners();
}

public abstract class ManagedThread implements Runnable, ManagedRunnable
{
	private String name = null;
	
	private Thread thread;
	private boolean threadIsActive;
	
	public boolean terminatedOnInterrupt = false;
	
	public CopyOnWriteArraySet<ThreadCompleteListener> completeListeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	public CopyOnWriteArraySet<ThreadStatusListener> statusListeners = new CopyOnWriteArraySet<ThreadStatusListener>();
	
	public ManagedThread()
	{
		
	}
	
	public void setThreadName(String newName)
	{
		name = newName;
	}
	
	@Override
	public void run()
	{
		terminatedOnInterrupt = false;
		try
		{
			doWork();
		}
		catch(InterruptedException e)
		{
			terminatedOnInterrupt = true;
		}
		finally
		{
			try
			{
				finalCleanup();
				notifyCompleteListeners();
			}
			finally
			{
				threadIsActive = false;
			}
		}
	}
	
	public boolean interrupt()
	{
		if(threadIsActive)
		{
			thread.interrupt();
			terminatedOnInterrupt = true;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void checkForInterrupt() throws InterruptedException
	{
		if(thread.isInterrupted())
		{
			terminatedOnInterrupt = true;
			preTerminateCleanup();
			throw new InterruptedException();
		}
	}
	
	public boolean isInterrupted()
	{
		return thread.isInterrupted();
	}
	
	public boolean startThread()
	{
		if(!threadIsActive)
		{
			threadIsActive = true;
			thread = new Thread(this);
			if(name != null)
			{
				thread.setName(name);
			}
			thread.start();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isActive()
	{
		return threadIsActive;
	}
	
	public String getThreadName()
	{
		return name;
	}
	
	public Thread getThread()
	{
		return thread;
	}

	
	public void addCompleteListener(ThreadCompleteListener listener)
	{
		completeListeners.add(listener);
	}

	public void removeCompleteListener(ThreadCompleteListener listener)
	{
		completeListeners.remove(listener);
	}

	public void addStatusListener(ThreadStatusListener listener)
	{
		statusListeners.add(listener);
	}

	public void removeStatusListener(ThreadStatusListener listener)
	{
		statusListeners.remove(listener);
	}
}