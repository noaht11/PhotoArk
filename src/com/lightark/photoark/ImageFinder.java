package com.lightark.photoark;

import java.io.File;
import java.util.ArrayList;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JLabel;

import com.lightark.Thread.ManagedThread;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.photoark.imagesearch.Search;

public class ImageFinder extends ManagedThread
{
	//private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	
	public ArrayList<File> imageFiles = new ArrayList<File>();
	public ArrayList<File> filteredFiles = new ArrayList<File>();
	private Search search;
	
	//public boolean wasInterrupted = false;
	
	public boolean filter = false;
	
	private JLabel updateOutput = null;
	private JLabel countOutput = null;

	public ImageFinder(Search search)
	{
		this.search = search;
	}
	
	/*public void addListener(final ThreadCompleteListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(final ThreadCompleteListener listener)
	{
		listeners.remove(listener);
	}*/
	
	public void setUpdateOutput(JLabel output)
	{
		this.updateOutput = output;
	}
	
	public void setCountOutput(JLabel output)
	{
		this.countOutput = output;
	}

	@Override
	public void notifyCompleteListeners()
	{
		for (ThreadCompleteListener listener : completeListeners)
		{
			listener.notifyOfThreadComplete(this);
		}
	}
	
	@Override
	public void notifyStatusListeners()
	{
		
	}

	@Override
	public void doWork() throws InterruptedException
	{
		/*this.wasInterrupted = false;
		try
		{
			scan();
		}
		finally
		{
			notifyListeners();
		}*/
		scan();
	}

	public void scan() throws InterruptedException
	{
		imageFiles.clear();
		filteredFiles.clear();
		if(search.includeSubDirs)
		{
			scanFileSystem(search.location);
		}
		else
		{
			File[] files = search.location.listFiles();
			for(File f : files)
			{
				/*if(Thread.currentThread().isInterrupted())
				{
					this.wasInterrupted = true;
					return;
				}*/
				checkForInterrupt();
				evaluateFile(f);
			}
		}
	}
	
	private void scanFileSystem(File start) throws InterruptedException
	{
		/*if(Thread.currentThread().isInterrupted())
		{
			if(updateOutput != null)
			{
				updateOutput.setText("Cancelling Search...");
			}
			this.wasInterrupted = true;
			return;
		}*/
		checkForInterrupt();
		if(updateOutput != null)
		{
			String path = start.getAbsolutePath();
			int pathLimit = 25;
			if(path.length() > pathLimit)
			{
				path = ("..." + path.substring(path.lastIndexOf("\\"), path.length()));
			}
			updateOutput.setText("<html><body>Searching in: <br>" + path + "</body></html>");
		}
		File[] list = start.listFiles();
		if(list != null)
		{
			for(File f : list)
			{
				if(f.isDirectory())
				{
					scanFileSystem(f);
				}
				else
				{
					evaluateFile(f);
				}
			}
		}
	}
	
	private void evaluateFile(File f)
	{
		String name = f.getName().toLowerCase();
		for(String ext : PhotoArk.imageExtensions)
		{
			if(name.endsWith(ext))
			{
				imageFiles.add(f);
				if(filter)
				{
					if(search.evaluateFile(f))
					{
						filteredFiles.add(f);
						if(countOutput != null)
						{
							countOutput.setText("Files (" + filteredFiles.size() + ")");
						}
					}
				}
				else
				{
					if(countOutput != null)
					{
						countOutput.setText("Files (" + filteredFiles.size() + ")");
					}
				}
			}
		}
	}

	@Override
	public void preTerminateCleanup()
	{
		if(updateOutput != null)
		{
			updateOutput.setText("Cancelling Search...");
		}
	}
	
	@Override
	public void finalCleanup()
	{
		
	}
	
}