package main.java.me.kluberge.treefeller;

import org.bukkit.plugin.java.JavaPlugin;

public class TreeFeller extends JavaPlugin{
	/*
	 * TODO:
	 * Config.yml:
	 *		enabled/disabled worlds. type of axe needed. limit selection. survival only boolean.
	 * WorldGuard, Factions, and Towny support
	 */
	@Override
	public void onLoad()
	{
		
	}
	
	@Override 
	public void onEnable()
	{
		this.getServer().getPluginManager().registerEvents(new TreeBreakListener(this), this);
	}
	
	@Override
	public void onDisable()
	{
		
	}

}
