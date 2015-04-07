package com.lightark.photoark;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.lightark.photoark.imagesearch.SearchFrame;
import com.lightark.photoark.imageviewer.ImageViewer;

public class OpenDialog extends JDialog implements ActionListener
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SearchFrame frame;
	
	private JPanel content;
	
	private JPanel programPanel;
	private ButtonGroup programBG;
	private JRadioButton viewer;
	private JRadioButton defaultViewer;
	private JPanel commandLinePanel;
	private JRadioButton commandLineButton;
	private JPanel commandLineFieldPanel;
	private JTextField commandLineProgramField;
	private JTextField commandLineArgField;
	private JButton commandLineExportButton;
	
	private JPanel filesToOpen;
	private ButtonGroup selectBG;
	private JRadioButton allFiles;
	private JRadioButton selectedFiles;
	
	private JPanel buttons;
	private JButton go;
	private JButton cancel;
	
	private ExportDialog exportOpts;
	private String exportPath;
	private String pathSep;
	
	public OpenDialog(SearchFrame _frame)
	{
		super(_frame, true);
		this.frame = _frame;
		
		this.setTitle("Open Files");
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.setLayout(new BorderLayout());
		
		exportOpts = new ExportDialog(frame, "OK", false);
		exportOpts.addListener(new DialogCompleteListener()
		{
			@Override
			public void dialogComplete(JDialog dialog)
			{
				exportPath = exportOpts.getPath();
				pathSep = exportOpts.getPathSep();
			}
		});
		exportPath = exportOpts.getPath();
		pathSep = exportOpts.getPathSep();
		
		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		Action enterAction = new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt)
			{
				go.doClick();
			}
		};
		content.getActionMap().put("ENTER", enterAction);
		
		programPanel = new JPanel();
		programPanel.setLayout(new GridLayout(0,1));
		programPanel.setBorder(BorderFactory.createTitledBorder("Open with"));
		
		viewer = new JRadioButton((PhotoArk.appName + " Viewer"));
		viewer.setSelected(true);
		
		defaultViewer = new JRadioButton("System Default Viewer");
		
		commandLinePanel = new JPanel();
		commandLinePanel.setLayout(new BorderLayout());
		//commandLinePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		
		commandLineButton = new JRadioButton("Command Line");
		commandLineFieldPanel = new JPanel();
		commandLineProgramField = new JTextField("\"C:\\Program Files\\MyViewer\\MyViewer.exe\"");
		commandLineArgField = new JTextField("-filelist");
		commandLineFieldPanel.add(commandLineProgramField);
		commandLineFieldPanel.add(commandLineArgField);
		commandLineExportButton = new JButton("Export Options");
		commandLineExportButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				exportOpts.setVisible(true);
			}
		});
		
		commandLinePanel.add(commandLineButton, BorderLayout.LINE_START);
		commandLinePanel.add(commandLineFieldPanel, BorderLayout.CENTER);
		commandLinePanel.add(commandLineExportButton, BorderLayout.LINE_END);
		
		programBG = new ButtonGroup();
		programBG.add(viewer);
		programBG.add(defaultViewer);
		programBG.add(commandLineButton);
		
		programPanel.add(viewer);
		programPanel.add(defaultViewer);
		programPanel.add(commandLinePanel);
		
		content.add(programPanel);
		
		filesToOpen = new JPanel();
		filesToOpen.setLayout(new GridLayout(0,1));
		filesToOpen.setBorder(BorderFactory.createTitledBorder("Files to open"));
		
		selectBG = new ButtonGroup();
		allFiles = new JRadioButton("All Files");
		//allFiles.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		selectedFiles = new JRadioButton("Selected Files");
		//selectedFiles.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		selectBG.add(allFiles);
		selectBG.add(selectedFiles);
		allFiles.setSelected(true);

		filesToOpen.add(allFiles);
		filesToOpen.add(selectedFiles);
		
		content.add(filesToOpen);
		
		Font buttonFont = new Font("Tahoma", Font.PLAIN, 13);
		buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		go = new JButton("Go");
		go.setFont(buttonFont);
		go.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.setFont(buttonFont);
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});
		buttons.add(go);
		buttons.add(cancel);
		
		this.add(content, BorderLayout.CENTER);
		
		this.add(buttons, BorderLayout.PAGE_END);
		
		this.pack();
		this.setLocationRelativeTo(frame);
	}
	
	@Override
	public void setVisible(boolean b)
	{
		this.setLocationRelativeTo(frame);
		super.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		this.dispose();
		String[] names = null;
		if(allFiles.isSelected())
		{
			names = frame.searchPanel.fileList.getAllFileNamesArray();
		}
		else
		{
			names = frame.searchPanel.fileList.getSelectedFileNamesArray();
		}
		
		if(viewer.isSelected())
		{
			ImageViewer iv = new ImageViewer(names,0, ImageViewer.SEARCH_RESULTS_DIR_STRING, true);
			iv.setVisible(true);
			PhotoArk.openFrames.add(iv);
		}
		else if(defaultViewer.isSelected())
		{
			for(String s : names)
			{
				File f = new File(s);
				try
				{
					Desktop.getDesktop().open(f);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(commandLineButton.isSelected())
		{
			try
			{
				frame.searchPanel.fileList.exportToFile(new File(exportPath), names, pathSep);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			ProcessBuilder pb = new ProcessBuilder(commandLineProgramField.getText(), commandLineArgField.getText(), exportPath);
			Process process = null;
			try
			{
				process = pb.start();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			final InputStream is = process.getInputStream();
			Thread t = new Thread(new Runnable()
			{
			    public void run()
			    {
			        try
			        {
			            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			            String line;
			            while ((line = reader.readLine()) != null)
			            {
			                System.out.println(line);
			            }
			        }
			        catch (IOException e)
			        {
			            e.printStackTrace();
			        }
			        finally
			        {
			            try
			            {
							is.close();
						}
			            catch (IOException e)
						{
							e.printStackTrace();
						}
			        }
			    }
			});
			t.start();
		}
	}
}