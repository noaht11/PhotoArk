package com.lightark.FileUtils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public abstract class DirectoryChooser implements Choosable
{
	private JFrame frame;
	private JFileChooser jfc;
	
	public DirectoryChooser(JFrame _frame)
	{
		this(_frame, null);
	}
	
	public DirectoryChooser(JFrame _frame, File defaultFile)
	{
		this.frame = _frame;

		jfc = new JFileChooser()
		{
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
		    public void approveSelection()
		    {
		        File f = getSelectedFile();
		        if(!f.exists() && getDialogType() == OPEN_DIALOG)
		        {
		        	String message = "The folder \"" + f.getName() + "\" does not exist. Please select another folder";
		            JOptionPane.showMessageDialog(frame, message, "Folder does not exist", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
			    super.approveSelection();
		    }
		};
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(defaultFile != null)
		{
			jfc.setSelectedFile(defaultFile);
		}
	}
	
	public void showChooser()
	{
		int returnVal = jfc.showOpenDialog(frame);

		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			chosen(jfc.getSelectedFile());
		}
	}
}