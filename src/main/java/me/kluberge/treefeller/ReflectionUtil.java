package main.java.me.kluberge.treefeller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ReflectionUtil 
{
	
	private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	
	public static Class<?> getNMSClass(String name)
	{
		try
		{
			return Class.forName("net.minecraft.server."+version+"."+name);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void breakBlockAsPlayer(Player p, Block b)
	{
		try 
		{
			//get playerInteractManager via reflection
			Class<?> cpClass = (Class<?>) Class.forName("org.bukkit.craftbukkit."+version+".entity.CraftPlayer");
			Object handle = cpClass.getMethod("getHandle").invoke(cpClass.cast(p));
			Field playerInteractManager = handle.getClass().getDeclaredField("playerInteractManager");
			Object pim = playerInteractManager.get(handle);
			
			//trick the server into thinking that the player broke the block
			Class<?> blockPosClass = getNMSClass("BlockPosition");
			Constructor<?> cons = blockPosClass.getConstructor(int.class, int.class, int.class);
			Location l = b.getLocation();
			Object blockPos = cons.newInstance(l.getBlockX(), l.getBlockY(), l.getBlockZ());
			pim.getClass().getDeclaredMethod("breakBlock", blockPosClass).invoke(pim, blockPos);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
