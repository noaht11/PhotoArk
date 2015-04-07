package com.lightark.photoark.imagesearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lightark.photoark.ImageFinder;
import com.lightark.photoark.PhotoArk;

public class Search
{
	public final static String THREAD_ID = "SEARCH_IMAGE_FINDER";
	
	private ArrayList<SearchCriterion> criteria = new ArrayList<SearchCriterion>();
	private ArrayList<BooleanOperator> operators = new ArrayList<BooleanOperator>();
	
	public File location;
	public boolean includeSubDirs = true;
	
	private ImageFinder finder;
	//private Thread finderThread;
	//public boolean finderActive = false;
	
	public Search(File location)
	{
		this.location = location;
		finder = new ImageFinder(this);
		finder.setThreadName(THREAD_ID);
		PhotoArk.threadManager.registerThread(THREAD_ID, finder);
	}
	
	public String getCriteriaAsString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < criteria.size();i++)
		{
			sb.append(criteria.get(i).toString());
			sb.append("\n");
			if(i < operators.size())
			{
				sb.append(operators.get(i).toString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public void clearCriteria()
	{
		criteria.clear();
	}
	
	public void clearOperators()
	{
		operators.clear();
	}
	
	public void addOperator(BooleanOperator op)
	{
		operators.add(op);
	}
	
	public void addCriteria(SearchCriterion crit)
	{
		criteria.add(crit);
	}
	
	public String[] generateStringFileList(boolean absPath, boolean filter)
	{
		if(!finder.isActive())
		{
			ArrayList<File> fileRef;
			if(filter)
			{
				fileRef = finder.filteredFiles;
			}
			else
			{
				fileRef = finder.imageFiles;
			}
			String[] strs = new String[fileRef.size()];
			for(int i = 0;i < fileRef.size();i++)
			{
				if(absPath)
				{
					strs[i] = fileRef.get(i).getAbsolutePath();
				}
				else
				{
					strs[i] = fileRef.get(i).getName();
				}
			}
			Arrays.sort(strs);
			return strs;
		}
		else
		{
			return null;
		}
	}
	
	public void refreshFileList(boolean filter)
	{
		/*if(!finderActive)
		{
			finderActive = true;
			if(filter)
			{
				finder.filter = true;
			}
			else
			{
				finder.filter = false;
			}
			finderThread = new Thread(finder);
			finderThread.start();
		}*/
		if(!finder.isActive())
		{
			finder.filter = filter;
			finder.startThread();
		}
	}
	
	public void cancel()
	{
		/*if(finderActive)
		{
			finderThread.interrupt();
		}*/
		finder.interrupt();
	}
	
	public ImageFinder getFinder()
	{
		if(!finder.isActive())
		{
			return finder;
		}
		else
		{
			return null;
		}
	}
	
	public void performSearch()
	{
		refreshFileList(true);
	}
	
	public boolean evaluateFile(File f)
	{
		if(criteria.size() <= 0)
		{
			return true;
		}
		
		int prevOR = -1;
		ArrayList<Boolean> ors = new ArrayList<Boolean>();
		for(int i = 0;i < operators.size();i++)
		{
			if(operators.get(i).type == BooleanOperator.BOOLEAN_OR)
			{
				ors.add(evaluateList(criteria.subList((prevOR + 1), (i + 1)), operators.subList((prevOR + 1), i), f));
				prevOR = i;
			}
		}
		ors.add(evaluateList(criteria.subList((prevOR + 1), (operators.size() + 1)), operators.subList((prevOR + 1), operators.size()), f));
		
		boolean prevResult = ors.get(0);
		for(int i = 1;i < ors.size();i++)
		{
			BooleanOperator boolOp = new BooleanOperator(BooleanOperator.BOOLEAN_OR);
			boolean val2 = ors.get(i);
			prevResult = boolOp.evaluate(prevResult, val2);
		}
		if(prevResult)
		{
			return true;
		}
		return false;
	}

	public static boolean evaluateList(List<SearchCriterion> criteria, List<BooleanOperator> operators, File f)
	{
		SearchCriterion c1 = criteria.get(0);
		boolean prevResult = c1.evaluate(f);
		for(int i = 1;i < criteria.size();i++)
		{
			SearchCriterion cx = criteria.get(i);
			BooleanOperator boolOp = operators.get(i - 1);
			boolean val2 = cx.evaluate(f);
			prevResult = boolOp.evaluate(prevResult, val2);
		}
		if(prevResult)
		{
			return true;
		}
		return false;
	}
}