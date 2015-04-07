package com.lightark.photoark;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.lightark.FileUtils.SaveFileChooser;
import com.lightark.photoark.imagesearch.SearchFrame;

interface DialogCompleteListener
{
	public void dialogComplete(JDialog dialog);
}

public class ExportDialog extends JDialog implements ActionListener
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<DialogCompleteListener> listeners = new ArrayList<DialogCompleteListener>();
	
	private SearchFrame frame;
	
	private JPanel dirPanel;
	private JPanel fieldPanel;
	private JTextField dirField;
	private JPanel browsePanel;
	private JButton browse;
	
	private JPanel content;
	
	private JPanel filesToExport;
	private ButtonGroup selectBG;
	private JRadioButton allFiles;
	private JRadioButton selectedFiles;
	
	private JPanel sepPanel;
	private ButtonGroup sepBG;
	private JRadioButton newLine;
	private JRadioButton tab;
	private JPanel customCharPanel;
	private JRadioButton customChar;
	private JTextField customCharField;
	
	private JPanel buttons;
	private JButton export;
	private JButton cancel;
	
	private boolean exportOnFinish;
	
	public ExportDialog(SearchFrame _frame, String actionButtonName, boolean exportOnFinish)
	{
		super(_frame, true);
		this.frame = _frame;
		this.exportOnFinish = exportOnFinish;
		
		this.setTitle("Export");
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setIconImages(PhotoArk.appIcons);
		this.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);

		this.setLayout(new BorderLayout());
		
		dirPanel  = new JPanel();
		dirPanel.setOpaque(false);
		dirPanel.setLayout(new BorderLayout());
		dirPanel.setBorder(BorderFactory.createTitledBorder("Export Destination"));
		
		fieldPanel = new JPanel();
		fieldPanel.setLayout(new BorderLayout());
		dirField  = new JTextField(25);
		dirField.setText((System.getProperty("user.home") + "\\Files.txt"));
		fieldPanel.add(dirField, BorderLayout.PAGE_START);
		browsePanel = new JPanel();
		browsePanel.setLayout(new BorderLayout());
		browse = new JButton("Browse");
		browse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				SaveFileChooser sfc = new SaveFileChooser(frame, null, null, "Files.txt")
				{
					@Override
					public void chosen(Object obj)
					{
						dirField.setText(((File)obj).getAbsolutePath());
					}
				};
				sfc.showChooser();
			}
		});
		browsePanel.add(browse, BorderLayout.PAGE_START);
		
		dirPanel.add(fieldPanel, BorderLayout.CENTER);
		dirPanel.add(browsePanel, BorderLayout.LINE_START);
		
		this.add(dirPanel, BorderLayout.PAGE_START);
		
		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		
		filesToExport = new JPanel();
		filesToExport.setLayout(new GridLayout(0,1));
		filesToExport.setBorder(BorderFactory.createTitledBorder("Files to export"));
		
		selectBG = new ButtonGroup();
		allFiles = new JRadioButton("All Files");
		selectedFiles = new JRadioButton("Selected Files");
		selectBG.add(allFiles);
		selectBG.add(selectedFiles);
		allFiles.setSelected(true);
		
		filesToExport.add(allFiles);
		filesToExport.add(selectedFiles);
		
		content.add(filesToExport);
		
		sepPanel = new JPanel();
		sepPanel.setLayout(new GridLayout(0,1));
		sepPanel.setBorder(BorderFactory.createTitledBorder("Seperate file paths by:"));		
		
		sepBG = new ButtonGroup();
		newLine = new JRadioButton("New Line");
		newLine.setSelected(true);
		tab = new JRadioButton("Tab");
		customCharPanel = new JPanel();
		customCharPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		customCharPanel.setBorder(null);
		customChar = new JRadioButton("Custom Character: ");
		customCharField = new JTextField(3);
		customCharField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent evt)
			{
				customChar.setSelected(true);
			}
		});
		customCharPanel.add(customChar);
		customCharPanel.add(customCharField);
		((FlowLayout)customCharPanel.getLayout()).setHgap(0);
		sepBG.add(newLine);
		sepBG.add(tab);
		sepBG.add(customChar);
		
		sepPanel.add(newLine);
		sepPanel.add(tab);
		sepPanel.add(customCharPanel);
		
		content.add(sepPanel);
		
		this.add(content, BorderLayout.CENTER);
		
		Font buttonFont = new Font("Tahoma", Font.PLAIN, 13);
		buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		export = new JButton(actionButtonName);
		export.setFont(buttonFont);
		export.addActionListener(this);
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
		buttons.add(export);
		buttons.add(cancel);
		
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
	
	public String getPath()
	{
		return dirField.getText();
	}
	
	public String getPathSep()
	{
		String sep = "";
		if(newLine.isSelected())
		{
			sep = "\n";
		}
		else if(tab.isSelected())
		{
			sep = "\t";
		}
		else if(customChar.isSelected())
		{
			sep = customCharField.getText();
		}
		return sep;
	}
	
	public void addListener(DialogCompleteListener dcl)
	{
		listeners.add(dcl);
	}
	
	private void notifyListeners()
	{
		for(DialogCompleteListener dcl : listeners)
		{
			dcl.dialogComplete(this);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		File f = new File(dirField.getText());
		if(f.getParentFile() != null)
		{
			if(!f.getParentFile().exists())
			{
				JOptionPane.showMessageDialog(this, "The file you have selected is not a valid file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "The file you have selected is not a valid file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(!exportOnFinish)
		{
			notifyListeners();
			this.dispose();
			return;
		}
		ArrayList<String> files = new ArrayList<String>();
		if(allFiles.isSelected())
		{
			files = frame.searchPanel.fileList.getAllFileNames();
		}
		else if(selectedFiles.isSelected())
		{
			files = frame.searchPanel.fileList.getSelectedFileNames();
		}
		
		String sep = getPathSep();
		
		try
		{
			frame.searchPanel.fileList.exportToFile(f, files, sep);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.dispose();
	}
}