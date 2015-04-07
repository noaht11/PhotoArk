package com.lightark.photoark.imagesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.lightark.Thread.ThreadCompleteListener;
import com.lightark.photoark.ComponentReplacer;
import com.lightark.photoark.ImageFinder;
import com.lightark.photoark.ProgressListener;
import com.lightark.photoark.filelist.FileList;

public class SearchPanel extends JPanel implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel fileListPanelContainer;
	private JLabel fileListLabel;
	public FileList fileList;
	private ComponentReplacer fileListCR;

	private JPanel progressPanel;
	private JProgressBar progress;
	private JButton progressCancel;
	
	public SearchCriteriaPanel criteriaPanel;
	
	private JButton searchButton;
	
	private Search search;
	
	private SearchFrame frame;

	public SearchPanel(Search _search, SearchFrame frame)
	{
		this.setLayout(new GridLayout(1,2));
		
		this.search = _search;
		this.frame = frame;

		
		fileListPanelContainer = new JPanel();
		fileListPanelContainer.setLayout(new BorderLayout());
		fileListPanelContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		fileListLabel = new JLabel("Results");
		fileListLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
		fileListLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));


		progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		
		progress = new JProgressBar(0, 0);
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
			public void initialize(int maximum)
			{
				progress.setMaximum(maximum);
				progress.setValue(0);
				progress.setString("Loading Thumbnails - 0%");
				if(Arrays.asList(fileListPanelContainer.getComponents()).indexOf(progressPanel) == -1)
				{
					fileListPanelContainer.add(progressPanel, BorderLayout.PAGE_END);
				}
			}

			@Override
			public void updateProgress(int increment)
			{
				progress.setValue(progress.getValue() + increment);
				double decimal = (double)progress.getValue() / (double)progress.getMaximum();
				int percentage = Math.round((float)(decimal * (double)100));
				progress.setString(("Loading Thumbnails - " + percentage + "%"));
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
		
		fileList = new FileList(fileListPanelContainer, search.generateStringFileList(true, true), FileList.VIEW_LIST, FileList.SCROLL_VERTICAL, pl);
		
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

		ImageFinder finder = search.getFinder();
		if(finder != null)
		{
			finder.addCompleteListener(new ThreadCompleteListener()
			{
				@Override
				public void notifyOfThreadComplete(Runnable thread)
				{
					//search.finderActive = false;
					if(((ImageFinder)thread).terminatedOnInterrupt)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								fileListLabel.setForeground(Color.red);
							}
						});
					}
					else
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								fileListLabel.setForeground(Color.black);
							}
						});
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							String[] newFiles = search.generateStringFileList(true, true);
							if(newFiles == null)
							{
								newFiles = new String[]{};
							}
							fileList.setFileNames(newFiles, 0);
							searchButton.setText("SEARCH");
						}
					});
				}
			});
			
			finder.setUpdateOutput(fileList.getLoadingLabel());
			finder.setCountOutput(fileListLabel);
		}
		
		fileListPanelContainer.add(fileListLabel, BorderLayout.PAGE_START);
		fileListPanelContainer.add(fileList.getComponent(), BorderLayout.CENTER);
		
		
		JPanel criteriaContainer = new JPanel();
		criteriaContainer.setLayout(new BorderLayout());
		criteriaContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel criteriaLabel = new JLabel("Search Criteria");
		criteriaLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
		criteriaLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		criteriaPanel = new SearchCriteriaPanel();

		JPanel searchButtonPanel = new JPanel();
		searchButtonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 20));
		
		searchButton = new JButton("SEARCH");
		searchButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
		searchButton.addActionListener(this);
		
		searchButtonPanel.add(searchButton);
		
		criteriaContainer.add(criteriaLabel, BorderLayout.PAGE_START);
		criteriaContainer.add(criteriaPanel.getComponent(), BorderLayout.CENTER);
		criteriaContainer.add(searchButtonPanel, BorderLayout.PAGE_END);
		
				
		this.add(criteriaContainer);
		this.add(fileListPanelContainer);
	}
	
	public void setFileListViewType(int newViewType)
	{
		fileList.setViewType(newViewType, fileListCR);
	}
	
	public boolean initializeSearch()
	{
		String errors = null;
		search.clearCriteria();
		search.clearOperators();
		ArrayList<SearchCriterion> scs = criteriaPanel.getCriteriaList();
		for(SearchCriterion sc : scs)
		{
			if(!sc.validateFields())
			{
				if(errors == null)
				{
					errors = new String(" - " + sc.getError() + "\n");
				}
				else
				{
					errors += (" - " + sc.getError() + "\n");
				}
			}
			search.addCriteria(sc);
		}
		ArrayList<BooleanOperator> bos = criteriaPanel.generateOperatorList();
		for(BooleanOperator bo : bos)
		{
			search.addOperator(bo);
		}
		if(errors != null)
		{
			String message = "The following errors occuring while attempting to perform your search:\n" + errors + "Please correct the above errors and perform your search again.";
			JOptionPane.showMessageDialog(frame, message, "Search Errors", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void refreshFileList()
	{
		if(search.getFinder() != null)
		{
			if(initializeSearch())
			{
				File dir = new File(frame.getDirectory());
				if(dir.exists() && dir.isDirectory())
				{
					searchButton.setText("CANCEL");
					fileListLabel.setForeground(Color.black);
					fileListLabel.setText("Results (0)");
					
					search.location = dir;
					fileList.loadNewData(search, true);
				}
				else
				{
					JOptionPane.showMessageDialog(frame, "Please enter valid directory before performing your seach", "Search Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(search.getFinder() != null)
		{
			refreshFileList();
		}
		else
		{
			search.cancel();
		}
	}
	
}