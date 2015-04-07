package com.lightark.photoark.filelist;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.lightark.photoark.PhotoArk;
import com.lightark.photoark.imageviewer.CompareWindow;
import com.lightark.photoark.imageviewer.ImageViewer;

public class FileRightClickMenu extends JPopupMenu implements ClipboardOwner
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FileList fileList;
	
	private JMenuItem folder;
	private JMenuItem copy;
	private JMenuItem compare;
	
	public FileRightClickMenu(FileList fl)
	{		
		this.fileList = fl;

		JMenuItem open = new JMenuItem("Open with system default viewer");
		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					for(JLabel l : fileList.selectedFileLabels)
					{
						Desktop.getDesktop().open(new File(l.getText()));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		this.add(open);
		
		JMenuItem arkOpen = new JMenuItem("Open with PhotoArk viewer");
		arkOpen.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(fileList.selectedFileLabels.size() > 1)
				{
					ImageViewer iv = new ImageViewer(fileList.getSelectedFileNamesArray(), 0, ImageViewer.SEARCH_RESULTS_DIR_STRING, true);
					iv.setVisible(true);
					PhotoArk.openFrames.add(iv);
				}
				else
				{
					ImageViewer iv = new ImageViewer(new String[]{fileList.selectedFileLabels.get(0).getFilePath()}, 0, "", false);
					iv.setVisible(true);
					PhotoArk.openFrames.add(iv);
				}
			}
		});
		this.add(arkOpen);
		
		folder = new JMenuItem("Open containing folder");
		folder.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(fileList.selectedFileLabels.size() == 1)
				{
					File f = fileList.selectedFileLabels.get(0).getFile();
					File parentF = f.getParentFile();
					try
					{
						Desktop.getDesktop().open(parentF);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		this.add(folder);
		
		this.addSeparator();
		
		JMenuItem remove = new JMenuItem("Remove from list (Does not delete files)");
		remove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				fileList.removeSelectedFiles();
			}
		});
		this.add(remove);
		
		this.addSeparator();

		copy = new JMenuItem("Copy file path to clipboard");
		copy.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(fileList.selectedFileLabels.size() == 1)
				{
				    StringSelection stringSelection = new StringSelection(fileList.selectedFileLabels.get(0).getFilePath());
				    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				    clipboard.setContents(stringSelection, FileRightClickMenu.this);
				}
			}
		});
		this.add(copy);
		
		this.addSeparator();
		
		compare = new JMenuItem("Compare...");
		compare.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(fileList.selectedFileLabels.size() > 1)
				{
					CompareWindow compare = new CompareWindow(fileList.getSelectedFileNamesArray());
					compare.setVisible(true);
					PhotoArk.openFrames.add(compare);
				}
			}
		});
		this.add(compare);
	}
	
	@Override
	public void show(Component invoker, int x, int y)
	{
		if(fileList.selectedFileLabels.size() > 1)
		{
			folder.setEnabled(false);
			copy.setEnabled(false);
		}
		if(fileList.selectedFileLabels.size() <= 1)
		{
			compare.setEnabled(false);
		}
		super.show(invoker, x, y);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		
	}
	
}