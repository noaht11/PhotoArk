package com.lightark.FileUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

public class CopyDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static int ASK = -1;
	public final static int COPY_AND_REPLACE = 0;
	public final static int COPY_AND_RENAME = 1;
	public final static int DONT_COPY = 2;

	private Component componentBelow;
	
	private JLabel title;
	
	private JPanel details;
	private JLabel name;
	private JLabel from;
	private JLabel to;
	private JLabel remain;
	private JProgressBar progressBar;
	
	private JPanel buttonPanel;
	private JButton cancel;
	private JButton cancelImm;
	
	private int remaining = 0;
	
	public CopyDialog(Component componentBelow, List<Image> icons, File source, File target, int numberOfFiles, FileSize fileSize)
	{
		this.componentBelow = componentBelow;
		
		String copyTitle = ("Copying " + numberOfFiles + " items" + " (" + fileSize.asString(2) + ")");
		this.setTitle(copyTitle);
		this.setSize(500, 325);
		this.setLocationRelativeTo(componentBelow);
		this.setIconImages(icons);
		this.setResizable(false);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.setLayout(new BorderLayout());
		
		remaining = numberOfFiles;
		
		title = new JLabel(copyTitle);
		title.setFont(new Font("Tahoma", Font.BOLD, 20));
		title.setOpaque(true);
		title.setBackground(new Color(0,150,255));
		title.setBorder(BorderFactory.createEmptyBorder(15,25,15,10));
		this.add(title, BorderLayout.PAGE_START);

		
		Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		JPanel detailsContainer = new JPanel();
		detailsContainer.setBackground(Color.white);
		detailsContainer.setLayout(new BorderLayout());
		
		details = new JPanel();
		details.setBackground(Color.white);
		details.setLayout(new BorderLayout());
		details.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 25));
		
		JPanel column1 = new JPanel();
		column1.setBackground(Color.white);
		column1.setLayout(new GridLayout(0,1));
		
		JPanel column2 = new JPanel();
		column2.setBackground(Color.white);
		column2.setLayout(new GridLayout(0,1));
		
		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setBorder(border);
		column1.add(nameLabel);
		
		name = new JLabel("PIE"/*files[0].getName()*/);
		name.setBorder(border);
		column2.add(name);
		
		if(source != null)
		{
			JLabel fromLabel = new JLabel("From:");
			fromLabel.setBorder(border);
			column1.add(fromLabel);
			
			from = new JLabel();
			updateFrom(source);
			from.setBorder(border);
			column2.add(from);
		}
		
		if(target != null)
		{
			JLabel toLabel = new JLabel("To:");
			toLabel.setBorder(border);
			column1.add(toLabel);
			
			to = new JLabel();
			updateTo(target);
			to.setBorder(border);
			column2.add(to);
		}
		
		JLabel remainLabel = new JLabel("Items Remaining:");
		remainLabel.setBorder(border);
		column1.add(remainLabel);
		
		remain = new JLabel(Integer.toString(remaining) + " (" + fileSize.asString(2) + ")");
		remain.setBorder(border);
		column2.add(remain);
		
		details.add(column1, BorderLayout.LINE_START);
		details.add(column2, BorderLayout.CENTER);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setBackground(Color.white);
		progressPanel.setLayout(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0,25,15,25));
		
		progressBar = new JProgressBar(0,numberOfFiles);
		progressBar.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		progressBar.setValue(0);
		
		progressPanel.add(progressBar, BorderLayout.CENTER);
		
		detailsContainer.add(details, BorderLayout.CENTER);
		detailsContainer.add(progressPanel, BorderLayout.PAGE_END);
		
		this.add(detailsContainer, BorderLayout.CENTER);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		cancel = new JButton("Cancel After File");
		cancel.setToolTipText("Cancels the copy after the current file has finished copying (ensures no corrupted files are created)");
		cancel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		cancelImm = new JButton("Cancel Immediately");
		cancelImm.setToolTipText("Cancels the copy immediately (may create corrupted files)");
		cancelImm.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		buttonPanel.add(cancel);
		buttonPanel.add(cancelImm);
		
		this.add(buttonPanel, BorderLayout.PAGE_END);
	}
	
	public int[] showFileExistsDialog(String fileName, String targetDir, String rename)
	{
		String message = "The file \"" + fileName + "\" already exists in the destination folder \"" + targetDir + "\".\nPlease select how you would like to handle this conflict.";
		JCheckBox check = new JCheckBox("Do this for all following conflicts");
		int result = JOptionPane.showOptionDialog(this, new Object[]{message, check}, "File Exists", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Copy and Replace", "Copy and Rename", "Don't Copy"}, 0);
		boolean showAgain = check.isSelected();
		int showAgainInt = 0;
		if(showAgain)
		{
			showAgainInt = 1;
		}
		return new int[]{result, showAgainInt};
	}
	
	public Component getComponentBelow()
	{
		return componentBelow;
	}
	
	public void setProgressBarMax(int max)
	{
		if(max > 0)
		{
			progressBar.setMaximum(max);
		}
	}
	
	public void updateFrom(File newFrom)
	{
		from.setText(newFrom.getName() + " (" + newFrom.getAbsolutePath() + ")");
	}
	
	public void updateTo(File newTo)
	{
		to.setText(newTo.getName() + " (" + newTo.getAbsolutePath() + ")");
	}
	
	public void updateCurrentFile(String fileName)
	{
		name.setText(fileName);
		this.revalidate();
		this.repaint();
	}
	
	public void updateNumberRemaining(int amountToAdd, FileSize newSizeRemaining)
	{
		remaining += amountToAdd;
		remain.setText(Integer.toString(remaining) + " (" + newSizeRemaining.asString(2) + ")");
		this.revalidate();
		this.repaint();
	}
	
	public void updateProgressBar(int amountToAdd)
	{
		progressBar.setValue(progressBar.getValue() + amountToAdd);
		this.revalidate();
		this.repaint();
	}
	
	public int getProgressValue()
	{
		return progressBar.getValue();
	}
	
	public int getNumberRemaining()
	{
		return remaining;
	}
	
	public void addCancelListener(ActionListener al)
	{
		cancel.addActionListener(al);
	}
	
	public void removeCancelListener(ActionListener al)
	{
		cancel.removeActionListener(al);
	}

	
	public void addImmCancelListener(ActionListener al)
	{
		cancelImm.addActionListener(al);
	}
	
	public void removeImmCancelListener(ActionListener al)
	{
		cancelImm.removeActionListener(al);
	}
}