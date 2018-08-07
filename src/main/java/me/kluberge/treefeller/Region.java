package main.java.me.kluberge.treefeller;

//represents a cylindrical region
public class Region 
{
	private int x, z;
	private int r2; //radius squared
	
	public Region(int x, int z, int r2)
	{
		this.x = x;
		this.z = z;
		this.r2 = r2;
	}
	
	public boolean contains(int x, int z)
	{
		return MathUtil.distSquared(this.x, this.z, x, z) < r2;
	}
	
	public void setRadiusViaVector(int x, int z)
	{
		r2 = MathUtil.distSquared(this.x, this.z, x, z);
	}
	
	public void setRadiusSquared(int r2)
	{
		this.r2 = r2;
	}
	
	public int getRadiusSquared()
	{
		return r2;
	}
	
	@Override
	public String toString()
	{
		return "x = "+x+", z = "+z+", r^2 = "+r2;
	}
}
