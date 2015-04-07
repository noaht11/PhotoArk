package com.lightark.criteria;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JComponent;

import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class DateTakenSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "Date/Time Original";

	public final static int BEFORE = 0;
	public final static int ON = 1;
	public final static int AFTER = 2;
	
	public final static String[] compareTypes = {"Before", "On", "After"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "Date Taken";
	private int compareIndex;
	private String value;
	private String example = "yyyy-MM-dd HH:mm:ss";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	private Calendar givenCal = null;
	private Calendar foundCal = null;
	
	public DateTakenSC()
	{
		this(0,"");
	}
	
	public DateTakenSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;

		generateGuiPanel();
	}
	
	@Override
	public SearchCriterion newInstance()
	{
		return new DateTakenSC();
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
		Date givenDate = null;
		if(value.length() < 10)
		{
			try
			{
				givenDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-01-01 " + value);
			}
			catch (ParseException e)
			{
				return false;
			}
			givenCal = GregorianCalendar.getInstance();
			givenCal.setTime(givenDate);
			
			if(foundCal != null)
			{
				foundCal.set(Calendar.YEAR, givenCal.get(Calendar.YEAR));
				foundCal.set(Calendar.MONTH, givenCal.get(Calendar.MONTH));
				foundCal.set(Calendar.DAY_OF_MONTH, givenCal.get(Calendar.DAY_OF_MONTH));
				foundCal.set(Calendar.MILLISECOND, givenCal.get(Calendar.MILLISECOND));
			}
		}
		else if(value.length() > 11)
		{
			try
			{
				givenDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
			}
			catch (ParseException e)
			{
				return false;
			}
			givenCal = GregorianCalendar.getInstance();
			givenCal.setTime(givenDate);
		}
		else
		{
			try
			{
				givenDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
			}
			catch (ParseException e)
			{
				return false;
			}
			givenCal = GregorianCalendar.getInstance();
			givenCal.setTime(givenDate); 
			
			if(foundCal != null)
			{
				foundCal.set(Calendar.HOUR_OF_DAY, givenCal.get(Calendar.HOUR_OF_DAY));
				foundCal.set(Calendar.MINUTE, givenCal.get(Calendar.MINUTE));
				foundCal.set(Calendar.SECOND, givenCal.get(Calendar.SECOND));
				foundCal.set(Calendar.MILLISECOND, givenCal.get(Calendar.MILLISECOND));
			}
		}
		return true;
	}
	
	@Override
	public String getError()
	{
		return (name + ": The value entered must be a valid date in the format \"yyyy-MM-dd\" and/or \"HH:mm:ss\" (time must be 24-hour if included)");
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

		ExifSubIFDDirectory exif = data.getMetadata().getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		if(exif == null)
		{
			return false;
		}
		if(!exif.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED))
		{
			return false;
		}

		Date foundDate = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		foundCal = GregorianCalendar.getInstance();
		if(foundDate == null)
		{
			return false;
		}
		foundCal.setTime(foundDate);
		
		if(!validateFields())
		{
			return false;
		}
		
		switch(compareIndex)
		{
			case BEFORE:
				if(foundCal.before(givenCal))
				{
					return true;
				}
				break;
			case ON:
				if(foundCal.equals(givenCal))
				{
					return true;
				}
				break;
			case AFTER:
				if(foundCal.after(givenCal))
				{
					return true;
				}
				break;
			default:
				return false;
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