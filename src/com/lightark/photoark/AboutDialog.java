package com.lightark.photoark;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel basePanel = new JPanel();
	private JLabel logoLabel;
	
	private JPanel contentPanel = new JPanel();
	private JLabel titleLabel = new JLabel();
	private JPanel flowContainer = new JPanel();
	private JLabel contentLabel = new JLabel();
	
	private JFrame frame;
	
	public AboutDialog(JFrame frame)
	{
		super(frame,("About " + PhotoArk.appName));
		this.frame = frame;
		this.setSize(710, 380);
		this.setLocationRelativeTo(frame);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		basePanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
		basePanel.setLayout(new BorderLayout());
		
		ImageIcon logo = new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_256.png"));
		logoLabel = new JLabel(logo);
		
		basePanel.add(logoLabel, BorderLayout.LINE_START);
				
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 35, 0, 0));
		
		titleLabel.setText(PhotoArk.appName);
		titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
		contentPanel.add(titleLabel, BorderLayout.PAGE_START);
		
		String updateString = "";
		String spacing = "&nbsp&nbsp&nbsp&nbsp&nbsp";
		if(PhotoArk.updateCheckFailed)
		{
			updateString = spacing + "<i>Could not check for updates</i>";
		}
		else if(PhotoArk.updateAvailable)
		{
			updateString = spacing + "<i>Updates are available for download</i>";
		}
		else
		{
			updateString = spacing + "<i>No updates available</i>";
		}
		
		contentLabel.setText(
				"<html>" +
				PhotoArk.version + updateString +
				"<br><br>" +
				PhotoArk.appName +
				" is a java application for viewing images<br>and their metadata as well as searching images by<br>metadata tags." +
				"<br><br>" +
				"The metadata is accessed using<br>Drew Noakes' Metadata-Extractor library." +
				"<br><br>" +
				"Copyright &copy; Noah Tajwar 2015. All rights reserved." +
				"</html>");
		contentLabel.setFont(new Font("Calibri", Font.PLAIN, 17));
		contentLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		flowContainer.add(contentLabel);
		contentPanel.add(flowContainer, BorderLayout.CENTER);
		
		
		basePanel.add(contentPanel, BorderLayout.CENTER);
		
		this.getContentPane().add(basePanel, BorderLayout.LINE_START);
	}
	
	@Override
	public void setVisible(boolean b)
	{
		this.setLocationRelativeTo(frame);
		super.setVisible(true);
	}
	
}