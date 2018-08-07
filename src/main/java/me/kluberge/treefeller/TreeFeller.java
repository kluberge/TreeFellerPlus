package main.java.me.kluberge.treefeller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeFeller extends JavaPlugin
{
	private static YamlConfiguration defaultConfig;
	private static TreeFeller instance;
	private static final String prefix = ChatColor.DARK_GREEN+"["+ChatColor.GREEN+ChatColor.BOLD+"TreeFellerPlus"+ChatColor.RESET+ChatColor.DARK_GREEN+"]"+ChatColor.RESET;
	
	@Override
	public void onLoad()
	{
		TreeFeller.instance = this;
	}
	
	@Override 
	public void onEnable()
	{
		if(!reload())
			return;
		
		this.getCommand("treefellerplus").setExecutor(new CommandHandler());
		
		this.getServer().getPluginManager().registerEvents(new TreeBreakListener(this), this);
	}
	
	@Override
	public void onDisable()
	{
		
	}
	
	public boolean reload()
	{
		File folder = this.getDataFolder();
		if(!folder.exists())
			folder.mkdir();
		
		//create config
		File configFile = new File(getDataFolder(), "config.yml");
		if(!configFile.exists())
			this.saveResource("config.yml", false);
		
		try 
		{
			this.getConfig().load(configFile);
			log(Level.INFO, "Successfully loaded config.yml");
		} 
		catch (IOException | InvalidConfigurationException e) 
		{
			e.printStackTrace();
			log(Level.WARNING, "Failed to load config, ignoring file and using values from default config");
			
			try 
			{
				this.getConfig().addDefaults(getDefaultConfig());
			} 
			catch (NullPointerException npe) 
			{
				//fatal error propagated from getDefaultConfig()
				npe.printStackTrace();
				this.setEnabled(false);
				return false;
			}
		}
		
		//verify config
		Setting.reloadSettings();
		try
		{
			Setting.verifySettings();
		}
		catch(IllegalArgumentException e)
		{
			//TODO: handle this
		}
		return true;
	}
	
	public static YamlConfiguration getDefaultConfig()
	{
		if(defaultConfig != null)
			return defaultConfig;
		try
		{
			InputStreamReader reader = new InputStreamReader(instance.getResource("config.yml"));
			return defaultConfig = YamlConfiguration.loadConfiguration(reader);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
			
			//cannot find config.yml inside jar
			log(Level.SEVERE, "Unable to load default config due to invalid jar file: please report this error");
		}
		return null;
	}
	
	public static TreeFeller getInstance()
	{
		return instance;
	}
	
	public static void log(Level lvl, String msg)
	{
		instance.getLogger().log(lvl, msg);
	}
	
	public static String getPrefix()
	{
		return prefix;
	}
}
