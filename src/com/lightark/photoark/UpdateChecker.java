package com.lightark.photoark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker
{
	private URL updateFlag;
	private String latestVersion = "";
	
	public UpdateChecker(URL url)
	{
		this.updateFlag = url;
	}
	
	public boolean checkForUpdate() throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(updateFlag.openStream()));
		latestVersion = in.readLine();
		if(!latestVersion.matches(PhotoArk.version))
		{
			return true;
		}
		return false;
	}
	
	public String getLatestVersionString()
	{
		return latestVersion;
	}
}