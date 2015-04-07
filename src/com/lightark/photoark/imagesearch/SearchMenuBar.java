package com.lightark.photoark.imagesearch;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.BackingStoreException;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.lightark.FileUtils.DirectoryChooser;
import com.lightark.FileUtils.FileTransfers;
/*import com.lightark.FileUtils.OpenFileChooser;
import com.lightark.FileUtils.SaveFileChooser;*/
import com.lightark.photoark.ExportDialog;
import com.lightark.photoark.OpenDialog;
import com.lightark.photoark.PhotoArk;
import com.lightark.photoark.filelist.FileList;
import com.lightark.photoark.imageviewer.ImageViewer;

public class SearchMenuBar extends JMenuBar
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SearchFrame frame;
	
	private JMenu file = new JMenu(" File ");
	//private JMenu edit = new JMenu("Edit ");
	//private JMenu view = new JMenu("View ");
	private JMenu criteria = new JMenu(" Criteria ");
	private JMenu fileList = new JMenu(" Results ");
	private JMenu help = new JMenu(" Help");

	public ExportDialog fileListExport;
	public OpenDialog fileListOpen;
	
	public SearchMenuBar(SearchFrame _frame)
	{
		this.frame = _frame;
		
		createFileMenu();
		createCriteriaMenu();
		createFileListMenu();
		createHelpMenu();
		
		add(file);
		//add(edit);
		//add(view);
		add(criteria);
		add(fileList);
		add(help);
	}
	
	private void createFileMenu()
	{
		JMenuItem newWindow = new JMenuItem("New Search Window");
		newWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		newWindow.addActionListener(new ActionListener()
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
		file.add(newWindow);
		
		JMenuItem launchViewer = new JMenuItem("New Viewer Window");
		launchViewer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						ImageViewer iv = new ImageViewer(new String[]{},0, System.getProperty("user.home"), false);
						iv.setVisible(true);
						PhotoArk.openFrames.add(iv);
					}
				});
			}
		});
		file.add(launchViewer);
		
		//file.addSeparator();
		
		/*JMenuItem openSearchCriteria = new JMenuItem("Open Search Criteria...");
		openSearchCriteria.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		openSearchCriteria.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				OpenFileChooser sfc = new OpenFileChooser(frame, ".ims", "Image Metadata Search Files (*.ims)")
				{
					@Override
					public void chosen(Object obj)
					{
						if(frame.searchPanel.criteriaPanel.panel.getComponentCount() > 0)
						{
							int result = JOptionPane.showConfirmDialog(frame, "Would you like to merge the criteria from the selected file with your existing criteria? If not, your existing criteria will be deleted.", "Merge Criteria", JOptionPane.YES_NO_OPTION);
							if(result == JOptionPane.NO_OPTION)
							{
								frame.searchPanel.criteriaPanel.removeAll();
							}
							else if(result == JOptionPane.CLOSED_OPTION);
							{
								return;
							}
						}
						CriteriaSerializer cs = CriteriaSerializer.readFromFile((File)obj);
						frame.searchPanel.criteriaPanel.loadFromCriteriaSerializer(cs);
					}
				};
				sfc.showChooser();
			}
		});
		file.add(openSearchCriteria);*/
		
		/*JMenuItem saveSearchCriteria = new JMenuItem("Save Search Criteria...");
		saveSearchCriteria.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		saveSearchCriteria.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SaveFileChooser sfc = new SaveFileChooser(frame, ".ims", "Image Metadata Search Files (*.ims)", "Search1.ims")
				{
					@Override
					public void chosen(Object obj)
					{
						CriteriaSerializer cs = new CriteriaSerializer(frame.searchPanel.criteriaPanel.getCriteriaList(), frame.searchPanel.criteriaPanel.generateOperatorList());
						CriteriaSerializer.writeToFile(cs, ((File)obj));
					}
				};
				sfc.showChooser();
				
			}
		});
		file.add(saveSearchCriteria);*/
		
		file.addSeparator();
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
		exit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
			}
		});
		file.add(exit);
	}
	
	private void createCriteriaMenu()
	{
		JMenu newCrit = new JMenu("New Criteria");	
		
		for(SearchCriterion sc : PhotoArk.criteriaExtensions)
		{
			CriteriaExtensionMenuItem menuItem = new CriteriaExtensionMenuItem(sc, frame.searchPanel.criteriaPanel);
			newCrit.add(menuItem);
		}
		
		criteria.add(newCrit);
		
		JMenuItem clear = new JMenuItem("Clear all criteria");
		clear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				frame.searchPanel.criteriaPanel.removeAll();
			}
		});
		criteria.add(clear);
	}
	
	private void createFileListMenu()
	{
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		refresh.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				frame.searchPanel.refreshFileList();
			}
		});
		fileList.add(refresh);
		
		fileList.addSeparator();
		
		JMenu view = new JMenu("View");
		
		JRadioButtonMenuItem list = new JRadioButtonMenuItem("List");
		list.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				frame.searchPanel.setFileListViewType(FileList.VIEW_LIST);
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
						frame.searchPanel.fileList.setThumbnailSize(FileList.THUMBNAIL_SMALL);
						frame.searchPanel.setFileListViewType(FileList.VIEW_THUMBNAIL);
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
						frame.searchPanel.fileList.setThumbnailSize(FileList.THUMBNAIL_MEDIUM);
						frame.searchPanel.setFileListViewType(FileList.VIEW_THUMBNAIL);
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
						frame.searchPanel.fileList.setThumbnailSize(FileList.THUMBNAIL_LARGE);
						frame.searchPanel.setFileListViewType(FileList.VIEW_THUMBNAIL);
					}
				});
			}
		});
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(list);
		bg.add(small);
		bg.add(medium);
		bg.add(large);
		list.setSelected(true);
		
		view.add(list);
		view.add(small);
		view.add(medium);
		view.add(large);
		fileList.add(view);

		fileList.addSeparator();
		
		fileListExport = new ExportDialog(frame, "Export", true);

		JMenuItem export = new JMenuItem("Export...");
		export.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				fileListExport.setVisible(true);
			}
		});
		fileList.add(export);


		fileListOpen = new OpenDialog(frame);
		
		JMenuItem open = new JMenuItem("Open Files...");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				fileListOpen.setVisible(true);
			}
		});
		fileList.add(open);
		
		fileList.addSeparator();
		
		JMenuItem copyFiles = new JMenuItem("Copy files to folder...");
		copyFiles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				DirectoryChooser dc = new DirectoryChooser(frame)
				{
					@Override
					public void chosen(Object obj)
					{
						File folder = (File)obj;
						File[] files = new File[frame.searchPanel.fileList.fileLabels.size()];
						for(int i = 0;i < frame.searchPanel.fileList.fileLabels.size();i++)
						{
							files[i] = frame.searchPanel.fileList.fileLabels.get(i).getFile();
						}
						FileTransfers.Copy copy = new FileTransfers.Copy(files, folder, frame, true);
						copy.startCopy();
					}
				};
				dc.showChooser();
			}
		});
		fileList.add(copyFiles);
	}
	
	private void createHelpMenu()
	{
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
				int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to forget your licenses?\nDoing so will immediately deactivate and close this program making it unusable until you provide another valid license.", "Confirm Forget Licenses", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION)
				{
					try
					{
						PhotoArk.licenseManager.clearPrefs();
						PhotoArk.exitApp();
					}
					catch (BackingStoreException e)
					{
						JOptionPane.showMessageDialog(frame, "An error occured attempting to forget your licenses.", "Error Forgetting Licenses", JOptionPane.ERROR_MESSAGE);
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
				frame.ad.setVisible(true);
			}
		});
		help.add(about);
	}
}