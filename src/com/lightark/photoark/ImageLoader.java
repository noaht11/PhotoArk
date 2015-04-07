package com.lightark.photoark;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.CopyOnWriteArraySet;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.lightark.Thread.ManagedThread;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.Thread.ThreadStatusListener;
import com.lightark.photoark.filelist.Thumbnail;
import com.lightark.photoark.imageviewer.ImagePanel;
import com.lightark.photoark.imageviewer.MetadataPanel;

public class ImageLoader extends ManagedThread
{
	public final static int FULL_IMAGE_MODE = 0;
	public final static int THUMBNAIL_MODE = 1;
	
	//private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	//private final Set<ThreadStatusListener> statusListeners = new CopyOnWriteArraySet<ThreadStatusListener>();
	
	private String[] fileNames;
	public CopyOnWriteArrayList<ImagePanel> images = new CopyOnWriteArrayList<ImagePanel>();
	public CopyOnWriteArrayList<MetadataPanel> metadata = new CopyOnWriteArrayList<MetadataPanel>();
	
	public CopyOnWriteArrayList<Thumbnail> thumbnails = new CopyOnWriteArrayList<Thumbnail>();
	
	//public boolean wasInterrupted = false;
	
	private String currentFilePath = "";
	private int currentLoadCount = 0;
	
	private JPanel imgRootPanel;
	
	private int mode;
	
	private int thumbnailSize;
	
	public ImageLoader(String[] fileNames, JPanel imgRootPanel, int mode, int thumbnailSize)
	{
		this.fileNames = fileNames;
		this.imgRootPanel = imgRootPanel;
		this.mode = mode;
		this.thumbnailSize = thumbnailSize;
	}
	
	public void reset()
	{
		images.clear();
		metadata.clear();
		thumbnails.clear();
		terminatedOnInterrupt = false;
		//wasInterrupted = false;
		fileNames = new String[]{};
	}
	
	public void setFileNames(String[] newFileNames)
	{		
		this.fileNames = newFileNames;
	}
	
	public int getThumbnailSize()
	{
		return thumbnailSize;
	}
	
	public void setThumbnailSize(int newSize)
	{
		thumbnailSize = newSize;
	}
	
	/*public void addListener(final ThreadCompleteListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(final ThreadCompleteListener listener)
	{
		listeners.remove(listener);
	}
	
	public void addStatusListener(final ThreadStatusListener listener)
	{
		statusListeners.add(listener);
	}

	public void removeStatusListener(final ThreadStatusListener listener)
	{
		statusListeners.remove(listener);
	}*/

	/*private void notifyListeners()
	{
		for (ThreadCompleteListener listener : listeners)
		{
			listener.notifyOfThreadComplete(this);
		}
	}
	
	private void notifyStatusListeners(String s, int count)
	{
		for (ThreadStatusListener listener : statusListeners)
		{
			listener.statusChanged(this, s, count);
		}
	}*/
	
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
		for (ThreadStatusListener listener : statusListeners)
		{
			listener.statusChanged(this, currentFilePath, currentLoadCount);
		}
	}

	@Override
	public void doWork() throws InterruptedException
	{
		/*this.wasInterrupted = false;
		try
		{
			load();
		}
		finally
		{
			notifyListeners();
		}*/
		load();
	}
	
	private void load() throws InterruptedException
	{
		int loadCount = 0;
		for(String s : fileNames)
		{
			/*if(Thread.currentThread().isInterrupted())
			{
				this.wasInterrupted = true;
				return;
			}*/
			checkForInterrupt();
			if(mode == FULL_IMAGE_MODE)
			{
				BufferedImage img = null;
				try
				{
					img = ImageIO.read(new File(s));
					
					ImagePanel imgPanel = new ImagePanel(img, s, imgRootPanel, false);
					images.add(imgPanel);
					
					MetadataPanel mp = new MetadataPanel(s);
					metadata.add(mp);
				}
				catch(IOException e)
				{
					ImagePanel errImgPanel = new ImagePanel(null, s, imgRootPanel, true);
					images.add(errImgPanel);
					
					MetadataPanel errMp = new MetadataPanel(s);
					metadata.add(errMp);
				}
			}
			else if(mode == THUMBNAIL_MODE)
			{
				Thumbnail t = new Thumbnail(s, true, thumbnailSize, thumbnailSize);
				thumbnails.add(t);
			}
			
			currentFilePath = s;
			currentLoadCount = loadCount;
			notifyStatusListeners();
			
			loadCount++;
		}
	}

	@Override
	public void preTerminateCleanup()
	{
		
	}

	@Override
	public void finalCleanup()
	{
		
	}
}