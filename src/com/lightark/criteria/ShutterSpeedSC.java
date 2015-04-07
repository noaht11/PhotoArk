package com.lightark.criteria;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lightark.MathUtils.Fraction;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class ShutterSpeedSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "Shutter Speed";

	public final static int FASTER = 0;
	public final static int EQUAL = 1;
	public final static int SLOWER = 2;
	
	public final static String[] compareTypes = {"Faster Than", "Equal To", "Slower Than"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "Shutter Speed";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"1/50\" (Unit is seconds)";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public ShutterSpeedSC()
	{
		this(1,"");
	}
	
	public ShutterSpeedSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new ShutterSpeedSC();
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
		if(value.toLowerCase().matches("(?<![\\/\\d])(\\d+)\\/(\\d+)(?![\\/\\d])"))
		{
			Fraction givenSpeedFrac = Fraction.parseFraction(value.toLowerCase());
			if(givenSpeedFrac != null)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getError()
	{
		return (name + ": The value entered must be a fraction (\"#/#\") where the denominator must not be zero");
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

		Fraction givenSpeedFrac = null;
		if(validateFields())
		{
			givenSpeedFrac = Fraction.parseFraction(value);
			if(givenSpeedFrac == null)
			{
				return false;
			}
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
			if(!exif.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED))
			{
				return false;
			}
			double apex = exif.getDouble(ExifSubIFDDirectory.TAG_SHUTTER_SPEED);
			double shutterSpeed = Math.pow(2, apex);
			int roundedShutterSpeed = (int) Math.round(shutterSpeed);
			Fraction foundFrac = null;
			try
			{
				foundFrac = new Fraction(1, roundedShutterSpeed);
			}
			catch(IllegalArgumentException e)
			{
				return false;
			}
			
			switch(compareIndex)
			{
				case FASTER:
					if(foundFrac.compareTo(givenSpeedFrac) == -1)
					{
						return true;
					}
					break;
				case EQUAL:
					if(foundFrac.compareTo(givenSpeedFrac) == 0)
					{
						return true;
					}
					break;
				case SLOWER:
					if(foundFrac.compareTo(givenSpeedFrac) == 1)
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