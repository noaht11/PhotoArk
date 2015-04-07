package com.lightark.criteria;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.lightark.FileUtils.FileNames;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class FileSC implements SearchCriterion, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "File";

	public final static int CONTAINS = 0;
	public final static int NCONTAINS = 1;
	public final static int MATCHES = 2;
	public final static int NMATCHES = 3;

	public final static String[] compareTypes = {"Contains", "Doesn't Contain", "Matches", "Doesn't Match"};

	public final static int FILE_NAME = 0;
	public final static int FILE_EXT = 1;
	public final static int FILE_FORMAT = 2;
	
	private final static String[] tagTypes = {"File Name (extension incl.)", "File Extension", "File Format"};
	private JComboBox<String> tagField = new JComboBox<String>(tagTypes);
	

	public final static int JPEG = 0;
	public final static int PNG = 1;
	public final static int GIF = 2;
	public final static int BMP = 3;
	
	private final static String[] fileFormatTypes = {"JPEG", "PNG", "GIF", "BMP"};
	private JComboBox<String> fileFormats = new JComboBox<String>(fileFormatTypes);

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "File";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"Image.jpg\"";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public FileSC()
	{
		this(0,"");
	}
	
	public FileSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new FileSC();
	}
	
	@Override
	public void focusGui()
	{
		scp.focus();
	}
	
	@Override
	public JComponent getGuiPanel()
	{
		return scp;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void collectData()
	{
		int tagType = tagField.getSelectedIndex();
		switch (tagType)
		{
			case FILE_NAME:
				this.compareIndex = scp.compareTypeField.getSelectedIndex();
				this.value = scp.valueField.getText();
				break;
			case FILE_EXT:
				this.compareIndex = scp.compareTypeField.getSelectedIndex();
				this.value = scp.valueField.getText();
				break;
			case FILE_FORMAT:
				break;
			default:
				break;
		}
	}
	
	@Override
	public boolean validateFields()
	{
		return true;
	}
	
	@Override
	public String getError()
	{
		return ("");
	}
		
	@Override
	public void generateGuiPanel()
	{
		scp = new SearchCriterionPanel(this, tag, compareIndex, value, example, compareTypes)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void focus()
			{
				valueField.requestFocusInWindow();
			}
		};
		
		tagField.addActionListener(this);
		
		createNormalPanel();
	}
	
	private boolean evaluateString(String s)
	{
		s = s.toLowerCase();
		switch (compareIndex)
		{
			case CONTAINS:
				if(s.contains(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case NCONTAINS:
				if(!s.contains(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case MATCHES:
				if(s.matches(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case NMATCHES:
				if(!s.matches(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			default:
				return false;
		}
		return false;
	}
	
	private String getFormat(File f)
	{
		ImageInputStream iis = null;
		try
		{
			iis = ImageIO.createImageInputStream(f);
			
			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if(!iter.hasNext())
			{
				return null;
			}
			ImageReader reader = iter.next();
			return reader.getFormatName();
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	@Override
	public boolean evaluate(File f)
	{
		String fileName = f.getName();
		String fileExt = FileNames.getExtension(f);
		
		int tagType = tagField.getSelectedIndex();
		switch (tagType)
		{
			case FILE_NAME:
				return evaluateString(fileName);
			case FILE_EXT:
				return evaluateString(fileExt);
			case FILE_FORMAT:
				String selected = (String)fileFormats.getSelectedItem();
				String format = getFormat(f);
				if(format != null)
				{
					if(format.toLowerCase().matches(selected.toLowerCase()))
					{
						return true;
					}
				}
				break;
			default:
				break;
		}
		return false;
	}

	@Override
	public void addDeleteListener(DeleteListener dl)
	{
		deleteListeners.add(dl);
	}

	@Override
	public void removeDeleteListener(DeleteListener dl)
	{
		deleteListeners.remove(dl);
	}
	
	@Override
	public void delete()
	{
		for(DeleteListener dl : deleteListeners)
		{
			dl.objectDeleted(this);
		}
	}
	
	private void createNormalPanel()
	{
		scp.fields.removeAll();
		scp.fields.setLayout(new GridLayout(0,3,5,5));
		scp.fields.add(tagField);
		scp.fields.add(scp.compareTypeField);
		scp.fields.add(scp.valueField);
		scp.exampleLabel.setText(example);
		scp.revalidate();
		scp.repaint();
	}
	
	private void createFormatPanel()
	{
		scp.fields.removeAll();
		scp.fields.setLayout(new GridLayout(0,2,5,5));
		scp.fields.add(tagField);
		scp.fields.add(fileFormats);
		scp.revalidate();
		scp.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		int tagType = tagField.getSelectedIndex();
		switch (tagType)
		{
			case FILE_NAME:
				example = "Image.jpg";
				createNormalPanel();
				break;
			case FILE_EXT:
				example = ".jpg";
				createNormalPanel();
				break;
			case FILE_FORMAT:
				createFormatPanel();
				break;
			default:
				break;
		}
	}
}