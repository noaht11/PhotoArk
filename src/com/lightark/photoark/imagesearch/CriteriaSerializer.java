package com.lightark.photoark.imagesearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class CriteriaSerializer implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ArrayList<SearchCriterion> scs = null;
	public ArrayList<BooleanOperator> bos = null;

	public CriteriaSerializer(ArrayList<SearchCriterion> scs, ArrayList<BooleanOperator> bos)
	{
		this.scs = scs;
		this.bos = bos;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < scs.size();i++)
		{
			sb.append(scs.get(i).toString());
			sb.append("\n");
			if(i < bos.size())
			{
				sb.append(bos.get(i).toString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void writeToFile(CriteriaSerializer cs, File f)
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(f);
	        ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(cs);
	        out.close();
	        fileOut.close();
		}
		catch(IOException i)
		{
			i.printStackTrace();	          
		}
	}
	
	public static CriteriaSerializer readFromFile(File f)
	{
		CriteriaSerializer cs = null;
		try
		{
			FileInputStream fileIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fileIn);
	        cs = (CriteriaSerializer) in.readObject();
	        in.close();
	        fileIn.close();
	        return cs;
		}
		catch(IOException i)
		{
			i.printStackTrace();
			return null;
		}
		catch(ClassNotFoundException c)
		{
			System.out.println("CriteriaSerializer class not found");
	        c.printStackTrace();
	        return null;
		}
	}
}