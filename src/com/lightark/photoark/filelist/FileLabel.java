package com.lightark.photoark.filelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class FileLabel extends JLabel
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int thumbnailSize = 100;

	private FileList fileList;
	
	private String filePath;
	
	private boolean selected = false;
	private boolean wasTmpDeselected = false;
	
	private JComponent component = null;

	private FileRightClickMenu fileRightClick;
	
	public FileLabel(FileList fl, String filePath, int viewType)
	{
		this.fileList = fl;
		this.filePath = filePath;
		
		this.component = this;
		
		if(viewType == FileList.VIEW_THUMBNAIL)
		{
			this.component = new ThumbnailPanel(filePath, true, fileList.getThumbnailSize(), fileList.getThumbnailSize());
			this.getComponent().setToolTipText(new File(filePath).getName());
		}
		
		this.setText(filePath);
		this.setOpaque(true);
		this.getComponent().setBackground(Color.white);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
		this.setFont(new Font("Arial", Font.PLAIN, 15));
		this.getComponent().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent evt)
			{
				fileList.focus();
				if(evt.getButton() == MouseEvent.BUTTON1)
				{
					if(evt.isControlDown() && !(evt.isShiftDown()) && !(evt.isAltDown()) && !(evt.isAltGraphDown()))
					{
						fileList.leftCTRLPress(evt, FileLabel.this);
					}
					else if(evt.isShiftDown() && !(evt.isAltDown()) && !(evt.isAltGraphDown()))
					{
						fileList.shiftPress(evt, FileLabel.this);
					}
					else if(!(evt.isAltDown()) && !(evt.isAltGraphDown()))
					{
						fileList.leftPress(evt, FileLabel.this);
					}
				}
				else if(evt.getButton() == MouseEvent.BUTTON3)
				{
					if(!evt.isControlDown() && !(evt.isShiftDown()) && !(evt.isAltDown()) && !(evt.isAltGraphDown()))
					{
						fileList.rightPress(evt, FileLabel.this);
					}
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				if((evt.getClickCount() == 2) && (evt.getButton() == MouseEvent.BUTTON1))
				{
					fileList.doubleLeftClick(evt, FileLabel.this);
				}
				else
				{
					if(evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3)
					{
						if(isSelected())
						{
							fileRightClick.show((Component)evt.getSource(), evt.getX(), evt.getY());
						}
					}
				}
			}
		});

		fileRightClick = new FileRightClickMenu(fileList);
	}
	
	public JComponent getComponent()
	{
		return component;
	}
	
	public File getFile()
	{
		return new File(filePath);
	}
	
	public String getFilePath()
	{
		return filePath;
	}
	
	public boolean wasTmpDeselected()
	{
		return wasTmpDeselected;
	}
	
	public boolean isSelected()
	{
		return selected;
	}

	public void select()
	{
		this.getComponent().setBackground(new Color(100,150,255));
		this.setForeground(Color.white);

		if(!fileList.selectedFileLabels.contains(this))
		{
			fileList.selectedFileLabels.add(this);
		}
		fileList.selected(this);
		fileList.getComponent().revalidate();
		fileList.getComponent().repaint();
		
		wasTmpDeselected = false;
		selected = true;
	}
	
	public void deselect()
	{
		this.getComponent().setBackground(Color.white);
		this.setForeground(Color.black);
		
		if(fileList.selectedFileLabels.contains(this))
		{
			fileList.selectedFileLabels.remove(this);
		}
		fileList.deselected(this);
		fileList.getComponent().revalidate();
		fileList.getComponent().repaint();
		
		wasTmpDeselected = false;
		selected = false;
	}
	
	public void tmpDeselect()
	{
		this.getComponent().setBackground(Color.LIGHT_GRAY);
		this.setForeground(Color.white);
		wasTmpDeselected = true;
		if(!fileList.tmpDeselected.contains(this))
		{
			fileList.tmpDeselected.add(this);
		}
		fileList.getComponent().revalidate();
		fileList.getComponent().repaint();
	}
	
	public void performAction()
	{
		try
		{
			Desktop.getDesktop().open(getFile());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}