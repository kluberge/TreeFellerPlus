package main.java.me.kluberge.treefeller;

import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Setting 
{
	CREATIVE("allow-creative", Boolean.class),
	COMPATIBILITY("enable-compatibility-mode", Boolean.class),
	DEBUG("debug", Boolean.class),
	DURABILITY("durability", Boolean.class),
	FELL_LEAVES("fell-leaves", Boolean.class),
	NEED_AXE("need-axe", Boolean.class),
	USE_HEURISTICS("use-heuristics", Boolean.class),
	VERSION("version", String.class), //TODO: use this setting to handle configuration updates
	WORLD_BLACKLIST("world-blacklist", ArrayList.class);
	
	private String section;
	private Class<?> def;
	private Object value;
	private Setting(String section, Class<?> def)
	{
		this.section = section;
		this.def = def; 
	}
	
	public String getSection()
	{
		return section;
	}
	
	public Class<?> getDefiningClass()
	{
		return def;
	}
	
	public Object getValue()
	{
		if(value == null)
			reload();
		return value;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	public void reload()
	{
		Object rawVal = TreeFeller.getInstance().getConfig().get(section);
		if(def.equals(String.class))
			value = rawVal.toString();
		else
			value = rawVal;
	}
	
	public static void reloadSettings()
	{
		Setting[] settings = Setting.values();
		for(int i=0; i<settings.length; i++)
			settings[i].reload();
	}
	
	public static void verifySettings()
	{
		Setting[] settings = Setting.values();
		for(int i=0; i<settings.length; i++)
		{
			Setting s = settings[i];
			if(s.getValue() == null || !s.getDefiningClass().equals(s.getValue().getClass()))
			{
				YamlConfiguration def = TreeFeller.getDefaultConfig();
				Object defValue = def.get(s.getSection());
				TreeFeller.log(Level.WARNING, "Unable to parse configuration entry for setting "+s+": "+s.getValue());
				TreeFeller.log(Level.WARNING, "Expected type "+s.getDefiningClass().getSimpleName());
				TreeFeller.log(Level.WARNING, "Falling back on default value: "+defValue);
				s.setValue(defValue);
			}
		}
	}
}
