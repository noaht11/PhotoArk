package com.lightark.photoark;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lightark.photoark.imagesearch.SearchFrame;
import com.lightark.photoark.imageviewer.ImageViewer;

public class LaunchFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LaunchFrame()
	{
		this.setUndecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		this.setSize(450, 500);
		this.setResizable(false);
		this.setTitle(PhotoArk.appName);
		
		Color bgColor = new Color(0,150,255);
		
		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setBackground(bgColor);
		
		JPanel windowButtons = new JPanel();
		windowButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		windowButtons.setOpaque(false);
		
		JButton minimize = new JButton("<html><b>_</b></html>");
		minimize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		minimize.setBorder(null);
		minimize.setContentAreaFilled(false);
		minimize.setFocusable(false);
		minimize.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				((JButton)evt.getSource()).setForeground(Color.red);
			}
			
			@Override
			public void mouseExited(MouseEvent evt)
			{
				((JButton)evt.getSource()).setForeground(Color.black);
			}
		});
		minimize.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				LaunchFrame.this.setState(JFrame.ICONIFIED);
			}
		});
		windowButtons.add(minimize);
				
		JButton exit = new JButton("<html><b>X</b></html>");
		exit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		exit.setBorder(null);
		exit.setContentAreaFilled(false);
		exit.setFocusable(false);
		exit.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				((JButton)evt.getSource()).setForeground(Color.red);
			}
			
			@Override
			public void mouseExited(MouseEvent evt)
			{
				((JButton)evt.getSource()).setForeground(Color.black);
			}
		});
		exit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}
		});
		windowButtons.add(exit);
		
		rootPanel.add(windowButtons, BorderLayout.PAGE_START);
				
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.setBackground(bgColor);
		
		JLabel titleBar = new JLabel("PhotoArk", new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_128.png")), JLabel.CENTER);
		titleBar.setFont(new Font("Comic Sans MS", Font.BOLD, 55));
		titleBar.setHorizontalTextPosition(JLabel.RIGHT);
		titleBar.setIconTextGap(30);
		titleBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));
		titleBar.setBackground(bgColor);
		
		content.add(titleBar, BorderLayout.PAGE_START);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,0));
				
		ActionListener searchAction = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SearchFrame newFrame = new SearchFrame();
				newFrame.setVisible(true);
				PhotoArk.openFrames.add(newFrame);
				dispose();
			}
		};
		LaunchButton launchSearch = new LaunchButton("Search", new ImageIcon(ResourceLoader.loadResource("Resources/Launch/Search_150.png")), searchAction);
		launchSearch.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 1, Color.black));
		
		ActionListener viewAction = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				ImageViewer iv = new ImageViewer(new String[]{},0, System.getProperty("user.home"), false);
				iv.setVisible(true);
				PhotoArk.openFrames.add(iv);
				dispose();
			}
		};
		LaunchButton launchView = new LaunchButton("View", new ImageIcon(ResourceLoader.loadResource("Resources/Launch/View_150.png")), viewAction);
		launchView.setBorder(BorderFactory.createMatteBorder(2, 1, 0, 0, Color.black));
		
		buttonPanel.add(launchSearch);
		buttonPanel.add(launchView);
		
		content.add(buttonPanel, BorderLayout.CENTER);
		
		rootPanel.add(content, BorderLayout.CENTER);
		
		this.add(rootPanel);
		this.setBackground(bgColor);
		this.setLocationRelativeTo(null);
	}
	
	static class LaunchButton extends JButton
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LaunchButton(String text, ImageIcon image, ActionListener al)
		{			
			super(text, image);

			Font buttonFont = new Font("Comic Sans MS", Font.BOLD, 40);
			
			this.setHorizontalTextPosition(JButton.CENTER);
			this.setVerticalTextPosition(JButton.TOP);
			this.setIconTextGap(20);
			this.setFont(buttonFont);
			this.setFocusable(false);
			this.setContentAreaFilled(false);
			this.setOpaque(true);
			setBackground(new Color(230,230,230));
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.addActionListener(al);
			
			this.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseEntered(MouseEvent evt)
				{
					setBackground(new Color(0,50,255));
					setForeground(Color.white);
				}

				@Override
				public void mouseExited(MouseEvent evt)
				{
					setBackground(new Color(230,230,230));
					setForeground(Color.black);
				}
			});
		}
	}
}