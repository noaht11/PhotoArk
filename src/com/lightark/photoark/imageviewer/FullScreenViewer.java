package com.lightark.photoark.imageviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.lightark.FileUtils.FileNames;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.photoark.ImageLoader;
import com.lightark.photoark.PhotoArk;

interface ExitListener
{
	public void exited(Object obj);
}

public class FullScreenViewer extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String THREAD_ID = "FULL_SCREEN_VIEWER_IMAGE_LOADER";
	
	private ArrayList<ExitListener> listeners = new ArrayList<ExitListener>();
	
	private LayoutManager imgBorderLayout = new BorderLayout();
	private LayoutManager imgGridBagLayout = new GridBagLayout();
	
	private ImagePanel currentPanel = null;
	
	//public boolean loaderActive = false;
	private ImageLoader loader;
	//private Thread loaderThread;
	private JLabel imgLoadingLabel;
	private String nextToLoad;
	
	private int screenWidth = 0;
	private int screenHeight = 0;
	
	private JPanel rootPanel;
	private JPanel imgRootPanel;
	
	int currentIndex = 0;
	private final String[] filePaths;
	
	public FullScreenViewer(String[] _filePaths, int preSelectedIndex)
	{
		this.filePaths = FileNames.verifyExistence(_filePaths);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		this.setSize(screenWidth, screenHeight);
		this.setUndecorated(true);
		this.setTitle(new File(filePaths[preSelectedIndex]).getName());

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				PhotoArk.openFrames.remove(FullScreenViewer.this);
				if(PhotoArk.openFrames.size() <= 0)
				{
					PhotoArk.exitApp();
				}
				dispose();
				PhotoArk.threadManager.terminateThreadsForKey(THREAD_ID);
			}
		});
		
		this.setLayout(new BorderLayout());

		imgLoadingLabel = new JLabel("Loading...");
		imgLoadingLabel.setForeground(Color.white);
		
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setBackground(Color.black);
		
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		Action escape = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
                WindowEvent wev = new WindowEvent(FullScreenViewer.this, WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
				for(ExitListener el : listeners)
				{
					el.exited(currentIndex);
				}
			}
		};
		rootPanel.getActionMap().put("ESCAPE", escape);
		
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		Action left = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				navLeft();
			}
		};
		rootPanel.getActionMap().put("LEFT", left);
		
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		Action right = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				navRight();
			}
		};
		rootPanel.getActionMap().put("RIGHT", right);
		
		AbstractAction zoomFitAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				currentPanel.zoomToFit();
			}
		};
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "SLASH");
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0), "SLASH");
		rootPanel.getActionMap().put("SLASH", zoomFitAction);

		AbstractAction zoom1Action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				currentPanel.zoomTo1();
			}
		};
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.SHIFT_DOWN_MASK), "STAR");
		rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), "STAR");
		rootPanel.getActionMap().put("STAR", zoom1Action);
		
		
		this.add(rootPanel, BorderLayout.CENTER);
		

		imgRootPanel = new JPanel();
		imgRootPanel.setBackground(Color.black);
		imgRootPanel.setLayout(imgBorderLayout);
		
		rootPanel.add(imgRootPanel, BorderLayout.CENTER);
		
		loader = new ImageLoader(filePaths, imgRootPanel, ImageLoader.FULL_IMAGE_MODE,0);
		loader.addCompleteListener(new ThreadCompleteListener()
		{
			@Override
			public void notifyOfThreadComplete(Runnable thread)
			{
				if(((ImageLoader)thread).images.size() <= 0)
				{
					return;
				}
				imgRootPanel.removeAll();
				imgRootPanel.setLayout(imgBorderLayout);
				currentPanel = ((ImageLoader)thread).images.get(0);
				currentPanel.setBackground(Color.black);
				currentPanel.setStringPaintingEnabled(true, Color.white);
				imgRootPanel.add(currentPanel.getRootComponent(), BorderLayout.CENTER);
				revalidate();
				repaint();
				//loaderActive = false;
				setTitle(new File(filePaths[currentIndex]).getName());
				if(nextToLoad != null)
				{
					loadImage(nextToLoad);
				}
			}
		});
		loader.setThreadName(THREAD_ID);
		PhotoArk.threadManager.registerThread(THREAD_ID, loader);
		
		loadImage(preSelectedIndex);
	}
	
	public void loadImage(int index)
	{
		if(index < 0 || index >= filePaths.length)
		{
			return;
		}
		loadImage(filePaths[index]);
	}
	
	public void loadImage(String filePath)
	{
		currentIndex = Arrays.asList(filePaths).indexOf(filePath);
		/*if(!loaderActive)
		{	
			imgRootPanel.removeAll();
			imgRootPanel.setLayout(imgGridBagLayout);
			imgRootPanel.add(imgLoadingLabel);
			imgRootPanel.revalidate();
			imgRootPanel.repaint();
			
			loaderActive = true;
			nextToLoad = null;
			loader.reset();
			loader.setFileNames(new String[]{filePath});
			loaderThread = new Thread(loader);
			loaderThread.start();
		}
		else
		{
			nextToLoad = filePath;
		}*/
		if(!loader.isActive())
		{	
			imgRootPanel.removeAll();
			imgRootPanel.setLayout(imgGridBagLayout);
			imgRootPanel.add(imgLoadingLabel);
			imgRootPanel.revalidate();
			imgRootPanel.repaint();
			
			nextToLoad = null;
			loader.reset();
			loader.setFileNames(new String[]{filePath});
			loader.startThread();
		}
		else
		{
			nextToLoad = filePath;
		}
	}
	
	public void navLeft()
	{
		int newIndex = currentIndex - 1;
		if(newIndex >= 0)
		{
			loadImage(newIndex);
		}
	}
	
	public void navRight()
	{
		int newIndex = currentIndex + 1;
		if(newIndex < filePaths.length)
		{
			loadImage(newIndex);
		}
	}
	
	public void addExitListener(ExitListener el)
	{
		listeners.add(el);
	}
	
	public void removeExitListener(ExitListener el)
	{
		listeners.remove(el);
	}
}