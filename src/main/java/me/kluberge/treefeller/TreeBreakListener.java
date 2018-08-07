package main.java.me.kluberge.treefeller;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class TreeBreakListener implements Listener 
{
	//private List<Location> arr = new ArrayList<>(600);
	private int leavesCount = 0;
	//for use in deciding which leaves belong to the tree currently being felled
	private Region region; 
	//when searching for leaves the algorithm only uses the six cardinal directions
	private static final UnitRelativePosition[] SEARCH_LEAVES = UnitRelativePosition.CARDINAL;
	private static final UnitRelativePosition[] SEARCH_LOGS = UnitRelativePosition.ALL;
	//prevents infinite recursion in compatibility mode
	private static boolean felling = false;
	
	private TreeFeller pl;
	
	public TreeBreakListener(TreeFeller pl)
	{
		this.pl = pl;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onTreeFell(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if(p == null || !p.hasPermission("treefellerplus.use") || felling)
			return;
		
		if(!(Boolean) Setting.CREATIVE.getValue()
				&& p.getGameMode().equals(GameMode.CREATIVE))
			return;
		
		@SuppressWarnings("deprecation") //needed for 1.8 compatibility
		ItemStack item = p.getInventory().getItemInHand();
		
		if((Boolean) Setting.NEED_AXE.getValue()
				&& item.getType() != Material.WOOD_AXE
				&& item.getType() != Material.STONE_AXE
				&& item.getType() != Material.IRON_AXE
				&& item.getType() != Material.GOLD_AXE
				&& item.getType() != Material.DIAMOND_AXE)
			return;
		Block b = e.getBlock();
		try
		{
			@SuppressWarnings("unchecked")
			ArrayList<String> blacklist = (ArrayList<String>) Setting.WORLD_BLACKLIST.getValue();
			for(int i=0; i<blacklist.size(); i++)
				if(b.getLocation().getWorld().getName().equalsIgnoreCase(blacklist.get(i)))
					return;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			TreeFeller.log(Level.WARNING, "Failed to parse world blacklist configuration setting: please check and correct the config.yml");
		}
		
		if(!isStump(b, false))
			return;
		
		if((Boolean)Setting.DEBUG.getValue())
			TreeFeller.log(Level.INFO, "Felling tree for "+p.getName());
		
		Location l = b.getLocation();
		
		//reset values
		List<Location> seen = new ArrayList<>();
		seen.add(l);
		leavesCount = 0;
		region = null;
		boolean leaves = (Boolean)Setting.FELL_LEAVES.getValue();
		List<Block> neighbors = search(seen, l, l, leaves, false);
		neighbors.add(b); //since the algorithm is depth-first, this is the easiest way to get it to take the initial block into account
		
		if((Boolean)Setting.DEBUG.getValue())
		{
			TreeFeller.log(Level.INFO, "num of neighbors: "+neighbors.size());
			TreeFeller.log(Level.INFO, "num of leaves: "+leavesCount);
		}
		
		if(neighbors.size() == 0)
			return;
		
		//anti-griefing heuristics
		if((Boolean) Setting.USE_HEURISTICS.getValue())
		{
			if(leavesCount < 0.5*neighbors.size())
				return;
			
			//checks to see if the selection spans at least two blocks in the y direction
			int y1 = neighbors.get(0).getY();
			boolean found = false;
			
			for(int i=1; i<neighbors.size(); i++)
			{
				Block near = neighbors.get(i);
				int y2 = near.getY();
				if(y1 != y2 && isLog(near))
				{
					found = true;
					break;
				}
			}
			
			if(!found)
				return;
		}
		
		//update tool durability and break blocks
		
		felling = true;
		
		short durability = item.getDurability();
		boolean broke = false, compat = (Boolean)Setting.COMPATIBILITY.getValue();
		for(int i=0; i<neighbors.size(); i++)
		{
			Block near = neighbors.get(i);
			
			if(region != null && !region.contains(near.getX(), near.getZ()) || near.getY() < b.getY())
				continue;
			
			//doesn't take durability for leaves
			if(isLog(near))
				++durability;
			
			if(compat && isLog(near))
				ReflectionUtil.breakBlockAsPlayer(p, near);
			else
				near.breakNaturally();
			
			if(durability > item.getType().getMaxDurability()
					&& p.getGameMode() != GameMode.CREATIVE)
			{
				broke = true;
				if(!compat)
					p.getInventory().remove(item); //the item should break on its own otherwise
				break;
			}
		}
		
		felling = false;
		
		if(!broke && !compat && p.getGameMode() != GameMode.CREATIVE)
			item.setDurability(durability);	
	}
	
	//TODO: instead of using contrived "modes," separate the method into multiple dependent methods
	//PHASES: mode 0 = outline the tree currently being felled
	//			mode 1 = outline the leaves (leaves = true)
	private List<Block> search(List<Location> seen, Location loc0, Location loc, boolean leaves, boolean outlinedTree)
	{
		List<Block> result = new ArrayList<>();
		//search directions vary based on the mode
		
		for(int mode = outlinedTree ? 1 : 0; mode < (leaves ? 2 : 1); mode++)
		{	
			UnitRelativePosition[] relativeLocs;
			if(mode == 1)
				relativeLocs = SEARCH_LEAVES;
			else
				relativeLocs = SEARCH_LOGS;
			
			for(int i=0; i<relativeLocs.length; i++)
			{
				Location neighbor = UnitRelativePosition.apply(relativeLocs[i], loc);
				int x = neighbor.getBlockX(), y = neighbor.getBlockY(), z = neighbor.getBlockZ();
				Block b = neighbor.getWorld().getBlockAt(x, y, z);
				
				//check to make sure block hasn't already been processed
				if(seen.contains(neighbor))
					continue;
				
				if(mode == 0 && !leaves && isLeaves(b)) 
					++leavesCount;
				
				if(mode == 0 && !isLog(b) || mode == 1 && !(isLeaves(b) || isLog(b)))
					continue;
				
				//another anti-griefing heuristic
				int dist = -1;
				if((Boolean)Setting.USE_HEURISTICS.getValue())
				{
					dist = MathUtil.distSquared(loc0.getBlockX(), loc0.getBlockZ(), x, z);
					//prevent huge capture
					if(dist >= 100)
					{
						if((Boolean)Setting.DEBUG.getValue())
							TreeFeller.log(Level.INFO, "Limit reached at x = "+x+",z = "+z);
						continue; 	
					}
				}
				
				if((Boolean) Setting.DEBUG.getValue())
					TreeFeller.log(Level.INFO, "Processing "+neighbor+":"+b.getType()+" in mode "+mode);
				seen.add(neighbor);
				
				if(mode == 1)
				{
					//the purpose of this code block is to define a region around the tree that is currently being felled
					//this region should not contain any other trees so that the leaves of the other trees are not affected  
					if(region != null && !region.contains(x, z))
						continue;
					if(dist == -1)
						dist = MathUtil.distSquared(loc0.getBlockX(), loc0.getBlockZ(), x, z);
					
					//this heuristic prevents the algorithm from classifying the base of the tree as the stump of another tree
					//note that this greedy behavior may not always work
					if(isStump(b, true))
							//&& (!(Boolean)Setting.USE_HEURISTICS.getValue() || dist > 2))
					{
						if((Boolean)Setting.DEBUG.getValue())
							TreeFeller.log(Level.INFO, "Found other tree at "+neighbor.getX()+" "+neighbor.getY()+" "+neighbor.getZ());
						List<Location> seenOther = new ArrayList<>(100);
						List<Block> otherTree = search(seenOther, neighbor, neighbor, false, false);
						int closestDist = Integer.MAX_VALUE;
						Location closest = null;
						for(int j=otherTree.size()-1; j>=0; j--)
						{
							Block treeBlock = otherTree.get(j);
							Location tl = treeBlock.getLocation();
							int logDist = MathUtil.distSquared(tl.getBlockX(), tl.getBlockZ(), loc0.getBlockX(), loc0.getBlockZ());
							if(logDist < closestDist)
							{
								closest = tl;
								closestDist = logDist;
							}
						}
						
						if(closest == null && (Boolean)Setting.DEBUG.getValue())
							TreeFeller.log(Level.WARNING, "closest = null");
						
						
						if(region == null 
								|| closest != null && closestDist <= region.getRadiusSquared())
						{
							UnitRelativePosition relPos = UnitRelativePosition.getRelativePosition(closest, loc0);
							Location offset = UnitRelativePosition.apply(relPos, closest);
							int ox = offset.getBlockX(), oz = offset.getBlockZ();
							
							//check to see if this makes the region smaller
							int candidateDist = MathUtil.distSquared(loc0.getBlockX(), loc0.getBlockZ(), ox, oz);
							if(region == null)
								region = new Region(loc0.getBlockX(), loc0.getBlockZ(), candidateDist);
							else if(candidateDist < region.getRadiusSquared())
								region.setRadiusSquared(candidateDist);
						}
						if((Boolean)Setting.DEBUG.getValue())
							TreeFeller.log(Level.INFO, "Region set to "+region);
						continue;
					}
				}
				
				//TreeFeller.log(Level.INFO, "Recurring on "+b.getLocation());
				result.add(b);
				
				List<Block> sub = search(seen, loc0, neighbor, leaves, mode == 1);
				
				for(int j=0; j<sub.size(); j++)
				{
					Block near = sub.get(j);
					if(!result.contains(near))
						result.add(near);
				}
				
				//for use with heuristic
				if(isLeaves(b))
					++leavesCount;
			}
		}
		return result;
	}
	
	//if strict = true, then the bottom must not be a log
	public boolean isStump(Block b, boolean strict)
	{
		if(!isLog(b))
			return false;
		Block bottom =  b.getRelative(BlockFace.DOWN);
		return bottom.getType() == Material.DIRT
					|| bottom.getType() == Material.GRASS
					|| bottom.getType() == Material.GRAVEL
					|| bottom.getType() == Material.SAND
					|| bottom.getType() == Material.CLAY
					|| (!strict && isLog(bottom));
	}
	
	public boolean isLeaves(Block b)
	{
		return b.getType() == Material.LEAVES || b.getType() == Material.LEAVES_2;
	}
	
	public boolean isLog(Block b)
	{
		return b.getType() == Material.LOG || b.getType() == Material.LOG_2;
	}
}
