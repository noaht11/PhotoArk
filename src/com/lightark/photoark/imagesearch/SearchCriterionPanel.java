package com.lightark.photoark.imagesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.lightark.photoark.SuggestionListener;
import com.lightark.photoark.SuggestionManager;

public class SearchCriterionPanel extends JPanel implements FocusListener, ActionListener, SuggestionListener, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SearchCriterion dataParent;
	
	private JPanel buttons;
	private JButton delete;
	
	private JPanel examplePanel = new JPanel();
	public JLabel exampleLabel = new JLabel();
	
	public JPanel fields;
	public JTextField tagField;
	public JTextField valueField;
	public JComboBox<String> compareTypeField;
	
	private SuggestionManager sm = new SuggestionManager(this, "");
	
	private String[] suggestions;
	private String[] valueExamples;
	
	public SearchCriterionPanel()
	{
		this(null,"",0,"","",new String[]{""});
	}
	
	public SearchCriterionPanel(SearchCriterion dataParent, String tag, int compareIndex, String value, String example, String[] compareTypes)
	{
		this.dataParent = dataParent;
		
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
		delete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
	
	/*public void fillInData(SearchCriterion sc)
	{
		sc.setTag(tagField.getText());
		sc.setCompareIndex(compareTypeField.getSelectedIndex());
		sc.setValue(valueField.getText());
		sc.setExample(exampleLabel.getText());
	}*/
	
	public void focus()
	{
		tagField.requestFocus();
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
		if(dataParent != null)
		{
			dataParent.delete();
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