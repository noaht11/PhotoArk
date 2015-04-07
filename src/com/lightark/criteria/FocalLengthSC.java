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

public class FocalLengthSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "Focal Length";

	public final static int LONGER = 0;
	public final static int EQUAL = 1;
	public final static int SHORTER = 2;
	
	public final static String[] compareTypes = {"Longer Than", "Equal To", "Shorter Than"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "Focal Length";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"50\" (Unit is millimetres)";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public FocalLengthSC()
	{
		this(1,"");
	}
	
	public FocalLengthSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new FocalLengthSC();
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
		return (name + ": The value entered must be a decimal number such as \"50\" or \"46.5\"");
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
		String givenFLStr = this.value.toLowerCase();
		double givenFocalLength = 0;
		if(validateFields())
		{
			givenFocalLength = Double.parseDouble(givenFLStr);
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
			double foundFL = exif.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
			
			switch(compareIndex)
			{
				case LONGER:
					if(foundFL > givenFocalLength)
					{
						return true;
					}
					break;
				case EQUAL:
					if(foundFL == givenFocalLength)
					{
						return true;
					}
					break;
				case SHORTER:
					if(foundFL < givenFocalLength)
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