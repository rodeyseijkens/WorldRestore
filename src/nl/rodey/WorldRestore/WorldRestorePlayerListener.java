package nl.rodey.WorldRestore;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

public class WorldRestorePlayerListener implements Listener {
	public static final Logger log = Logger.getLogger("Minecraft");
	
	private PluginManager pm;
	private WorldRestore plugin;

	public WorldRestorePlayerListener(WorldRestore plugin) {
        this.plugin = plugin;
	}

	public void registerEvents()
    {
        pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{	
		if(plugin.teleportOnQuit)
		{
			Player player = event.getPlayer();
			String playerFromWorld = player.getWorld().getName();
			
			String data = plugin.WordRestoreList;
			if(data != null)
			{
				//Check if the world is in the config
		        String[] worlds = data.split(",");
		        
		        for (final String world : worlds)
		        {	 
		        	if( playerFromWorld.equalsIgnoreCase(world))
		        	{
		    			if(plugin.debug)
		    			{ 
		    				log.info("[" + plugin.getDescription().getName() + "] Trying Player Teleport");
		    			}
		    			
		        		player.teleport(plugin.getPlayerTeleportLoc(player));
		        	}
		        }
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{		
    	Player player = event.getPlayer();
    	final String playerFromWorld = event.getFrom().getWorld().getName();
    	String playerToWorld = event.getTo().getWorld().getName();
    	
    	if(plugin.checkWorldList(player, playerFromWorld, playerToWorld))
    	{
        	int TickConvertedTime = plugin.WorldRestoreDelay * 20;
        	
        	// Set minimum for random disconnects/reconnects
        	if(plugin.WorldRestoreDelay <= 10)
        	{
        		TickConvertedTime = 10;
        	}        	
    		
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

        	    public void run() {
    		    	plugin.checkWorldPlayerList(playerFromWorld);
        	    }
        	}, TickConvertedTime);
    	}
    	else
    	{
    		if(plugin.teleportOnQuit)
    		{
				String data = plugin.WordRestoreList;
				if(data != null)
				{
					
					//Check if the world is in the config
			        String[] worlds = data.split(",");
			        
			        for (final String world : worlds)
			        {	 
			        	if( (playerToWorld.equalsIgnoreCase(world)) &&  (!playerFromWorld.equalsIgnoreCase(world)))
			        	{
			        		plugin.setPlayerTeleportLoc(player, event.getFrom());
			        	}
			        }
				}
    		}
    	}
	}
	
	
	
}
