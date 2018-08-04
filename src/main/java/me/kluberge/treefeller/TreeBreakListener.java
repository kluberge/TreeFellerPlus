package main.java.me.kluberge.treefeller;

import java.util.*;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class TreeBreakListener implements Listener {
	private List<Location> arr = new ArrayList<>(600);
	private int leavesCount = 0;
	private TreeFeller pl;
	
	public TreeBreakListener(TreeFeller pl)
	{
		this.pl = pl;
	}
	
	@EventHandler
	public void onTreeFell(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if(p == null || !p.hasPermission("treefeller.use") || p.getGameMode().equals(GameMode.CREATIVE))
			return;
		
		ItemStack item = p.getInventory().getItemInMainHand();
		if(!item.getType().equals(Material.WOOD_AXE) && !item.getType().equals(Material.STONE_AXE) && !item.getType().equals(Material.GOLD_AXE) && !item.getType().equals(Material.IRON_AXE) && !item.getType().equals(Material.DIAMOND_AXE))
			return;
		Block b = e.getBlock();
		Block ground =  b.getRelative(BlockFace.DOWN);
		if(!isLog(b) || (!ground.getType().equals(Material.DIRT) && !ground.getType().equals(Material.GRASS) && !ground.getType().equals(Material.AIR) && !isLog(ground)))
			return;
		//pl.getLogger().info("Conditions met for "+p.getName());
		arr.clear();
		leavesCount = 0;
		List<Block> neighbors = search(b.getLocation(), b.getLocation());
		//pl.getLogger().info("num of neighbors: "+neighbors.size());
		if(leavesCount < 0.5*neighbors.size())
			return;
		
		//checks to see if the selection spans at least two blocks in the y direction
		int y1 = neighbors.get(0).getY();
		boolean found = false;
		
		for(int i=1; i<neighbors.size(); i++)
		{
			Block near = neighbors.get(i);
			int y2 = near.getY();
			if(y1 != y2)
			{
				found = true;
				break;
			}
		}
		
		if(!found)
			return;
		
		//update tool durability
		short durability = item.getDurability();
		for(int i=0; i<neighbors.size(); i++)
		{
			Block near = neighbors.get(i);
			near.breakNaturally();
			++durability;
		}
		if(durability <= item.getType().getMaxDurability())
			item.setDurability(durability);
		else
			p.getInventory().remove(item);
	}
	
	private List<Block> search(Location loc0, Location loc)
	{
		List<Block> result = new ArrayList<>();
		//searches in +/-x, +y, and +/-z directions
		for(int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++)
			for(int y = loc.getBlockY(); y <= loc.getBlockY() + 1; y++)
				for(int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++)
				{
					Block b = loc.getWorld().getBlockAt(x, y, z);
					int dist = (x - loc0.getBlockX())*(x - loc0.getBlockX()) + (z - loc0.getBlockZ())*(z - loc0.getBlockZ());
					if(dist >= 100)
						continue; //prevent huge capture
					
					if(arr.contains(b.getLocation()))
						continue;
					arr.add(b.getLocation());
					if(isLog(b))
					{
						result.add(b);
						List<Block> sub = search(loc0, b.getLocation());
						for(int i=0; i<sub.size(); i++)
						{
							Block near = sub.get(i);
							if(!result.contains(near))
								result.add(near);
						}
					}
					else if(isLeaves(b))
						++leavesCount;
				}
		return result;
	}
	
	public boolean isLeaves(Block b)
	{
		return b.getType().equals(Material.LEAVES) || b.getType().equals(Material.LEAVES_2);
	}
	
	public boolean isLog(Block b)
	{
		return b.getType().equals(Material.LOG) || b.getType().equals(Material.LOG_2);
	}
}
