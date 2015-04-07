package com.lightark.photoark;

import java.io.Serializable;

public interface SuggestionListener extends Serializable
{
	public void suggestionFound(String fullSuggestion);
	public void noSuggestionFound();
}
