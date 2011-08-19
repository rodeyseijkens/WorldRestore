package nl.rodey.WorldRestore;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

public class WorldRestorePlayerListener extends PlayerListener {
	public static final Logger log = Logger.getLogger("Minecraft");
	
	private PluginManager pm;
	private WorldRestore plugin;

	public WorldRestorePlayerListener(WorldRestore plugin) {
        this.plugin = plugin;
	}

	public void registerEvents()
    {
        pm = plugin.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.High, plugin);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.High, plugin);
    }
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{		
    	Player player = event.getPlayer();
    	String playerFromWorld = player.getWorld().getName();
    	String playerToWorld = null;

    	if(plugin.checkWorldList(player, playerFromWorld, playerToWorld))
    	{        	
        	if(plugin.TeleportQuit)
    		{ 
    			Location TeleportLoc = new Location(plugin.getServer().getWorld(plugin.TeleportQuitLoc_World), plugin.TeleportQuitLoc_X, plugin.TeleportQuitLoc_Y, plugin.TeleportQuitLoc_Z);
    		
    			player.teleport(TeleportLoc);
    		}
		}
	}
	
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
    		
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() 
        	{
    		    public void run() {
    		    	plugin.checkWorldPlayerList(playerFromWorld);
    		    }
    		}, TickConvertedTime);
    	}
	}
	
	
	
}
