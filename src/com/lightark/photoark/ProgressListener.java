package com.lightark.photoark;

public interface ProgressListener
{
	public void initialize(int maximum);
	public void updateProgress(int increment);
	public void progressComplete();
}