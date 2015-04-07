package com.lightark.photoark.imagesearch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

interface CriteriaAdder
{
	public void addCriteria(SearchCriterion sc);
}

public class CriteriaExtensionMenuItem extends JMenuItem implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SearchCriterion instanceMaker = null;
	private CriteriaAdder adder = null;
	
	public CriteriaExtensionMenuItem(SearchCriterion instanceMaker, CriteriaAdder ca)
	{
		this.setText(instanceMaker.getName());
		this.instanceMaker = instanceMaker;
		this.adder = ca;
		
		this.addActionListener(this);
	}


	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(instanceMaker != null)
		{
			adder.addCriteria(instanceMaker.newInstance());
		}
	}
}