package com.lightark.photoark.imagesearch;

import java.io.Serializable;

public class BooleanOperator implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static int BOOLEAN_AND = 0;
	public final static int BOOLEAN_OR = 1;
	public final static int BOOLEAN_NOT = 2;
	public final static int BOOLEAN_NAND = 2;
	public final static int BOOLEAN_NOR = 2;
	public final static int BOOLEAN_XOR = 3;

	public final static String[] opStrs = {"AND", "OR"};
	public final static int[] ops = {BooleanOperator.BOOLEAN_AND, BooleanOperator.BOOLEAN_OR};
	
	
	public int type = -1;
	
	public BooleanOperator(int type)
	{
		this.type = type;
	}
	
	public boolean evaluate(boolean i1, boolean i2)
	{
		if(type == BOOLEAN_AND)
		{
			if(i1 && i2)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(type == BOOLEAN_OR)
		{
			if(i1 || i2)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(type == BOOLEAN_XOR)
		{
			if((i1 || i2) && !(i1 && i2))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public String toString()
	{
		String boolType = "Invalid boolean type";
		switch(type)
		{
			case BOOLEAN_AND:
				boolType = "AND";
				break;
			case BOOLEAN_OR:
				boolType = "OR";
			default:
				break;
		}
		return "com.znt.imagesearch.BooleanOperator[" + boolType + "]";
	}
}