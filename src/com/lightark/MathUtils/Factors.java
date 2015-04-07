package com.lightark.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class Factors
{
	public static List<Long> getNearestFactors(long number)
	{
		if(number <= 0)
		{
			return null;
		}
		
		List<Long> nearest = new ArrayList<Long>();
		
		double sqrt = Math.sqrt(number);
		long ceilSqrt = (long) Math.ceil(sqrt);
		long floorSqrt = (long) Math.floor(sqrt);
		for(long i = 0;i < floorSqrt;i++)
		{
			long val = (floorSqrt - i);
			if((number % val) == 0)
			{
				nearest.add(val);
				break;
			}
		}
		for(long i = 0;i <= (number - ceilSqrt + 1);i++)
		{
			long val = (ceilSqrt + i);
			if((number % val) == 0)
			{
				nearest.add(val);
				break;
			}
		}
		
		return nearest;
	}
	
	public static List<Long> createFactorList(long number)
	{
		//List<Long> primes = createPrimeFactorList(number);
		List<Long> factors = new ArrayList<Long>();
		
		return factors;
	}
	
	public static List<Long> createPrimeFactorList(long number)
	{
		if(number <= 0)
		{
			return null;
		}
		
		List<Long> factors = new ArrayList<Long>();
		long currentVal = number;
		long max = (long) Math.ceil(Math.sqrt(currentVal));
		long index;
		for(index = 2;index <= max;index++)
		{
			if(currentVal % index == 0)
			{
				factors.add(index);
				currentVal = currentVal / index;
				index = 1;
				max = (long) Math.ceil(Math.sqrt(currentVal));
			}
		}
		if(currentVal > 1)
		{
			factors.add(currentVal);
		}
		
		return factors;
	}
}