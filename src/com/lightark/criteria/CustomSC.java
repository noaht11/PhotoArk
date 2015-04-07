package com.lightark.criteria;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;
//import javax.swing.JMenuItem;

import com.drew.metadata.Tag;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class CustomSC implements SearchCriterion
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static String name = "Custom";
	
	public final static int CONTAINS = 0;
	public final static int NCONTAINS = 1;
	public final static int MATCHES = 2;
	public final static int NMATCHES = 3;

	public final static String[] compareTypes = {"Contains", "Doesn't Contain", "Matches", "Doesn't Match"};
	
	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag;
	private int compareIndex;
	private String value;
	private String example;

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();

	private static String[] suggestions = {
			"",
			"Filename",
			"Compression Type",
			"Data Precision",
			"Image Height",
			"Image Width",
			"Number of Components",
			"Exposure Time",
			"F-Number",
			"ISO Speed Ratings",
			"Exif Version",
			"Shutter Speed Value",
			"Aperture Value",
			"Exposure Bias Value",
			"Flash",
			"Focal Length",
			"Color Space",
			"Exif Image Width",
			"Exif Image Height",
			"Custom Rendered",
			"Exposure Mode",
			"White Balance Mode",
			"Scene Capture Type",
			"Make",
			"Model",
			"X Resolution",
			"Y Resolution",
			"Resolution Unit",
			"Software",
			"Artist",
			"Keywords"
	};
	
	private String[] valueExamples = {
			"",
			"Image.jpg",
			"Baseline",
			"8 bits",
			"2212 pixels",
			"3318 pixels",
			"3",
			"0.02 sec",
			"F3.5",
			"400",
			"2.21",
			"1/50 sec",
			"F3.5",
			"0 EV",
			"Flash fired",
			"21.0 mm",
			"sRGB",
			"3318 pixels",
			"2212 pixels",
			"Normal process",
			"Manual exposure",
			"Auto white balance",
			"Standard",
			"Canon",
			"Canon EOS 30D",
			"350 dots per inch",
			"350 dots per inch",
			"Inch",
			"Digital Photo Professional",
			"Ray Sewlochan",
			"key1"
	};

	public CustomSC()
	{
		this("",0,"","");
	}
	
	public CustomSC(String tag, int compareIndex, String value, String example)
	{
		this.tag = tag;
		this.compareIndex = compareIndex;
		this.value = value;
		this.example = example;
		
		for(int i = 0;i < suggestions.length;i++)
		{
			suggestions[i] = suggestions[i].toLowerCase();
		}
		generateGuiPanel();
	}
	
	@Override
	public SearchCriterion newInstance()
	{
		return new CustomSC();
	}
	
	@Override
	public void generateGuiPanel()
	{
		scp = new SearchCriterionPanel(this,tag, compareIndex, value, example, compareTypes);
		scp.initSuggestionManager(suggestions, valueExamples);
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
		this.tag = scp.tagField.getText();
		this.compareIndex = scp.compareTypeField.getSelectedIndex();
		this.value = scp.valueField.getText();
		this.example = scp.exampleLabel.getText();
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
	public boolean evaluate(File f)
	{
		ImageData data = new ImageData(f);
		if(data.invalidFile)
		{
			return false;
		}
		
		if(this.tag.length() <= 0)
		{
			return true; 
		}
		String foundValue = "";
		if(this.tag.toLowerCase().matches("filename"))
		{
			foundValue = f.getName().toLowerCase();
		}
		else
		{
			Tag tag = data.getTag(this.tag);
			if(tag == null)
			{
				return false;
			}
			foundValue = tag.getDescription().toLowerCase();
		}
		switch (compareIndex)
		{
			case CONTAINS:
				if(foundValue.contains(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case NCONTAINS:
				if(!foundValue.contains(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case MATCHES:
				if(foundValue.matches(this.value.toLowerCase()))
				{
					return true;
				}
				break;
			case NMATCHES:
				if(!foundValue.matches(this.value.toLowerCase()))
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