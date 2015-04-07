package com.lightark.photoark.imagesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import com.lightark.FileUtils.DirectoryChooser;
import com.lightark.photoark.AboutDialog;
import com.lightark.photoark.PhotoArk;

public class SearchFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//public final static File tempDir = new File(System.getProperty("user.home") + "\\." + PhotoArk.appName + "_Temp_1qw23er45ty67ui89op0");
	
	public SearchPanel searchPanel;
	
	private JPanel dirPanel;
	private JTextField directoryField;
	private JPanel dirButtonPanel;
	
	private Search search;

	public AboutDialog ad;
	
	private SearchMenuBar menuBar;

	public SearchFrame()
	{	
		this.setSize(500,500);
		this.setTitle(PhotoArk.appName + " - Search");
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);

		ToolTipManager.sharedInstance().setInitialDelay(100);
		
		ad = new AboutDialog(this);
		
		search = new Search(new File(System.getProperty("user.home")));
		
		searchPanel = new SearchPanel(search, this);
		
		dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());
		dirPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 20));
		
		JPanel browsePanel = new JPanel();
		browsePanel.setLayout(new BorderLayout());
		browsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		
		JButton browse = new JButton("BROWSE...");
		browse.setFont(new Font("Comic Sans MS",Font.PLAIN,16));
		browse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				DirectoryChooser dc = new DirectoryChooser(SearchFrame.this, new File(getDirectory()))
				{
					@Override
					public void chosen(Object obj)
					{
						directoryField.setText(((File)obj).getAbsolutePath());
						KeyEvent evt = new KeyEvent(directoryField,KeyEvent.KEY_RELEASED,0,0,KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
						directoryField.dispatchEvent(evt);
					}
				};
				dc.showChooser();
			}
		});
		browsePanel.add(browse, BorderLayout.CENTER);
		
		dirPanel.add(browsePanel, BorderLayout.LINE_START);
		
		directoryField = new JTextField(search.location.getAbsolutePath());
		directoryField.setCaretPosition(directoryField.getText().length());
		directoryField.setFont(new Font("Arial",Font.PLAIN,25));
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
						src.setForeground(Color.red);
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
					JTextField src = (JTextField) evt.getSource();
					src.selectAll();
				}
			}
		});
		directoryField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				searchPanel.actionPerformed(null);
			}
		});
		
		
		dirPanel.add(directoryField, BorderLayout.CENTER);
		
		dirButtonPanel = new JPanel();
		JCheckBox includeSubDirs = new JCheckBox("Include Sub-Directories");
		includeSubDirs.setSelected(true);
		includeSubDirs.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(evt.getSource() instanceof JCheckBox)
				{
					JCheckBox src = (JCheckBox) evt.getSource();
					search.includeSubDirs = src.isSelected();
				}
			}
		});
		
		dirButtonPanel.add(includeSubDirs);
		
		dirPanel.add(dirButtonPanel, BorderLayout.LINE_END);
		
		Container pane = this.getContentPane();
		pane.add(dirPanel,BorderLayout.PAGE_START);
		pane.add(searchPanel, BorderLayout.CENTER);
		
		this.getRootPane().addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent evt)
			{
				
			}
		});
		
		/*if(!MainFrame.tempDir.exists())
		{
			MainFrame.tempDir.mkdir();
		}*/
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				PhotoArk.openFrames.remove(SearchFrame.this);
				if(PhotoArk.openFrames.size() <= 0)
				{
					PhotoArk.exitApp();
				}
				dispose();
				PhotoArk.threadManager.terminateThreadsForKey(Search.THREAD_ID);
			}
		});
		
		menuBar = new SearchMenuBar(this);

		this.setJMenuBar(menuBar);
	}
	
	public String getDirectory()
	{
		return directoryField.getText();
	}
}