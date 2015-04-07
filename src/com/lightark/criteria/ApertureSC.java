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

public class ApertureSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "Aperture";

	public final static int LARGER = 0;
	public final static int EQUAL = 1;
	public final static int SMALLER = 2;
	
	public final static String[] compareTypes = {"Larger Than", "Equal To", "Smaller Than"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "Aperture";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"3.5\" or \"5\"";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public ApertureSC()
	{
		this(1,"");
	}
	
	public ApertureSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new ApertureSC();
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
		if(value.toLowerCase().matches("[0-9]+(.[0-9]+)?"))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String getError()
	{
		return (name + ": The value entered must be a decimal number such as \"3.5\" or \"5\"");
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
		
		String givenFNum = this.value.toLowerCase();
		double givenFNumDouble = 0;
		if(validateFields())
		{
			givenFNumDouble = Double.parseDouble(givenFNum);
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
			if(!exif.containsTag(ExifSubIFDDirectory.TAG_APERTURE))
			{
				return false;
			}
			double apex = exif.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
			double foundFNum = Math.pow(Math.sqrt(2), apex);
			foundFNum = Math.round(foundFNum * 100.0) / 100.0;
			
			switch(compareIndex)
			{
				case LARGER:
					if(foundFNum < givenFNumDouble)
					{
						return true;
					}
					break;
				case EQUAL:
					if(foundFNum == givenFNumDouble)
					{
						return true;
					}
					break;
				case SMALLER:
					if(foundFNum > givenFNumDouble)
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