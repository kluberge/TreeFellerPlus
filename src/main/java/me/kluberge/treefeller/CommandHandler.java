package main.java.me.kluberge.treefeller;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if((label.equalsIgnoreCase("treefeller") || label.equalsIgnoreCase("treefellerplus") || label.equalsIgnoreCase("tf") || label.equalsIgnoreCase("tfp")) 
				&& args.length == 1 
				&& args[0].equalsIgnoreCase("reload")
				&& sender.hasPermission("treefellerplus.reload"))
		{
			if(TreeFeller.getInstance().reload())
				sender.sendMessage(TreeFeller.getPrefix()+ChatColor.GREEN+" Successfully reloaded the config");
			else
				sender.sendMessage(TreeFeller.getPrefix()+ChatColor.RED+" Failed to relaod config");
			return true;
		}
		return false;
	}

}
