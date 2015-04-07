package com.lightark.criteria;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.drew.metadata.iptc.IptcDirectory;
import com.lightark.photoark.DeleteListener;
import com.lightark.photoark.ImageData;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imagesearch.SearchCriterionPanel;

public class IptcSC implements SearchCriterion
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String name = "IPTC";

	public final static int CONTAINS = 0;
	public final static int DOESNT_CONTAIN = 1;
	
	public final static String[] compareTypes = {"Contains", "Doesn't Contain"};

	private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
	
	private String tag = "IPTC";
	private int compareIndex;
	private String value;
	private String example = "e.g. \"Soccer\"";

	private transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	public IptcSC()
	{
		this(0,"");
	}
	
	public IptcSC(int compareIndex, String value)
	{
		this.compareIndex = compareIndex;
		this.value = value;
		
		generateGuiPanel();
	}

	@Override
	public SearchCriterion newInstance()
	{
		return new IptcSC();
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
				
		IptcDirectory iptc = data.getMetadata().getFirstDirectoryOfType(IptcDirectory.class);
		if(iptc == null)
		{
			return false;
		}
		if(!iptc.containsTag(IptcDirectory.TAG_KEYWORDS))
		{
			return false;
		}
		List<String> keywords = iptc.getKeywords();
		if(keywords == null)
		{
			return false;
		}
		List<String> lowerCaseKeywords = new ArrayList<String>();
		for(String s : keywords)
		{
			lowerCaseKeywords.add(s.toLowerCase());
		}
		
		switch(compareIndex)
		{
			case CONTAINS:
				if(lowerCaseKeywords.contains(value.toLowerCase()))
				{
					return true;
				}
				break;
			case DOESNT_CONTAIN:
				if(!lowerCaseKeywords.contains(value.toLowerCase()))
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