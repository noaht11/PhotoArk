package com.lightark.photoark.imagesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.Serializable;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
//import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lightark.photoark.DeleteListener;

public class SearchCriteriaPanel implements Serializable, CriteriaAdder
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JScrollPane component;
	
	private JPanel bg;
	protected JPanel panel;
	
	//private JPanel buttonPanel;
	//private JButton addCrit;
	//private JButton addDateCrit;

	//private ArrayList<SearchCriterionPanel> criteria = new ArrayList<SearchCriterionPanel>();
	//private ArrayList<OperatorPanel> operators = new ArrayList<OperatorPanel>();
	private ArrayList<SearchCriterion> criteria = new ArrayList<SearchCriterion>();
	private ArrayList<OperatorPanel> operators = new ArrayList<OperatorPanel>();
	
	public SearchCriteriaPanel()
	{
		bg = new JPanel();
		bg.setLayout(new BorderLayout());
		bg.setOpaque(true);
		bg.setBorder(BorderFactory.createLineBorder(Color.black, 1));

		panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		panel.setOpaque(false);
		panel.setBorder(null);
		
		bg.add(panel, BorderLayout.PAGE_START);

		component = new JScrollPane(bg);
		component.getVerticalScrollBar().setUnitIncrement(15);
		
		createButtonPanel();
		
		//panel.add(buttonPanel);
	}
	
	public void addCriteriaPanel(SearchCriterion crit, int prevOpType)
	{
		if(panel.getComponentCount() > 0)
		{
			addOperatorPanel(prevOpType);
		}
		panel.add(crit.getGuiPanel());
		component.revalidate();
		component.repaint();
		crit.focusGui();
		criteria.add(crit);
		crit.addDeleteListener(new DeleteListener()
		{

			@Override
			public void objectDeleted(Object obj)
			{
				removeCriteriaPanel((SearchCriterion)obj);
			}
			
		});
	}

	public void addCriteria(SearchCriterion sc)
	{
		addCriteriaPanel(sc, BooleanOperator.BOOLEAN_AND);
	}
	
	/*public void addTagCriteriaPanel(String tag, int compareIndex, String value, String example, int prevOpType)
	{
		SearchCriterion crit = new CustomSC(tag, compareIndex, value, example);
		addCriteriaPanel(crit, prevOpType);
	}*/

	/*public void addDateTakenCriteriaPanel(int compareIndex, String value, int prevOpType)
	{
		DateTakenSC crit = new DateTakenSC(this, compareIndex, value);
		addCriteriaPanel(crit, prevOpType);
	}*/
	
	public void addOperatorPanel(int type)
	{
		OperatorPanel op = new OperatorPanel(type);
		panel.add(op);
		component.revalidate();
		component.repaint();
		operators.add(op);
	}
	
	public void removeCriteriaPanel(SearchCriterion sc)
	{
		int assocOp = Arrays.asList(panel.getComponents()).indexOf(sc.getGuiPanel()) - 1;
		OperatorPanel op = null;
		if(assocOp > 0)
		{
			op = (OperatorPanel)panel.getComponent(assocOp);
			panel.remove(assocOp);
		}
		else if(panel.getComponentCount() > 2)
		{
			assocOp = (assocOp + 2);
			op = (OperatorPanel)panel.getComponent(assocOp);
			panel.remove(assocOp);
		}
		panel.remove(sc.getGuiPanel());
		component.revalidate();
		component.repaint();
		criteria.remove(sc);
		if(op != null)
		{
			operators.remove(op);
		}
	}
	
	public void removeAll()
	{
		panel.removeAll();
		criteria.removeAll(criteria);
		operators.removeAll(operators);
		panel.revalidate();
		panel.repaint();
	}
	
	public void createButtonPanel()
	{
		/*buttonPanel = new JPanel();
		
		addCrit = new JButton("Add Criteria");
		addCrit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(panel.getComponentCount() > 1)
				{
					addOperatorPanel();
				}
				addCriteriaPanel();
			}
		});

		addDateCrit = new JButton("Add Date Criteria");
		addDateCrit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(panel.getComponentCount() > 1)
				{
					addOperatorPanel();
				}
				addDateCriteriaPanel();
			}
		});
		
		buttonPanel.add(addCrit);
		buttonPanel.add(addDateCrit);
		
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));*/
	}
	
	public void loadFromCriteriaSerializer(CriteriaSerializer cs)
	{
		for(int i = 0;i < cs.scs.size();i++)
		{
			int opNum = BooleanOperator.BOOLEAN_AND;
			if(i > 0)
			{
				opNum = cs.bos.get((i - 1)).type;
			}
			cs.scs.get(i).generateGuiPanel();
			addCriteriaPanel(cs.scs.get(i),opNum);
		}
	}
	
	/*public void loadFromCriteriaSerializer(CriteriaSerializer cs)
	{
		for(int i = 0;i < cs.scs.size();i++)
		{
			if(cs.scs.get(i).tag.matches("Date"))
			{
				addDateCriteriaPanel(cs.scs.get(i).compareType, cs.scs.get(i).value);
			}
			else
			{
				addCriteriaPanel(cs.scs.get(i).tag,cs.scs.get(i).compareType,cs.scs.get(i).value);
			}
			if(i < cs.bos.size())
			{
				addOperatorPanel(cs.bos.get(i).type);
			}
		}
	}*/
	
	/*public ArrayList<SearchCriteria> generateCriteriaList()
	{
		ArrayList<SearchCriteria> scs = new ArrayList<SearchCriteria>();
		for(SearchCriterionPanel scp : criteria)
		{
			scs.add(scp.generateSearchCriteria());
		}
		return scs;
	}*/
	
	public ArrayList<SearchCriterion> getCriteriaList()
	{
		for(SearchCriterion sc : criteria)
		{
			sc.collectData();
		}
		return criteria;
	}
	
	public ArrayList<BooleanOperator> generateOperatorList()
	{
		ArrayList<BooleanOperator> bos = new ArrayList<BooleanOperator>();
		for(OperatorPanel op : operators)
		{
			bos.add(op.generateBooleanOperator());
		}
		return bos;
	}
	
	public JComponent getComponent()
	{
		return component;
	}
	
	public void refreshDimensions()
	{
		
	}
	
}