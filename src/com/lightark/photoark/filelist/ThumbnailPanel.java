package com.lightark.photoark.filelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lightark.photoark.ResourceLoader;

public class ThumbnailPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static int THUMBNAIL_BORDER = 10;
	
	public static URL tmpIconURL = ResourceLoader.loadResource("Resources/Image_TMP_200.png");
	public static ImageIcon tmpIcon = new ImageIcon(tmpIconURL);
	
	private String filePath;
	private int width;
	private int height;
	
	private Thumbnail thumbnail;
	private JLabel label;
	
	private int border = 10;
	
	public ThumbnailPanel(String filePath, boolean calculateDim, int width, int height)
	{
		this.filePath = filePath;
		this.width = width;
		this.height = height;

		this.setLayout(new BorderLayout());
		
		this.thumbnail = new Thumbnail(tmpIconURL, calculateDim, width, height);
		
		label = new JLabel(getFile().getName());
		label.setHorizontalAlignment(JLabel.CENTER);
		
		if(width > 0 && height > 0)
		{
			this.add(thumbnail, BorderLayout.CENTER);
		}
		this.add(label, BorderLayout.PAGE_END);
		
		this.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
	}
	
	public void setThumbnail(Thumbnail t)
	{
		this.remove(thumbnail);
		this.add(t, BorderLayout.CENTER);
	}
	
	public String getFilePath()
	{
		return filePath;
	}
	
	public File getFile()
	{
		return new File(filePath);
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension((border + width + border), (border + height + border + label.getHeight()));
	}
}