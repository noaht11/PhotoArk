package com.lightark.photoark.imageviewer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;

import com.lightark.FileUtils.DirectoryChooser;
import com.lightark.FileUtils.FileNames;
import com.lightark.FileUtils.OpenFileChooser;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.Thread.ThreadStatusListener;
import com.lightark.photoark.AboutDialog;
import com.lightark.photoark.ComponentReplacer;
import com.lightark.photoark.ImageLoader;
import com.lightark.photoark.PhotoArk;
import com.lightark.photoark.ProgressListener;
import com.lightark.photoark.ResourceLoader;
import com.lightark.photoark.SelectionListener;
import com.lightark.photoark.filelist.FileLabel;
import com.lightark.photoark.filelist.FileList;
import com.lightark.photoark.imagesearch.SearchFrame;

public class ImageViewer extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String SEARCH_RESULTS_DIR_STRING = "<Search Results>";
	
	public static final String THREAD_ID = "IMAGE_VIEWER_IMAGE_LOADER";

	private JSplitPane splitPaneLeft;
	//private JSplitPane splitPaneRight;
	
	boolean isShowingResults = false;
	private JPanel fileListPanelContainer;
	private JLabel fileListLabel;
	private JTextField directoryField;
	protected FileList fileList;
	
	private ComponentReplacer fileListCR;

	boolean ignoreKeyCommands = false;
	private JPanel previewContainer;
	private JPanel imgRootPanel;
	private LayoutManager imgBorderLayout = new BorderLayout();
	private LayoutManager imgGridBagLayout = new GridBagLayout();
	
	private JPanel metadataContainer;
	private JPanel metaRootPanel;
	private LayoutManager metaBorderLayout = new BorderLayout();
	private LayoutManager metaGridBagLayout = new GridBagLayout();
	
	//public boolean loaderActive = false;
	private ImageLoader loader;
	//private Thread loaderThread;
	private JLabel imgLoadingLabel;
	private JLabel metaLoadingLabel;
	private FileLabel nextToLoad = null;
	
	private ImagePanel currentPanel = null;
	
	private JPanel progressPanel;
	private JProgressBar progress;
	private JButton progressCancel;
	
	private String[] fileNames;
	
	private JMenuBar menuBar;
	private ArrayList<JMenuItem> imageMenuItems = new ArrayList<JMenuItem>();

	public AboutDialog ad;
	
	public ImageViewer(String[] initFiles, int preSelectedIndex, String folderPath, boolean isSearchResults)
	{
		this.isShowingResults = isSearchResults;
		this.fileNames = FileNames.verifyExistence(initFiles);
		
		this.setSize(500,500);
		this.setTitle((PhotoArk.appName + " - " + "Viewer"));
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		
		this.setLayout(new BorderLayout());
		
		ad = new AboutDialog(this);
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				PhotoArk.openFrames.remove(ImageViewer.this);
				if(PhotoArk.openFrames.size() <= 0)
				{
					PhotoArk.exitApp();
				}
				dispose();
				PhotoArk.threadManager.terminateThreadsForKey(THREAD_ID);
			}
		});

		ImageIcon loading = new ImageIcon(ResourceLoader.loadResource("Resources/loading_50.gif"));
		imgLoadingLabel = new JLabel("",loading, JLabel.CENTER);
		imgLoadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		imgLoadingLabel.setHorizontalTextPosition(JLabel.CENTER);
		ImageIcon loading2 = new ImageIcon(ResourceLoader.loadResource("Resources/loading_50.gif"));
		metaLoadingLabel = new JLabel("",loading2, JLabel.CENTER);
		metaLoadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		metaLoadingLabel.setHorizontalTextPosition(JLabel.CENTER);

		fileListPanelContainer = new JPanel()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height + fileList.getComponent().getHorizontalScrollBar().getHeight());
			}
		};
		fileListPanelContainer.setLayout(new BorderLayout());
		fileListPanelContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel fileListLabelPanel = new JPanel();
		fileListLabelPanel.setLayout(new BorderLayout());
		
		fileListLabel = new JLabel()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText()
			{
				return "File List (" + getNumberOfFiles() + ")";
			}
		};
		fileListLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
		fileListLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		fileListLabelPanel.add(fileListLabel, BorderLayout.LINE_START);

		JPanel directoryFieldPanel = new JPanel();
		directoryFieldPanel.setLayout(new BorderLayout());
		directoryFieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
		
		directoryField = new JTextField(folderPath);
		directoryField.setCaretPosition(directoryField.getText().length());
		directoryField.setFont(new Font("Arial",Font.PLAIN,16));
		directoryField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		directoryField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent evt)
			{
				if(evt.getSource() instanceof JTextField)
				{
					JTextField src = (JTextField) evt.getSource();
					File testFile = new File(src.getText());
					if(testFile.exists() && testFile.isDirectory())
					{
						src.setForeground(Color.black);
					}
					else
					{
						if(!isShowingResults)
						{
							src.setForeground(Color.red);
						}
					}
				}
			}
		});
		directoryField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent evt)
			{
				if(evt.getSource() instanceof JTextField)
				{
					ignoreKeyCommands = true;
					JTextField src = (JTextField) evt.getSource();
					src.selectAll();
				}
			}
			
			@Override
			public void focusLost(FocusEvent evt)
			{
				ignoreKeyCommands = false;
			}
		});
		directoryField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				loadFromDirField(0);
			}
		});
		directoryFieldPanel.add(directoryField, BorderLayout.CENTER);
		
		
		fileListLabelPanel.add(directoryFieldPanel, BorderLayout.CENTER);

		JPanel browsePanel = new JPanel();
		browsePanel.setLayout(new BorderLayout());
		browsePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
		
		JButton browseButton = new JButton("BROWSE");
		browseButton.setFont(new Font("Comic Sans MS",Font.BOLD,15));
		browseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				DirectoryChooser dc = new DirectoryChooser(ImageViewer.this, new File(directoryField.getText()))
				{
					@Override
					public void chosen(Object obj)
					{
						directoryField.setText(((File)obj).getAbsolutePath());
						loadFromDirField(0);
					}
				};
				dc.showChooser();
			}
		});
		
		browsePanel.add(browseButton);
		
		fileListLabelPanel.add(browsePanel, BorderLayout.LINE_END);

		progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
				
		progress = new JProgressBar(0, fileNames.length);
		progress.setValue(0);
		progress.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		progress.setStringPainted(true);
		
		progressCancel = new JButton("Cancel");
		progressCancel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		progressCancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				fileList.cancelThumbnailLoading();
			}
		});
		
		progressPanel.add(progress, BorderLayout.CENTER);
		progressPanel.add(progressCancel, BorderLayout.LINE_END);
		
		ProgressListener pl = new ProgressListener()
		{
			@Override
			public void initialize(final int maximum)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						progress.setMaximum(maximum);
						progress.setValue(0);
						progress.setString("Loading Thumbnails - 0%");
						if(Arrays.asList(fileListPanelContainer.getComponents()).indexOf(progressPanel) == -1)
						{
							fileListPanelContainer.add(progressPanel, BorderLayout.PAGE_END);
							fileListPanelContainer.revalidate();
							fileListPanelContainer.repaint();
						}
					}
				});
			}

			@Override
			public void updateProgress(final int increment)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						progress.setValue(progress.getValue() + increment);
						double decimal = (double)progress.getValue() / (double)progress.getMaximum();
						int percentage = Math.round((float)(decimal * (double)100));
						progress.setString(("Loading Thumbnails - " + percentage + "%"));
					}
				});
			}

			@Override
			public void progressComplete()
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						fileListPanelContainer.remove(progressPanel);
						revalidate();
						repaint();
					}
				});
			}
		};
		
		fileList = new FileList(fileListPanelContainer, fileNames, FileList.VIEW_THUMBNAIL, FileList.SCROLL_HORIZONTAL, pl);
		fileList.addSelectionListener(new SelectionListener()
		{
			@Override
			public void selected(Object data)
			{
				FileLabel source = (FileLabel)data;
				if(source.wasTmpDeselected())
				{
					return;
				}
				imgRootPanel.removeAll();
				imgRootPanel.setLayout(imgGridBagLayout);
				imgRootPanel.add(imgLoadingLabel);
				imgRootPanel.revalidate();
				imgRootPanel.repaint();
				metaRootPanel.removeAll();
				metaRootPanel.setLayout(metaGridBagLayout);
				metaRootPanel.add(metaLoadingLabel);
				metaRootPanel.revalidate();
				metaRootPanel.repaint();
				/*if(!loaderActive)
				{
					loaderActive = true;
					nextToLoad = null;
					loader.reset();
					loader.setFileNames(new String[]{source.getFilePath()});
					loaderThread = new Thread(loader);
					loaderThread.start();
				}
				else
				{
					nextToLoad = source;
				}*/
				if(!loader.isActive())
				{
					nextToLoad = null;
					loader.reset();
					loader.setFileNames(new String[]{source.getFilePath()});
					loader.startThread();
				}
				else
				{
					nextToLoad = source;
				}
			}

			@Override
			public void deselected(Object data)
			{
				
			}
		});
		
		fileListPanelContainer.add(fileListLabelPanel, BorderLayout.PAGE_START);
		fileListPanelContainer.add(fileList.getComponent(), BorderLayout.CENTER);
		
		fileListCR = new ComponentReplacer()
		{
			@Override
			public void removeOldComponent(JComponent oldComponent)
			{
				fileListPanelContainer.remove(oldComponent);
				fileListPanelContainer.validate();
				fileListPanelContainer.repaint();
			}
			
			@Override
			public void addNewComponent(JComponent newComponent)
			{
				fileListPanelContainer.add(newComponent, BorderLayout.CENTER);
				fileListPanelContainer.validate();
				fileListPanelContainer.repaint();
			}
		};
		
		//this.add(fileListPanelContainer);
		

		previewContainer = new JPanel();
		previewContainer.setLayout(new BorderLayout());
		previewContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		JLabel previewLabel = new JLabel("Preview");
		previewLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
		previewLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		imgRootPanel = new JPanel();
		imgRootPanel.setLayout(imgBorderLayout);
		imgRootPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		imgRootPanel.setBackground(Color.white);
		
		previewContainer.add(previewLabel, BorderLayout.PAGE_START);
		previewContainer.add(imgRootPanel, BorderLayout.CENTER);

		metadataContainer = new JPanel();
		metadataContainer.setLayout(new BorderLayout());
		metadataContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		JLabel metadataLabel = new JLabel("Metadata");
		metadataLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
		metadataLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		metaRootPanel = new JPanel();
		metaRootPanel.setLayout(metaBorderLayout);
		metaRootPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		metaRootPanel.setBackground(Color.white);
		
		metadataContainer.add(metadataLabel, BorderLayout.PAGE_START);
		metadataContainer.add(metaRootPanel, BorderLayout.CENTER);
		
		//this.add(previewContainer);
		
		/*splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		splitPaneLeft.setLeftComponent(fileListPanelContainer);
		splitPaneLeft.setRightComponent(previewContainer);
		splitPaneLeft.setResizeWeight(0.2);
		splitPaneLeft.setOneTouchExpandable(true);
		
		splitPaneRight.setLeftComponent(splitPaneLeft);
		splitPaneRight.setRightComponent(metadataContainer);
		splitPaneRight.setResizeWeight(0.8);
		splitPaneRight.setOneTouchExpandable(true);*/
		splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//splitPaneRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		splitPaneLeft.setLeftComponent(previewContainer);
		splitPaneLeft.setRightComponent(metadataContainer);
		splitPaneLeft.setResizeWeight(0.8);
		splitPaneLeft.setOneTouchExpandable(true);
		
		/*splitPaneRight.setTopComponent(splitPaneLeft);
		splitPaneRight.setBottomComponent(fileListPanelContainer);
		splitPaneRight.setResizeWeight(0.7);
		splitPaneRight.setOneTouchExpandable(true);*/
		
		fileList.constrainGrid(1, 0);
		fileList.getComponent().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		this.add(splitPaneLeft, BorderLayout.CENTER);
		
		this.add(fileListPanelContainer, BorderLayout.PAGE_END);
		
		loader = new ImageLoader(fileNames, imgRootPanel, ImageLoader.FULL_IMAGE_MODE,0);
		loader.addCompleteListener(new ThreadCompleteListener()
		{
			@Override
			public void notifyOfThreadComplete(Runnable thread)
			{
				if(((ImageLoader)thread).images.size() <= 0)
				{
					return;
				}
				if(((ImageLoader)thread).images.get(0).invalidFile)
				{
					disableImageMenuItems();
				}
				else
				{
					enableImageMenuItems();
				}
				imgRootPanel.removeAll();
				metaRootPanel.removeAll();
				imgRootPanel.setLayout(imgBorderLayout);
				metaRootPanel.setLayout(metaBorderLayout);
				imgRootPanel.add(((ImageLoader)thread).images.get(0).getRootComponent(), BorderLayout.CENTER);
				metaRootPanel.add(((ImageLoader)thread).metadata.get(0).getRootComponent(), BorderLayout.CENTER);
				currentPanel = ((ImageLoader)thread).images.get(0);
				revalidate();
				repaint();
				//loaderActive = false;
				if(nextToLoad != null)
				{
					fileList.selected(nextToLoad);
				}
			}
		});
		loader.addStatusListener(new ThreadStatusListener()
		{
			@Override
			public void statusChanged(Runnable thread, Object... info)
			{
				progress.setValue((progress.getValue() + 1));
			}
		});
		loader.setThreadName(THREAD_ID);
		PhotoArk.threadManager.registerThread(THREAD_ID, loader);
		
		createMenuBar();
		this.setJMenuBar(menuBar);

		File folder = new File(folderPath);
		if(folder.exists() && folder.isDirectory())
		{
			loadFromDirField(preSelectedIndex);
		}
		
		if((fileList.fileLabels.size() > preSelectedIndex) && (preSelectedIndex >= 0))
		{
			//fileList.fileLabels.get(preSelectedIndex).select();
		}
	}
	
	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		fileList.scrollDownToSelected();
		fileList.focus();
	}
	
	private void loadFromDirField(int selectedIndex)
	{
		File dir = new File(directoryField.getText());
		if(dir.exists())
		{
			fileList.setFileNames(dir.listFiles(), selectedIndex);
			isShowingResults = false;
		}
	}

	private int getNumberOfFiles()
	{
		if(fileList != null && fileList.fileLabels != null)
		{
			return fileList.fileLabels.size();
		}
		else
		{
			return 0;
		}
	}
	
	public void setFileListViewType(int viewType)
	{
		fileList.setViewType(viewType, fileListCR);
	}
	
	private void enableImageMenuItems()
	{
		for(JMenuItem m : imageMenuItems)
		{
			m.setEnabled(true);
		}
	}
	
	private void disableImageMenuItems()
	{
		for(JMenuItem m : imageMenuItems)
		{
			m.setEnabled(false);
		}
	}
	
	private void createMenuBar()
	{
		menuBar = new JMenuBar();
		
		JMenu file = new JMenu(" File ");
		JMenu view = new JMenu(" View ");
		JMenu help = new JMenu(" Help ");
		
		JMenuItem newWindow = new JMenuItem("New Viewer Window");
		newWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		newWindow.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				ImageViewer newFrame = new ImageViewer(new String[]{}, 0, System.getProperty("user.home"), false);
				newFrame.setVisible(true);
				PhotoArk.openFrames.add(newFrame);
			}
		});
		file.add(newWindow);

		JMenuItem newSearchWindow = new JMenuItem("New Search Window");
		newSearchWindow.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						SearchFrame newFrame = new SearchFrame();
						newFrame.setVisible(true);
						PhotoArk.openFrames.add(newFrame);
					}
				});
			}
		});
		file.add(newSearchWindow);
		
		file.addSeparator();
		
		JMenuItem addFilesToList = new JMenuItem("Add Files to List...");
		addFilesToList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				OpenFileChooser ofc = new OpenFileChooser(ImageViewer.this, PhotoArk.imageExtensions, "Image Files")
				{
					@Override
					public void chosen(Object obj)
					{
						File[] files = (File[])obj;
						fileList.addFiles(files);
					}
				};
				ofc.enableMultiSelection();
				ofc.showChooser();
			}
		});
		file.add(addFilesToList);

		JMenuItem addFolderToList = new JMenuItem("Add Folder to List...");
		addFolderToList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				DirectoryChooser ofc = new DirectoryChooser(ImageViewer.this)
				{
					@Override
					public void chosen(Object obj)
					{
						File folder = (File)obj;
						File[] files = OpenFileChooser.filterFiles(PhotoArk.imageExtensions, folder.listFiles());
						fileList.addFiles(files);
					}
				};
				ofc.showChooser();
			}
		});
		file.add(addFolderToList);
		
		file.addSeparator();

		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
		exit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				WindowEvent wev = new WindowEvent(ImageViewer.this, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
			}
		});
		file.add(exit);
		
		
		
		JRadioButtonMenuItem list = new JRadioButtonMenuItem("List");
		list.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				setFileListViewType(FileList.VIEW_LIST);
			}
		});
		
		JRadioButtonMenuItem small = new JRadioButtonMenuItem("Thumbnails - Small");
		small.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						fileList.setThumbnailSize(FileList.THUMBNAIL_SMALL);
						setFileListViewType(FileList.VIEW_THUMBNAIL);
					}
				});
			}
		});
		JRadioButtonMenuItem medium = new JRadioButtonMenuItem("Thumbnails - Medium");
		medium.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						fileList.setThumbnailSize(FileList.THUMBNAIL_MEDIUM);
						setFileListViewType(FileList.VIEW_THUMBNAIL);
					}
				});
			}
		});
		JRadioButtonMenuItem large = new JRadioButtonMenuItem("Thumbnails - Large");
		large.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						fileList.setThumbnailSize(FileList.THUMBNAIL_LARGE);
						setFileListViewType(FileList.VIEW_THUMBNAIL);
					}
				});
			}
		});
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(list);
		bg.add(small);
		bg.add(medium);
		bg.add(large);
		small.setSelected(true);
		
		view.add(list);
		view.add(small);
		view.add(medium);
		view.add(large);
		
		view.addSeparator();
		
		JMenuItem fullScreen = new JMenuItem("Full Screen");
		fullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		fullScreen.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				ExitListener el = new ExitListener()
				{
					@Override
					public void exited(Object obj)
					{
						int index = (Integer)obj;
						fileList.deselectAllFiles();
						fileList.selectedFileLabels.clear();
						fileList.tmpDeselected.clear();
						fileList.fileLabels.get(index).select();
						fileList.scrollUpToSelected();
						fileList.scrollDownToSelected();
					}
				};
				FullScreenViewer fsv = new FullScreenViewer(fileList.getAllFileNamesArray(), fileList.getAllFileNames().indexOf(fileList.getSelectedFileNamesArray()[0]));
				fsv.addExitListener(el);
				fsv.setVisible(true);
				PhotoArk.openFrames.add(fsv);
			}
		});
		view.add(fullScreen);
		
		view.addSeparator();
		
		JMenuItem zoomIn = new JMenuItem("Zoom In");
		zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK));
		zoomIn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomIn();
				}
			}
		});
		view.add(zoomIn);
		
		JMenuItem zoomOut = new JMenuItem("Zoom Out");
		zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK));
		zoomOut.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomOut();
				}
			}
		});
		view.add(zoomOut);
		
		JMenuItem zoomFit = new JMenuItem("Zoom to Fit");
		zoomFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0));
		this.imgRootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "SLASH");
		AbstractAction slashAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomToFit();
				}
			}
		};
		this.imgRootPanel.getActionMap().put("SLASH", slashAction);
		zoomFit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomToFit();
				}
			}
		});
		view.add(zoomFit);
		
		JMenuItem zoom1 = new JMenuItem("Zoom 1:1");
		zoom1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0));
		this.imgRootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.SHIFT_DOWN_MASK), "STAR");
		AbstractAction starAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomTo1();
				}
			}
		};
		this.imgRootPanel.getActionMap().put("STAR", starAction);
		zoom1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!ignoreKeyCommands)
				{
					currentPanel.zoomTo1();
				}
			}
		});
		view.add(zoom1);		
		
		imageMenuItems.add(zoomIn);
		imageMenuItems.add(zoomOut);
		imageMenuItems.add(zoomFit);
		imageMenuItems.add(zoom1);
		
		JMenuItem tutorial = new JMenuItem("Tutorial");
		tutorial.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		tutorial.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					Desktop.getDesktop().browse(new URI(PhotoArk.tutorialPageLocation));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		});
		help.add(tutorial);

		JMenuItem homePage = new JMenuItem("Home Page");
		homePage.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					Desktop.getDesktop().browse(new URI(PhotoArk.homePageLocation));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		});
		help.add(homePage);
		
		JMenuItem forgetLicenses = new JMenuItem("Forget Licenses");
		forgetLicenses.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				int result = JOptionPane.showConfirmDialog(ImageViewer.this, "Are you sure you want to forget your licenses?\nDoing so will immediately deactivate and close this program making it unusable until you provide another valid license.", "Confirm Forget Licenses", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION)
				{
					try
					{
						PhotoArk.licenseManager.clearPrefs();
						PhotoArk.exitApp();
					}
					catch (BackingStoreException e)
					{
						JOptionPane.showMessageDialog(ImageViewer.this, "An error occured attempting to forget your licenses.", "Error Forgetting Licenses", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		help.add(forgetLicenses);
		
		JMenuItem about = new JMenuItem("About...");
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				ad.setVisible(true);
			}
		});
		help.add(about);
		
		menuBar.add(file);
		menuBar.add(view);
		menuBar.add(help);
	}
}