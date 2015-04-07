package com.lightark.criteria;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class IsoSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "ISO Speed Ratings";

	public final static int GREATER = 0;
	public final static int EQUAL = 1;
	public final static int LESS = 2;
	
	public final static String[] compareTypes = {"Greater Than", "Equal To", "Less Than"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "ISO Speed Ratings";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"400\"";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public IsoSC()
	{
		this(1,"");
	}
	
	public IsoSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new IsoSC();
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
		this.compareIndex = scp.compareTypeField.getSelectedIndex();
		this.value = scp.valueField.getText();
	}
	
	@Override
	public boolean validateFields()
	{
		if(value.toLowerCase().matches("[0-9]+"))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String getError()
	{
		return (name + ": The value entered must be an integer such as \"400\"");
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
		scp.tagField.setEditable(false);
	}
	
	@Override
	public boolean evaluate(File f)
	{
		ImageData data = new ImageData(f);
		if(data.invalidFile)
		{
			return false;
		}
		if(value.length() <= 0)
		{
			return false;
		}
		String givenISOStr = this.value.toLowerCase();
		int givenISO = 0;
		if(validateFields())
		{
			givenISO = Integer.parseInt(givenISOStr);
		}
		else
		{
			return false;
		}
		try
		{
			ExifSubIFDDirectory exif = data.getMetadata().getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if(exif == null)
			{
				return false;
			}
			if(!exif.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH))
			{
				return false;
			}
			int foundISO = exif.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
			
			switch(compareIndex)
			{
				case GREATER:
					if(foundISO > givenISO)
					{
						return true;
					}
					break;
				case EQUAL:
					if(foundISO == givenISO)
					{
						return true;
					}
					break;
				case LESS:
					if(foundISO < givenISO)
					{
						return true;
					}
					break;
				default:
					return false;
			}
		}
		catch (MetadataException e)
		{
			e.printStackTrace();
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
}