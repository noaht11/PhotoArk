package com.lightark.photoark.imageviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.lightark.FileUtils.FileNames;
import com.lightark.MathUtils.Factors;
import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.Thread.ThreadStatusListener;
import com.lightark.photoark.ImageLoader;
import com.lightark.photoark.PhotoArk;
import com.lightark.photoark.ResourceLoader;

public class CompareWindow extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String THREAD_ID = "COMPARE_WINDOW_IMAGE_LOADER";
	
	private String[] filePaths;
	
	private JPanel[] rootPanels;
	private ImagePanel[] imagePanels;
	
	//public boolean loaderActive = false;
	private ImageLoader loader;
	//private Thread loaderThread;
	
	private JMenuBar menuBar;
	private JCheckBoxMenuItem displayInfo;

	public CompareWindow(String[] filePaths)
	{
		this.filePaths = FileNames.verifyExistence(filePaths);
		
		this.setSize(500, 500);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		this.setTitle((PhotoArk.appName + " - " + "Compare"));
		this.getContentPane().setBackground(Color.white);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				PhotoArk.openFrames.remove(CompareWindow.this);
				if(PhotoArk.openFrames.size() <= 0)
				{
					PhotoArk.exitApp();
				}
				dispose();
				PhotoArk.threadManager.terminateThreadsForKey(THREAD_ID);
			}
		});
		
		if(filePaths.length <= 1)
		{
			return;
		}
		
		createComparePanels();
		
		menuBar = new JMenuBar();
		
		JMenu view = new JMenu("View");
		
		displayInfo = new JCheckBoxMenuItem("Display Image Info");
		displayInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
		displayInfo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(displayInfo.isSelected())
				{
					for(ImagePanel ip : imagePanels)
					{
						ip.setStringPaintingEnabled(true, Color.black);
					}
				}
				else
				{
					for(ImagePanel ip : imagePanels)
					{
						ip.setStringPaintingEnabled(false, null);
					}
				}
			}
		});
		view.add(displayInfo);

		view.addSeparator();
		
		JMenuItem zoomAllFit = new JMenuItem("Zoom All to Fit");
		AbstractAction zoomAllFitAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				for(ImagePanel ip : imagePanels)
				{
					ip.zoomToFit();
				}
			}
		};
		zoomAllFit.addActionListener(zoomAllFitAction);
		zoomAllFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0));
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "SLASH");
		this.getRootPane().getActionMap().put("SLASH", zoomAllFitAction);
		view.add(zoomAllFit);
		
		JMenuItem zoomAll1 = new JMenuItem("Zoom All 1:1");
		AbstractAction zoomAll1Action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				for(ImagePanel ip : imagePanels)
				{
					ip.zoomTo1();
				}
			}
		};
		zoomAll1.addActionListener(zoomAll1Action);
		zoomAll1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0));
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.SHIFT_DOWN_MASK), "STAR");
		this.getRootPane().getActionMap().put("STAR", zoomAll1Action);
		view.add(zoomAll1);
		
		menuBar.add(view);
		
		this.setJMenuBar(menuBar);
	}
	
	private int getGridCols(int panels)
	{
		List<Long> nearestFactors = Factors.getNearestFactors(panels);
		if(nearestFactors.get(0).longValue() == 1)
		{
			if(panels % 2 != 0)
			{
				panels = panels + 1;
				nearestFactors = Factors.getNearestFactors(panels);
			}
		}
		boolean windowIsWide = getWidth() >= getHeight();
		if(windowIsWide)
		{
			return (int) Math.max(nearestFactors.get(0), nearestFactors.get(1));
		}
		else
		{
			return (int) Math.min(nearestFactors.get(0), nearestFactors.get(1));
		}
	}
	
	private void createComparePanels()
	{
		int cols = getGridCols(filePaths.length);
		
		this.setLayout(new GridLayout(0,cols,10,10));
		
		imagePanels = new ImagePanel[filePaths.length];
		rootPanels = new JPanel[filePaths.length];
		ImageIcon loading = new ImageIcon(ResourceLoader.loadResource("Resources/loading_50.gif"));
		for(int i = 0;i < filePaths.length;i++)
		{
			JPanel theRootPanel = new JPanel();
			theRootPanel.setLayout(new BorderLayout());
			theRootPanel.setBackground(Color.white);
			
			JLabel imgLoadingLabel = new JLabel("",loading, JLabel.CENTER);
			imgLoadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
			imgLoadingLabel.setHorizontalTextPosition(JLabel.CENTER);
			theRootPanel.add(imgLoadingLabel, BorderLayout.CENTER);
			
			JLabel fileName = new JLabel(new File(filePaths[i]).getName());
			fileName.setHorizontalAlignment(JLabel.CENTER);
			theRootPanel.add(fileName, BorderLayout.PAGE_START);
			
			this.add(theRootPanel);
			
			rootPanels[i] = theRootPanel;
		}
		
		loader = new ImageLoader(filePaths, rootPanels[0], ImageLoader.FULL_IMAGE_MODE,0);
		loader.addStatusListener(new ThreadStatusListener()
		{
			@Override
			public void statusChanged(final Runnable thread, final Object... info)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						int index = (Integer)info[1];
						ImagePanel theImage = ((ImageLoader)thread).images.get(index);
						imagePanels[index] = theImage;
						rootPanels[index].remove(0);
						rootPanels[index].add(theImage.getRootComponent(), BorderLayout.CENTER);
						revalidate();
						repaint();
					}
				});
			}
		});
		loader.addCompleteListener(new ThreadCompleteListener()
		{
			@Override
			public void notifyOfThreadComplete(Runnable thread)
			{
				
			}
		});
		loader.setThreadName(THREAD_ID);
		PhotoArk.threadManager.registerThread(THREAD_ID, loader);
		
		//loaderActive = true;
		loader.reset();
		loader.setFileNames(filePaths);
		//loaderThread = new Thread(loader);
		//loaderThread.start();
		loader.startThread();
	}
}