package com.lightark.photoark;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.JTextField;

public class SuggestionManager extends KeyAdapter implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean wasTypeable = false;
	
	private String typedText;
	private String fullSuggestedText;
	
	private String [] suggestions;
	
	private SuggestionListener listener;
	
	public SuggestionManager(SuggestionListener listener, String... suggestions)
	{
		this.suggestions = suggestions;
		this.listener = listener;
	}
	
	public String getFullSuggestion(String start)
	{
		for(String sugg : suggestions)
		{
			sugg = sugg.toLowerCase();
			start = start.toLowerCase();
			if(sugg.startsWith(start))
			{
				return sugg;
			}
		}
		return null;
	}
	
	@Override
	public void keyTyped(KeyEvent evt)
	{
		wasTypeable = true;
	}
	
	@Override
	public void keyReleased(KeyEvent evt)
	{
		if(evt.getSource() instanceof JTextField)
		{
			JTextField src = (JTextField) evt.getSource();
			typedText = src.getText();
			if(typedText.length() <= 0)
			{
				listener.noSuggestionFound();
			}
			if(evt.getKeyCode() == KeyEvent.VK_BACK_SPACE || evt.getKeyCode() == KeyEvent.VK_DELETE)
			{
				wasTypeable = false;
				return;
			}
			if(wasTypeable)
			{
					if(typedText.length() > 0)
					{
						fullSuggestedText = getFullSuggestion(typedText);
						if(fullSuggestedText != null)
						{
							src.setText(fullSuggestedText);
							src.setSelectionStart(typedText.length());
							src.setSelectionEnd(fullSuggestedText.length());
							listener.suggestionFound(fullSuggestedText);
						}
						else
						{
							listener.noSuggestionFound();
						}
					}
			}
		}
		wasTypeable = false;
	}
}