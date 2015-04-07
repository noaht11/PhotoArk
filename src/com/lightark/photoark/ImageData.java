package com.lightark.photoark;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class ImageData
{
	public String filePath;
	public File file;
	
	private Metadata metadata = null;
	
	public boolean invalidFile = false;
	
	public ImageData(String filePath)
	{
		this(new File(filePath));
	}
	
	public ImageData(File src)
	{
		this.filePath = src.getAbsolutePath();
		this.file = src;
		
		if(file.exists())
		{
			try
			{
				metadata = ImageMetadataReader.readMetadata(file);
			}
			catch (ImageProcessingException e)
			{
				invalidFile = true;
			}
			catch (IOException e)
			{
				invalidFile = true;
			}
		}
		else
		{
			invalidFile = true;
		}
	}
	
	public Metadata getMetadata()
	{
		return metadata;
	}
	
	public Tag getTag(String tagName)
	{
		tagName = tagName.toLowerCase();
		for (Directory directory : metadata.getDirectories())
		{
	    	for(Tag tag : directory.getTags())
	    	{
	    		if(tag.getTagName().toLowerCase().matches(tagName))
	    		{
	    			return tag;
	    		}
	    	}
		}
		return null;
	}

	public void printData()
	{
		for (Directory directory : metadata.getDirectories())
		{
		    for (Tag tag : directory.getTags())
		    {
		        System.out.println(tag);
		    }
		}
	}


	public void printData(int num, String... tags)
	{
		for (Directory directory : metadata.getDirectories())
		{
		    for (Tag tag : directory.getTags())
		    {
		    	for(int i = 0;i < tags.length;i++)
		    	{
		    		if(tag.getTagName().toLowerCase().matches(tags[i].toLowerCase()))
		    		{
		    			System.out.println(tag);
		    		}
		    	}
		    }
		}
	}
	
	public void printData(String... directories)
	{
		for (Directory directory : metadata.getDirectories())
		{
			boolean dirMatch = false;
			for(String s : directories)
			{
				if(directory.getName().toLowerCase().matches(s.toLowerCase()))
				{
					dirMatch = true;
					break;
				}
			}
			if(dirMatch)
			{
				for (Tag tag : directory.getTags())
				{
					System.out.println(tag);
				}
			}
		}
	}
}