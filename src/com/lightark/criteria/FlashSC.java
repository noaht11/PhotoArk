package com.lightark.criteria;

import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class FlashSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "Flash";

	public final static int ON_CODE = 9;
	public final static int OFF_CODE = 16;
	
	public final static int ON = 0;
	public final static int OFF = 1;
	
	public final static String[] compareTypes = {"On","Off"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "Flash";
	private int compareIndex;
	private String value;
	private String example = "";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public FlashSC()
	{
		this(1,"");
	}
	
	public FlashSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new FlashSC();
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
	}
	
	@Override
	public boolean validateFields()
	{
		return true;
	}
	
	@Override
	public String getError()
	{
		return "";
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
				compareTypeField.requestFocusInWindow();
			}
		};
		scp.fields.removeAll();
		scp.fields.setLayout(new GridLayout(0,2,5,5));
		scp.fields.add(scp.tagField);
		scp.fields.add(scp.compareTypeField);
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
		try
		{
			ExifSubIFDDirectory exif = data.getMetadata().getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if(exif == null)
			{
				return false;
			}
			if(!exif.containsTag(ExifSubIFDDirectory.TAG_FLASH))
			{
				return false;
			}
			int foundFlash = exif.getInt(ExifSubIFDDirectory.TAG_FLASH);
			
			switch(compareIndex)
			{
				case ON:
					if(foundFlash == ON_CODE)
					{
						return true;
					}
					break;
				case OFF:
					if(foundFlash == OFF_CODE)
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