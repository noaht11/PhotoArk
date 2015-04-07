package com.lightark.Thread;

public interface ThreadStatusListener
{
	public void statusChanged(Runnable thread, Object... info);
}