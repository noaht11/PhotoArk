package com.lightark.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class FileNames
{
	public static String getExtension(String fileName)
	{
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0)
		{
		    extension = fileName.substring(i+1);
		}
		return extension;
	}
	
	public static String getExtension(File f)
	{
		return getExtension(f.getName());
	}

	
	public static String getBaseName(String fileName)
	{
		String base = fileName.replaceFirst("[.][^.]+$", "");
		return base;
	}
	
	public static String getBaseName(File f)
	{
		return getBaseName(f.getName());
	}
	
	public static String createFileName(String baseName, String extension)
	{
		if(baseName == null || baseName.length() <= 0)
		{
			return null;
		}
		else
		{
			if(extension == null || extension.length() <= 0)
			{
				return baseName;
			}
			else
			{
				return (baseName + "." + extension);
			}
		}
	}
	
	public static String generateRename(File folder, File originalFile)
	{
		String extension = FileNames.getExtension(originalFile);
		int nameAppend = 2;
		String separator = "_";
		String baseName = FileNames.getBaseName(originalFile);
		String rename = (baseName + separator + nameAppend);
		ArrayList<String> fileStrs = new ArrayList<String>();
		for(File f : folder.listFiles())
		{
			fileStrs.add(FileNames.getBaseName(f));
		}
		while(fileStrs.indexOf(rename) != -1)
		{
			nameAppend++;
			rename = (baseName + separator + nameAppend);
		}
		
		String fullRename = FileNames.createFileName(rename, extension);
		return fullRename;
	}

	public static String[] verifyExistence(String... files)
	{
		if(files == null)
		{
			return null;
		}
		ArrayList<String> verified = new ArrayList<String>();
		for(String s : files)
		{
			if(new File(s).exists())
			{
				verified.add(s);
			}
		}
		String[] verifiedArray = new String[verified.size()];
		return verified.toArray(verifiedArray);
	}
	public static File[] verifyExistence(File... files)
	{
		if(files == null)
		{
			return null;
		}
		ArrayList<File> verified = new ArrayList<File>();
		for(File f : files)
		{
			if(f.exists())
			{
				verified.add(f);
			}
		}
		File[] verifiedArray = new File[verified.size()];
		return verified.toArray(verifiedArray);
	}
}