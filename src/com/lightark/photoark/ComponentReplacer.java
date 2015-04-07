package com.lightark.photoark;

import javax.swing.JComponent;

public interface ComponentReplacer
{
	public void removeOldComponent(JComponent oldComponent);
	public void addNewComponent(JComponent newComponent);
}