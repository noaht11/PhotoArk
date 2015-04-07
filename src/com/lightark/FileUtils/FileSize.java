package com.lightark.FileUtils;

import java.io.File;

public class FileSize
{
	public final static int UNIT_BITS = -1;
	public final static int UNIT_BYTES = 0;
	public final static int UNIT_KB = 1;
	public final static int UNIT_MB = 2;
	public final static int UNIT_GB = 3;
	public final static int UNIT_TB = 4;
	public final static int UNIT_MAX = 4;
	
	private double size;
	private int units;
	
	public FileSize(File file, int units)
	{
		this(file.length(), units);
	}
	
	public FileSize(double size, int units)
	{
		this.size = size;
		this.units = units;
	}
	
	public double getSize()
	{
		return size;
	}
	
	public int getUnits()
	{
		return units;
	}
	
	public String asString(int decimals)
	{
		double rounded = (double)Math.round(size * (Math.pow(10, decimals))) / (Math.pow(10, decimals));
		String unitString = "";
		if(units == UNIT_BITS)
		{
			unitString = "Bits";
		}
		else if(units == UNIT_BYTES)
		{
			unitString = "Bytes";
		}
		else if(units == UNIT_KB)
		{
			unitString = "KB";
		}
		else if(units == UNIT_MB)
		{
			unitString = "MB";
		}
		else if(units == UNIT_GB)
		{
			unitString = "GB";
		}
		else if(units == UNIT_TB)
		{
			unitString = "TB";
		}
		return (rounded + " " + unitString);
	}
	
	public static FileSize getFileSizeInBestUnits(File file)
	{
		return getFileSizeInBestUnits(file.length());
	}

	public static FileSize getFileSizeInBestUnits(long bytes)
	{
		double unitBytes = bytes;
		int units = UNIT_BYTES;
		if(bytes >= Math.pow(1024, 1) && bytes < Math.pow(1024, 2))
		{
			units = UNIT_KB;
		}
		else if(bytes >= Math.pow(1024, 2) && bytes < Math.pow(1024, 3))
		{
			units = UNIT_MB;
		}
		else if(bytes >= Math.pow(1024, 3) && bytes < Math.pow(1024, 4))
		{
			units = UNIT_GB;
		}
		else if(bytes >= Math.pow(1024, 4))
		{
			units = UNIT_TB;
		}
		unitBytes = getFileSizeAs(bytes, units);
		return new FileSize(unitBytes, units);
	}
	
	public static double getFileSizeAs(File file, int units)
	{
		return getFileSizeAs(file.length(), units);
	}
	
	public static double getFileSizeAs(long bytes, int units)
	{
		double unitBytes = -1;
		if(units == UNIT_BITS)
		{
			unitBytes = (bytes / 8);
		}
		if(units == UNIT_KB)
		{
			unitBytes = (bytes * Math.pow(1024,-1));
		}
		else if(units == UNIT_MB)
		{
			unitBytes = (bytes * Math.pow(1024, -2));
		}
		else if(units == UNIT_GB)
		{
			unitBytes = (bytes * Math.pow(1024, -3));
		}
		else if(units == UNIT_TB)
		{
			unitBytes = (bytes * Math.pow(1024, -4));
		}
		return unitBytes;
	}
	
	public static long getCombinedFileSize(File[] files)
	{
		long bytes = 0;
		for(File f : files)
		{
			bytes += f.length();
		}
		return bytes;
	}
}