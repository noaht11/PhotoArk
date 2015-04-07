package com.lightark.photoark.imagesearch;

/*import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;*/
import java.io.File;
import java.io.Serializable;
/*import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;*/
import javax.swing.JComponent;
/*import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;*/

import com.lightark.photoark.DeleteListener;

public interface SearchCriterion extends Serializable
{	
	public SearchCriterion newInstance();
	
	public void generateGuiPanel();
	public JComponent getGuiPanel();
	public void focusGui();
	
	public String getName();

	public void collectData();
	public boolean validateFields();
	public String getError();
	
	public boolean evaluate(File f);
	
	public void addDeleteListener(DeleteListener dl);
	public void removeDeleteListener(DeleteListener dl);
	public void delete();
}

/*public abstract class SearchCriterion implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	protected int subClassID = -1;
	
	public String tag;
	public int compareIndex;
	public String value;
	public String example;
	
	protected transient SearchCriterionPanel scp = new SearchCriterionPanel();
	
	protected transient JMenuItem menuItem = new JMenuItem("");

	public SearchCriterion()
	{
		this("",0,"","");
	}
	
	public SearchCriterion(String tag, int compareIndex, String value, String example)
	{
		this.tag = tag;
		this.compareIndex = compareIndex;
		this.value = value;
		this.example = example;
	}
	
	public SearchCriterion newInstance()
	{
		return null;
	}

	public void generateGUIPanel(Object... objs)
	{
		//TO BE OVERRIDEN
	}
	
	public void generateMenuItem(Object... objs)
	{
		
	}
	
	public SearchCriterionPanel getGUIPanel()
	{
		return scp;
	}
	
	public JMenuItem getMenuItem()
	{
		return menuItem;
	}
	
	public boolean evaluate(ImageData data, File f)
	{
		return false;
	}
	
	public void collectData()
	{
		scp.fillInData(this);
	}

	public static boolean evaluateList(List<SearchCriterion> criteria, List<BooleanOperator> operators, ImageData data, File f)
	{
		SearchCriterion c1 = criteria.get(0);
		boolean prevResult = c1.evaluate(data, f);
		for(int i = 1;i < criteria.size();i++)
		{
			SearchCriterion cx = criteria.get(i);
			BooleanOperator boolOp = operators.get(i - 1);
			boolean val2 = cx.evaluate(data, f);
			prevResult = boolOp.evaluate(prevResult, val2);
		}
		if(prevResult)
		{
			return true;
		}
		return false;
	}*/
	
	
	
	

	/*public class SearchCriterionPanel extends JPanel implements FocusListener, ActionListener, SuggestionListener, Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();
		
		private JPanel buttons;
		private JButton delete;
		
		private JPanel examplePanel = new JPanel();
		private JLabel exampleLabel = new JLabel();
		
		private JPanel fields;
		protected JTextField tagField;
		protected JTextField valueField;
		protected JComboBox<String> compareTypeField;
		
		private SuggestionManager sm = new SuggestionManager(this, "");
		
		private String[] suggestions;
		private String[] valueExamples;
		
		public SearchCriterionPanel()
		{
			this("",0,"","",new String[]{});
		}
		
		public SearchCriterionPanel(String tag, int compareIndex, String value, String example, String[] compareTypes)
		{		
			Font theFont = new Font("Times New Roman", Font.PLAIN, 15);
					
			this.setLayout(new BorderLayout());
			
			buttons = new JPanel();
			buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			
			delete = new JButton("x");
			delete.setFont(new Font("Arial", Font.BOLD, 15));
			delete.setContentAreaFilled(false);
			delete.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			delete.setFocusable(false);
			delete.setOpaque(true);
			delete.setForeground(Color.red);
			delete.addActionListener(this);
			
			buttons.add(delete);
			
			this.add(buttons, BorderLayout.LINE_START);
			
			fields = new JPanel();
			fields.setLayout(new GridLayout(0,3,5,5));
			
			tagField = new JTextField(tag);
			tagField.setToolTipText("Metadata Tag Name");
			tagField.setFont(theFont);
			tagField.addFocusListener(this);
			tagField.requestFocus();
			
			valueField = new JTextField(value);
			valueField.setToolTipText("Value");
			valueField.setFont(theFont);
			valueField.addFocusListener(this);
			
			compareTypeField = new JComboBox<String>(compareTypes);
			compareTypeField.setSelectedIndex(compareIndex);
			compareTypeField.setFont(theFont);
			
			fields.add(tagField);
			fields.add(compareTypeField);
			fields.add(valueField);
			
			this.add(fields, BorderLayout.CENTER);
			
			exampleLabel.setText(example);
			examplePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			examplePanel.add(exampleLabel);
			
			this.add(examplePanel, BorderLayout.PAGE_END);
			
			this.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		}
		
		public void initSuggestionManager(String[] suggestions, String[] valueExamples)
		{
			this.suggestions = suggestions;
			this.valueExamples = valueExamples;
			sm = new SuggestionManager(this, this.suggestions);
			tagField.addKeyListener(sm);
		}
		
		public void fillInData(SearchCriterion sc)
		{
			sc.tag = tagField.getText();
			sc.compareIndex = compareTypeField.getSelectedIndex();
			sc.value = valueField.getText();
			sc.example = exampleLabel.getText();
		}
		
		public void focus()
		{
			tagField.requestFocus();
		}
		
		public void addDeleteListener(DeleteListener dl)
		{
			deleteListeners.add(dl);
		}
		
		public void removeDeleteListener(DeleteListener dl)
		{
			deleteListeners.remove(dl);
		}
		
		@Override
		public void focusGained(FocusEvent evt)
		{
			if (evt.getSource() instanceof JTextField)
			{
				JTextField src = (JTextField) evt.getSource();
				src.selectAll();
			}
		}

		@Override
		public void focusLost(FocusEvent evt)
		{

		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			//guiParent.removeCriteriaPanel(dataParent);
			for(DeleteListener dl : deleteListeners)
			{
				dl.objectDeleted(SearchCriterion.this);
			}
		}

		@Override
		public void suggestionFound(String fullSuggestion)
		{
			exampleLabel.setText("e.g. \"" + valueExamples[Arrays.asList(suggestions).indexOf(fullSuggestion)] + "\"");
		}

		@Override
		public void noSuggestionFound()
		{
			exampleLabel.setText("");
		}
	
	}
}*/