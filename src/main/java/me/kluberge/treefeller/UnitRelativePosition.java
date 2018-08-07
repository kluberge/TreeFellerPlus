package main.java.me.kluberge.treefeller;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class UnitRelativePosition {
	
	private static List<UnitRelativePosition> MEMO = new ArrayList<>(27);
	public static final UnitRelativePosition[] CARDINAL = new UnitRelativePosition[]
			{
					get(1,0,0), get(0,1,0), get(0,0,1), get(-1,0,0), get(0,-1,0), get(0,0,-1)
			};
	public static final UnitRelativePosition[] ALL;
	
	static
	{
		//excludes the trivial relative position (0,0,0) and all relative positions for which y = -1
		ALL = new UnitRelativePosition[26];
		int next = 0;
		for(int x = -1; x <= 1; x++)
			for(int y = -1; y <= 1; y++)
				for(int z = -1; z <= 1; z++)
					if(!(x == 0 && y == 0 && z == 0))
						ALL[next++] = get(x,y,z);
	}
	
	private int dx, dy, dz;
	
	public UnitRelativePosition(int dx, int dy, int dz)
	{
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
	public int getDx()
	{
		return dx;
	}
	
	public int getDy()
	{
		return dy;
	}
	
	public int getDz()
	{
		return dz;
	}
	
	@Override
	public String toString()
	{
		return getDx()+" "+getDy()+" "+getDz();
	}
	
	//returns the object associated with the relative position if it exists
	private static UnitRelativePosition memoGet(int dx, int dy, int dz)
	{
		for(int i=0; i<MEMO.size(); i++)
		{
			UnitRelativePosition m = MEMO.get(i);
			int mdx = m.getDx(), mdy = m.getDy(), mdz = m.getDz();
			if(dx == mdx && dy == mdy && dz == mdz)
				return m;
		}
		return null;
	}
	
	private static UnitRelativePosition get(int dx, int dy, int dz)
	{
		UnitRelativePosition search = memoGet(dx, dy, dz);
		if(search != null)
			return search;
		UnitRelativePosition entry = new UnitRelativePosition(dx, dy, dz);
		MEMO.add(entry);
		return entry;
	}
	
	//Note: these functions could be improved by using spherical coordinates and trigonometry, but this implementation is good enough
	public static UnitRelativePosition getRelativePosition(int dx, int dy, int dz)
	{
		dx = MathUtil.unitize(dx);
		dy = MathUtil.unitize(dy);
		dz = MathUtil.unitize(dz);
		return get(dx, dy, dz);
	}
	
	public static UnitRelativePosition getRelativePosition(Location from, Location to)
	{
		return getRelativePosition(to.getBlockX() - from.getBlockX(), to.getBlockY() - from.getBlockY(), to.getBlockZ() - from.getBlockZ());
	}
	
	public static Location apply(UnitRelativePosition pos, Location l)
	{
		return new Location(l.getWorld(), l.getBlockX() + pos.getDx(), l.getBlockY() + pos.getDy(), l.getBlockZ() + pos.getDz());
	}
}
