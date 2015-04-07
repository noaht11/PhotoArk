package com.lightark.photoark.imagesearch;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JPanel;

public class OperatorPanel extends JPanel implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JButton operators;
	
	public OperatorPanel(int type)
	{
		operators = new JButton(BooleanOperator.opStrs[type]);
		operators.setFont(new Font("Times New Roman", Font.BOLD, 15));
		operators.addActionListener(this);
		operators.setContentAreaFilled(false);
		operators.setFocusable(false);
		operators.setForeground(Color.blue);
		operators.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.add(operators);
	}

	public BooleanOperator generateBooleanOperator()
	{
		int index = Arrays.asList(BooleanOperator.opStrs).indexOf(operators.getText());
		if(index != -1)
		{
			return new BooleanOperator(BooleanOperator.ops[index]);
		}
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() instanceof JButton)
		{
			JButton src = (JButton) evt.getSource();
			int i = Arrays.asList(BooleanOperator.opStrs).indexOf(src.getText());
			if(i != -1)
			{
				int newI = i + 1;
				if(newI >= BooleanOperator.opStrs.length)
				{
					newI = 0;
				}
				src.setText(BooleanOperator.opStrs[newI]);
			}
		}
	}
}