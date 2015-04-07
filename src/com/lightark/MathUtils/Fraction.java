package com.lightark.MathUtils;

public class Fraction
{
	public int numerator;
	public int denominator;
	
	public Fraction(int numerator, int denominator)
	{
		if(denominator == 0)
		{
			throw new IllegalArgumentException("denominator cannot be zero");
		}
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public void multiplyBy(Fraction frac)
	{
		
	}
	
	public void simplify()
	{
		int gcd = Fraction.GCD(numerator, denominator);
		numerator /= gcd;
		denominator /= gcd;
	}
	
	/**
	 * Compare this Fraction object with the given Fraction, f to determine which is greater or if they are equal
	 * @param f the Fraction to compare to this Fraction object
	 * @return 1 = this Fraction is greater than the given Fraction, f
	 *         0 = the two Fractions are equal
	 *         -1 = this Fraction is less than the given Fraction, f
	 */
	public int compareTo(Fraction f)
	{
		int f1num = numerator;
		int f1den = denominator;
		
		int f2num = f.numerator;
		int f2den = f.denominator;
		
		int scaledf1num = f1num * f2den;
		int scaledf2num = f2num * f1den;
		
		if(scaledf1num > scaledf2num)
		{
			return 1;
		}
		else if(scaledf1num < scaledf2num)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public String toString()
	{
		return (numerator + "/" + denominator);
	}
	
	public static Fraction parseFraction(String s)
	{
		String numeratorStr = s.substring(0, s.indexOf("/"));
		int numerator = Integer.parseInt(numeratorStr);
		
		String denominatorStr = s.substring((s.indexOf("/") + 1), s.length());
		int denominator = Integer.parseInt(denominatorStr);
		
		if(denominator == 0)
		{
			return null;
		}
		return new Fraction(numerator, denominator);
	}
	
	public static int GCD(int a, int b)
	{
		if(b == 0)
		{
			return a;
		}
		return GCD(b,a%b);
	}
}