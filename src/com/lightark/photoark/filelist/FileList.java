package com.lightark.photoark.filelist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.lightark.FileUtils.FileNames;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.Thread.ThreadStatusListener;
import com.lightark.photoark.ComponentReplacer;
import com.lightark.photoark.ImageLoader;
import com.lightark.photoark.PhotoArk;
import com.lightark.photoark.ProgressListener;
import com.lightark.photoark.ResourceLoader;
import com.lightark.photoark.SelectionListener;
import com.lightark.photoark.imagesearch.Search;

public class FileList implements FocusListener
{
	public final static String THREAD_ID = "FILE_LIST_THUMBNAIL_LOADER";
	
	public final static int VIEW_LIST = 0;
	public final static int VIEW_THUMBNAIL = 1;
	
	public final static int THUMBNAIL_SMALL = 100;
	public final static int THUMBNAIL_MEDIUM = 200;
	public final static int THUMBNAIL_LARGE = 400;

	private final static int LIST_SEP = 0;
	private final static int THUMBNAIL_SEP = 20;
	
	public final static int SCROLL_HORIZONTAL = 0;
	public final static int SCROLL_VERTICAL = 1;
	
	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	
	public CopyOnWriteArrayList<FileLabel> fileLabels = new CopyOnWriteArrayList<FileLabel>();
	public CopyOnWriteArrayList<FileLabel> selectedFileLabels = new CopyOnWriteArrayList<FileLabel>();
	
	public CopyOnWriteArrayList<FileLabel> tmpDeselected = new CopyOnWriteArrayList<FileLabel>();
	private boolean focusGainedOnOtherElement = false;
	
	private Container parent;
	
	private JScrollPane component;
	
	private JPanel bg;
	private JPanel panel;
	
	private boolean interruptedForSearch = false;
	private Search queuedSearch = null;
	private boolean queuedFilter = false;
	
	private boolean interruptedForViewChange = false;
	private int queuedNewViewType = -1;
	private ComponentReplacer queuedComponentReplacer = null;
	
	//private boolean thumbnailLoaderActive = false;
	//private Thread thumbnailLoaderThread;
	private ImageLoader thumbnailLoader;
	private GridLayout thumbnailGrid;
	
	private boolean gridConstrained = false;
	private int constrainedGridRows = 0;
	private int constrainedGridCols = 0;
	
	private int thumbnailSize = THUMBNAIL_SMALL;
	
	private JLabel loadingLabel;
	private JPanel loadingPanel;
	
	private int viewType;
	
	private int scrollOrient;
	
	public FileList(Container _parent, String[] initFilePaths, int _viewType, int _scrollOrient, ProgressListener pl)
	{
		this.parent = _parent;
		this.viewType = _viewType;  
		this.scrollOrient = _scrollOrient;
		if(pl != null)
		{
			addProgressListener(pl);
		}
		
		String[] checkedFilePaths = FileNames.verifyExistence(initFilePaths);
		
		createBG();
		
		createPanel();
		
		createComponent();

		ImageIcon loading = new ImageIcon(ResourceLoader.loadResource("Resources/loading_50.gif"));
		loadingLabel = new JLabel("Loading Files...",loading, JLabel.CENTER);
		loadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		loadingLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		loadingPanel = new JPanel();
		loadingPanel.setLayout(new GridBagLayout());
		loadingPanel.setBackground(Color.white);
		loadingPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		loadingPanel.add(loadingLabel);

		createThumbnailLoader(checkedFilePaths);

		generateFileLabels(checkedFilePaths);
	}
	
	private void createBG()
	{
		bg = new JPanel();
		if(viewType == VIEW_LIST)
		{
			bg.setLayout(new FlowLayout(FlowLayout.LEFT));
		}
		else if(viewType == VIEW_THUMBNAIL)
		{
			bg.setLayout(new BorderLayout());
		}
		bg.setOpaque(true);
		bg.setBackground(Color.white);
		bg.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		bg.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent evt)
			{
				if(!evt.isControlDown() && !(evt.isShiftDown()) && !(evt.isAltDown()) && !(evt.isAltGraphDown()))
				{
					deselectAllFiles();
				}
			}
		});
	}
	
	private void createPanel()
	{
		panel = new JPanel()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				
				if(viewType == VIEW_THUMBNAIL)
				{
					if(!gridConstrained)
					{
						if(scrollOrient == SCROLL_VERTICAL)
						{
							double gridCols = parent.getWidth() / (thumbnailSize + FileList.THUMBNAIL_SEP + ThumbnailPanel.THUMBNAIL_BORDER + component.getVerticalScrollBar().getWidth());
							if(gridCols <= 0)
							{
								gridCols = 1;
							}
							thumbnailGrid.setRows(0);
							thumbnailGrid.setColumns((int) Math.floor(gridCols));
						}
						else if(scrollOrient == SCROLL_HORIZONTAL)
						{
							double gridRows = parent.getHeight() / (thumbnailSize + FileList.THUMBNAIL_SEP + ThumbnailPanel.THUMBNAIL_BORDER + component.getHorizontalScrollBar().getHeight());
							if(gridRows <= 0)
							{
								gridRows = 1;
							}
							thumbnailGrid.setRows((int) Math.floor(gridRows));
							thumbnailGrid.setColumns(0);
						}
					}
					this.revalidate();
				}
			}
		};
		if(viewType == VIEW_LIST)
		{
			panel.setLayout(new GridLayout(0,1));
			panel.setBorder(null);
		}
		else if(viewType == VIEW_THUMBNAIL)
		{
			thumbnailGrid = new GridLayout(0,4);
			thumbnailGrid.setHgap(20);
			thumbnailGrid.setVgap(20);
			panel.setLayout(thumbnailGrid);
			panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}
		panel.setOpaque(true);
		panel.setBackground(Color.white);
		panel.setFocusable(true);
		panel.addFocusListener(this);
		AbstractAction upAction = new AbstractAction()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(selectedFileLabels.size() > 0)
				{
					int oldIndex = fileLabels.indexOf(selectedFileLabels.get((selectedFileLabels.size() - 1)));
					int newIndex = oldIndex - 1;
					if(newIndex >= 0)
					{
						deselectAllFiles();
						FileLabel newLabel = fileLabels.get(newIndex);
						newLabel.select();
						
						scrollUpToSelected();
					}
				}
			}
			
		};
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		panel.getActionMap().put("UP", upAction);
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		panel.getActionMap().put("LEFT", upAction);
		
		AbstractAction downAction = new AbstractAction()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(selectedFileLabels.size() > 0)
				{
					int oldIndex = fileLabels.indexOf(selectedFileLabels.get((selectedFileLabels.size() - 1)));
					int newIndex = oldIndex + 1;
					if(newIndex < fileLabels.size())
					{
						deselectAllFiles();
						FileLabel newLabel = fileLabels.get(newIndex);
						newLabel.select();
						
						scrollDownToSelected();
					}
				}
			}
			
		};
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		panel.getActionMap().put("DOWN", downAction);
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		panel.getActionMap().put("RIGHT", downAction);
		
		AbstractAction enterAction = new AbstractAction()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				for(FileLabel fl : selectedFileLabels)
				{
					fl.performAction();
				}
			}
			
		};
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		panel.getActionMap().put("ENTER", enterAction);

		AbstractAction selectAllAction = new AbstractAction()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				selectAllFiles();
			}
			
		};
		panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "SELECTALL");
		panel.getActionMap().put("SELECTALL", selectAllAction);
		
		if(viewType == VIEW_LIST)
		{
			bg.add(panel);
		}
		else if(viewType == VIEW_THUMBNAIL)
		{
			if(scrollOrient == SCROLL_VERTICAL)
			{
				bg.add(panel, BorderLayout.PAGE_START);
			}
			else if(scrollOrient == SCROLL_HORIZONTAL)
			{
				bg.add(panel, BorderLayout.LINE_START);
			}
		}
	}
	
	private void createComponent()
	{
		component = new JScrollPane(bg);
		component.getVerticalScrollBar().setUnitIncrement(20);
		component.getHorizontalScrollBar().setUnitIncrement(20);
	}
	
	private void createThumbnailLoader(String[] filePaths)
	{
		//if(viewType == VIEW_THUMBNAIL)
		//{
			thumbnailLoader = new ImageLoader(filePaths, null, ImageLoader.THUMBNAIL_MODE, thumbnailSize);
			thumbnailLoader.addStatusListener(new ThreadStatusListener()
			{
				@Override
				public void statusChanged(Runnable thread, final Object... info)
				{
					final Thumbnail t = ((ImageLoader)thread).thumbnails.get((Integer)info[1]);
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							((ThumbnailPanel)fileLabels.get(getAllFileNames().indexOf(info[0])).getComponent()).setThumbnail(t);
							for(ProgressListener pl : progressListeners)
							{
								pl.updateProgress(1);
							}
							panel.revalidate();
							panel.repaint();
							component.revalidate();
							component.repaint();
						}
					});
				}
			});
			thumbnailLoader.addCompleteListener(new ThreadCompleteListener()
			{
				@Override
				public void notifyOfThreadComplete(Runnable thread)
				{
					//thumbnailLoaderActive = false;
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for(ProgressListener pl : progressListeners)
							{
								pl.progressComplete();
							}
							if(interruptedForSearch)
							{
								loadNewData(queuedSearch, queuedFilter);
							}
							if(interruptedForViewChange)
							{
								setViewType(queuedNewViewType, queuedComponentReplacer);
							}
						}
					});
				}
			});
			thumbnailLoader.setThreadName(THREAD_ID);
			PhotoArk.threadManager.registerThread(THREAD_ID, thumbnailLoader);
		//}
	}
	
	private void generateFileLabels(String[] filePaths)
	{
		panel.removeAll();
		fileLabels.clear();
		selectedFileLabels.clear();
		tmpDeselected.clear();
		
		if(Arrays.asList(parent.getComponents()).indexOf(loadingPanel) != -1)
		{
			parent.remove(loadingPanel);
			parent.add(this.component);
			parent.revalidate();
			parent.repaint();
		}
		
		if(filePaths != null)
		{
			for(String s : filePaths)
			{
				FileLabel label = new FileLabel(this, s, viewType);
				fileLabels.add(label);
				panel.add(label.getComponent());
			}
		}
		
		panel.revalidate();
		panel.repaint();
		component.revalidate();
		component.repaint();

		
		if(viewType == VIEW_THUMBNAIL)
		{
			/*if(!thumbnailLoaderActive)
			{
				thumbnailLoaderActive = true;
				for(ProgressListener pl : progressListeners)
				{
					pl.initialize(filePaths.length);
				}
				thumbnailLoader.reset();
				thumbnailLoader.setFileNames(filePaths);
				thumbnailLoaderThread = new Thread(thumbnailLoader);
				thumbnailLoaderThread.start();
			}*/
			if(!thumbnailLoader.isActive())
			{
				for(ProgressListener pl : progressListeners)
				{
					pl.initialize(filePaths.length);
				}
				thumbnailLoader.reset();
				thumbnailLoader.setFileNames(filePaths);
				thumbnailLoader.startThread();
			}
		}
	}
	
	public void setViewType(int newViewType, ComponentReplacer cr)
	{
		if(thumbnailLoader.isActive())
		{
			//thumbnailLoaderThread.interrupt();
			thumbnailLoader.interrupt();
		}
		if(!thumbnailLoader.isActive())
		{
			interruptedForViewChange = false;
			queuedNewViewType = -1;
			queuedComponentReplacer = null;
			
			cr.removeOldComponent(getComponent());
			this.viewType = newViewType;
			createBG();
			createPanel();
			if(gridConstrained)
			{
				constrainGrid(constrainedGridRows, constrainedGridCols);
			}
			createComponent();
			createThumbnailLoader(getAllFileNamesArray());
			generateFileLabels(getAllFileNamesArray());
			cr.addNewComponent(getComponent());
		}
		else
		{
			interruptedForViewChange = true;
			queuedNewViewType = newViewType;
			queuedComponentReplacer = cr;
		}
	}
	
	public void constrainGrid(int rows, int cols)
	{
		gridConstrained = true;
		constrainedGridRows = rows;
		constrainedGridCols = cols;
		thumbnailGrid.setRows(rows);
		thumbnailGrid.setColumns(cols);
		panel.revalidate();
	}
	
	public void deconstrainGrid()
	{
		gridConstrained = false;
		panel.revalidate();
	}
	
	public void scrollDownToSelected()
	{
		if(selectedFileLabels.size() <= 0)
		{
			return;
		}
		JComponent offScreen = selectedFileLabels.get((selectedFileLabels.size() - 1)).getComponent();
		int sepVal = 0;
		if(viewType == VIEW_LIST)
		{
			sepVal = LIST_SEP + 10;
		}
		else if(viewType == VIEW_THUMBNAIL)
		{
			sepVal = THUMBNAIL_SEP;
		}

		if(scrollOrient == SCROLL_VERTICAL)
		{
			int vertDiff = (int) (offScreen.getHeight() - offScreen.getVisibleRect().getHeight());
			if(vertDiff > 0)
			{
				int labelBound = (offScreen.getBounds().y + offScreen.getHeight() + sepVal);
				int componentHeight = component.getHeight();
				component.getVerticalScrollBar().setValue(labelBound - componentHeight);
			}
		}
		else if(scrollOrient == SCROLL_HORIZONTAL)
		{
			int horizDiff = (int) (offScreen.getWidth() - offScreen.getVisibleRect().getWidth());
			if(horizDiff > 0)
			{
				int labelBound = (offScreen.getBounds().x + offScreen.getWidth() + sepVal);
				int componentWidth = component.getWidth();
				component.getHorizontalScrollBar().setValue(labelBound - componentWidth);
			}
		}
	}
	
	public void scrollUpToSelected()
	{
		if(selectedFileLabels.size() <= 0)
		{
			return;
		}
		JComponent offScreen = selectedFileLabels.get((selectedFileLabels.size() - 1)).getComponent();
		int sepVal = 0;
		if(viewType == VIEW_LIST)
		{
			sepVal = LIST_SEP + 10;
		}
		else if(viewType == VIEW_THUMBNAIL)
		{
			sepVal = THUMBNAIL_SEP;
		}
		
		if(scrollOrient == SCROLL_VERTICAL)
		{
			int vertDiff = (int) (offScreen.getHeight() - offScreen.getVisibleRect().getHeight());
			if(vertDiff > 0)
			{
				int labelY = (offScreen.getBounds().y);
				component.getVerticalScrollBar().setValue(labelY);
			}
		}
		else if(scrollOrient == SCROLL_HORIZONTAL)
		{
			int horizDiff = (int) (offScreen.getWidth() - offScreen.getVisibleRect().getWidth());
			if(horizDiff > 0)
			{
				int labelX = (offScreen.getBounds().x);
				component.getHorizontalScrollBar().setValue(labelX - sepVal);
			}
		}
	}
	
	public void cancelThumbnailLoading()
	{
		if(thumbnailLoader.isActive())
		{
			//thumbnailLoaderThread.interrupt();
			thumbnailLoader.interrupt();
		}
	}
	
	public int getThumbnailSize()
	{
		return thumbnailSize;
	}
	
	public void setThumbnailSize(int newSize)
	{
		thumbnailSize = newSize;
	}
	
	public void addProgressListener(ProgressListener pl)
	{
		progressListeners.add(pl);
	}
	
	public void removeProgressListener(ProgressListener pl)
	{
		progressListeners.remove(pl);
	}
	
	public void addSelectionListener(SelectionListener sl)
	{
		listeners.add(sl);
	}
	
	public void removeSelectionListener(SelectionListener sl)
	{
		listeners.remove(sl);
	}
	
	public void doubleLeftClick(MouseEvent evt, FileLabel src)
	{
		for(FileLabel fl : selectedFileLabels)
		{
			fl.performAction();
		}
	}
	
	public void rightPress(MouseEvent evt, FileLabel src)
	{
		if(tmpDeselected.indexOf(src) != -1)
		{
			refocus();
		}
		else
		{
			focusGainedOnOtherElement = true;
			if(!src.isSelected())
			{
				deselectAllFiles();
				src.select();
			}
			refocus();
		}
	}
	
	public void leftPress(MouseEvent evt, FileLabel src)
	{
		if(tmpDeselected.indexOf(src) != -1)
		{
			refocus();
		}
		else
		{
			focusGainedOnOtherElement = true;
			deselectAllFiles();
			src.select();
			refocus();
		}
	}
	
	public void leftCTRLPress(MouseEvent evt, FileLabel src)
	{
		if(src.isSelected())
		{
			src.deselect();
		}
		else
		{
			src.select();
		}
	}
	
	public void shiftPress(MouseEvent evt, FileLabel src)
	{
		int prevSelect = fileLabels.indexOf(selectedFileLabels.get(selectedFileLabels.size() - 1));
		if(!evt.isControlDown())
		{
			deselectAllFiles();
		}
		int newSelect = fileLabels.indexOf(src);
		int start = Math.min(prevSelect, newSelect);
		int end = Math.max(prevSelect, newSelect);
		for(int i = start;i <= end;i++)
		{
			fileLabels.get(i).select();
		}
	}
	
	public Container getParent()
	{
		return parent;
	}
	
	public JLabel getLoadingLabel()
	{
		return loadingLabel;
	}
	
	public void loadNewData(Search search, boolean filter)
	{	
		parent.remove(this.component);
		parent.add(loadingPanel);
		parent.revalidate();
		parent.repaint();

		if(thumbnailLoader.isActive())
		{
			//thumbnailLoaderThread.interrupt();
			thumbnailLoader.interrupt();
		}
		if(thumbnailLoader.isActive())
		{
			interruptedForSearch = true;
			queuedSearch = search;
			queuedFilter = filter;
		}
		else
		{
			interruptedForSearch = false;
			queuedSearch = null;
			queuedFilter = false;
			search.refreshFileList(filter);
		}
	}
	
	public void removeSelectedFiles()
	{
		for(FileLabel fl : selectedFileLabels)
		{
			if(Arrays.asList(panel.getComponents()).indexOf(fl.getComponent()) != -1)
			{
				panel.remove(fl.getComponent());
			}
			fileLabels.remove(fl);
		}
		selectedFileLabels.clear();
		
		panel.revalidate();
		panel.repaint();
		component.revalidate();
		component.repaint();
	}

	public void addFiles(File[] files)
	{
		String[] strs = new String[files.length];
		for(int i = 0;i < files.length;i++)
		{
			strs[i] = files[i].getAbsolutePath();
		}
		addFiles(strs);
	}
	
	public void addFiles(String[] filePaths)
	{
		ArrayList<String> existingFiles = getAllFileNames();
		ArrayList<String> addedFiles = new ArrayList<String>();
		for(String s : filePaths)
		{
			if(!existingFiles.contains(s))
			{
				FileLabel label = new FileLabel(this, s, viewType);
				fileLabels.add(label);
				panel.add(label.getComponent());
				addedFiles.add(s);
			}
		}
		String[] addedFilesArray = new String[addedFiles.size()];
		addedFilesArray = addedFiles.toArray(addedFilesArray);
		
		panel.revalidate();
		panel.repaint();
		component.revalidate();
		component.repaint();

		
		if(viewType == VIEW_THUMBNAIL)
		{
			if(!thumbnailLoader.isActive())
			{
				//thumbnailLoaderActive = true;
				for(ProgressListener pl : progressListeners)
				{
					pl.initialize(addedFilesArray.length);
				}
				thumbnailLoader.reset();
				thumbnailLoader.setFileNames(addedFilesArray);
				//thumbnailLoaderThread = new Thread(thumbnailLoader);
				//thumbnailLoaderThread.start();
				thumbnailLoader.startThread();
			}
		}
	}
	
	public void setFileNames(File[] newFiles, int selectedIndex)
	{
		ArrayList<String> filePaths = new ArrayList<String>();
		for(int i = 0;i < newFiles.length;i++)
		{
			if(newFiles[i].exists() && !newFiles[i].isDirectory())
			{
				String extension = FileNames.getExtension(newFiles[i]);
				for(int j = 0;j < PhotoArk.imageExtensions.length;j++)
				{
					if(("." + extension).toLowerCase().matches(PhotoArk.imageExtensions[j].toLowerCase()))
					{
						filePaths.add(newFiles[i].getAbsolutePath());
					}
				}
			}
		}
		String[] filePathsArray = new String[filePaths.size()];
		filePaths.toArray(filePathsArray);
		Arrays.sort(filePathsArray);
		setFileNames(filePathsArray, selectedIndex);
	}
	
	public void setFileNames(ArrayList<String> newNames, int selectedIndex)
	{
		String[] strs = new String[newNames.size()];
		newNames.toArray(strs);
		Arrays.sort(strs);
		setFileNames(newNames, selectedIndex);
	}
	
	public void setFileNames(String[] newNames, int selectedIndex)
	{
		Arrays.sort(newNames);
		generateFileLabels(FileNames.verifyExistence(newNames));
		if(fileLabels.size() > 0)
		{
			if(selectedIndex >= 0 && selectedIndex < fileLabels.size())
			{
				fileLabels.get(selectedIndex).select();
			}
			else
			{
				fileLabels.get(0).select();
			}
		}
	}
	
	public JScrollPane getComponent()
	{
		return component;
	}

	public ArrayList<String> getSelectedFileNames()
	{
		ArrayList<String> strs = new ArrayList<String>();
		for(FileLabel fl : selectedFileLabels)
		{
			strs.add(fl.getFilePath());
		}
		return strs;
	}
	
	public String[] getSelectedFileNamesArray()
	{
		String[] strs = new String[selectedFileLabels.size()];
		for(int i = 0;i < selectedFileLabels.size();i++)
		{
			strs[i] = selectedFileLabels.get(i).getFilePath();
		}
		return strs;
	}
	
	public ArrayList<String> getAllFileNames()
	{
		ArrayList<String> strs = new ArrayList<String>();
		for(FileLabel fl : fileLabels)
		{
			strs.add(fl.getFilePath());
		}
		return strs;
	}
	
	public String[] getAllFileNamesArray()
	{
		String[] strs = new String[fileLabels.size()];
		for(int i = 0;i < fileLabels.size();i++)
		{
			strs[i] = fileLabels.get(i).getFilePath();
		}
		return strs;
	}

	public File[] getAllFilesArray()
	{
		File[] files = new File[fileLabels.size()];
		for(int i = 0;i < fileLabels.size();i++)
		{
			files[i] = fileLabels.get(i).getFile();
		}
		return files;
	}

	public void exportToFile(File dest, String[] files, String sep) throws IOException
	{
		ArrayList<String> strs = new ArrayList<String>();
		for(String s : files)
		{
			strs.add(s);
		}
		exportToFile(dest, strs, sep);
	}
	public void exportToFile(File dest, ArrayList<String> files, String sep) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
		for(int i = 0;i < files.size();i++)
		{
			bw.write(files.get(i));
			if(i < (files.size() - 1))
			{
				bw.write(sep);
			}
		}
		bw.close();
	}
	
	public int getFileCount()
	{
		return fileLabels.size();
	}

	public void selectAllFiles()
	{
		for(FileLabel l : fileLabels)
		{
			l.select();
		}
	}
	
	public void deselectAllFiles()
	{
		for(FileLabel l : fileLabels)
		{
			l.deselect();
		}
	}
	
	public void selected(Object data)
	{
		for(SelectionListener sl : listeners)
		{
			sl.selected(data);
		}
	}
	public void deselected(Object data)
	{
		for(SelectionListener sl : listeners)
		{
			sl.deselected(data);
		}
	}

	public void focus()
	{
		panel.requestFocusInWindow();
	}
	
	public void refocus()
	{
		if(focusGainedOnOtherElement)
		{
			for(FileLabel l : tmpDeselected)
			{
				l.deselect();
			}
		}
		else
		{
			for(FileLabel l : tmpDeselected)
			{
				l.select();
			}
		}
		tmpDeselected.clear();
	}
	
	@Override
	public void focusGained(FocusEvent evt)
	{
		refocus();
	}

	@Override
	public void focusLost(FocusEvent evt)
	{
		for(FileLabel l : selectedFileLabels)
		{
			l.tmpDeselect();
		}
		focusGainedOnOtherElement = false;
	}
}