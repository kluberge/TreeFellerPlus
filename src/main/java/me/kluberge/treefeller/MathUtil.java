package main.java.me.kluberge.treefeller;

public class MathUtil {
	public static int square(int i)
	{
		return i*i;
	}
	
	public static int unitize(int i)
	{
		if(Math.abs(i) <= 1)
			return i;
		return i / Math.abs(i);
	}
	
	public static int distSquared(int x0, int z0, int x, int z)
	{
		return square(x0 - x) + square(z0 - z);
	}
}
