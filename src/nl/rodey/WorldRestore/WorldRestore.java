package nl.rodey.WorldRestore;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.ConsoleCommandSender;

public class WorldRestore extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public Boolean usingpermissions = false;
	public boolean debug = false;
	public String WordRestoreList = null;
	public boolean TeleportQuit = false;
	public String TeleportQuitLoc_World = null;
	public int TeleportQuitLoc_X;
	public int TeleportQuitLoc_Y;
	public int TeleportQuitLoc_Z;
	public int WorldRestoreDelay = 10;

	private final WorldRestorePlayerListener playerListener = new WorldRestorePlayerListener(this);
	
	public void onEnable()
	{ 
		// Turn off level saving
		getServer().dispatchCommand(new ConsoleCommandSender(getServer()), "save-off");
		
		// Load Console Message
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is loading...");
		
		// Load Config
		loadConfig();
		
		// Register Events
        playerListener.registerEvents();

		Plugin permissions = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions == null)
		{
			if (permissions != null)
			{
				Permissions = ((Permissions)permissions).getHandler();
				log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled with permissions!");
				usingpermissions = true;
			}
			else
			{
				log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled without permissions!");
				usingpermissions = false;
			}
		}
	}
	
	public void onDisable()
	{
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is disabled!");
	}
	
	public void loadConfig()
    {
        // Ensure config directory exists
        File configDir = this.getDataFolder();
        if (!configDir.exists())
            configDir.mkdir();

        // Check for existance of config file
        File configFile = new File(this.getDataFolder().toString()
                + "/config.yml");
        Configuration config = new Configuration(configFile);

        config.load();
        TeleportQuit = config.getBoolean("teleportquit", false);
        TeleportQuitLoc_World = config.getString("teleportquitloc_world", null);
        TeleportQuitLoc_X = config.getInt("teleportquitloc_X", 0);
        TeleportQuitLoc_Y = config.getInt("teleportquitloc_Y", 0);
        TeleportQuitLoc_Z = config.getInt("teleportquitloc_Z", 0);
        debug = config.getBoolean("debug", false);
        WordRestoreList = config.getString("worlds", null);
        WorldRestoreDelay = config.getInt("worldRestoreDelay", 10);
        
        if(WordRestoreList != null)
        {
        	log.info("[" + getDescription().getName() + "] Worlds loaded: " + WordRestoreList);
        }

        // Create default configuration if required
        if (!configFile.exists())
        {
            try
            {
                configFile.createNewFile();
                log.info("[" + getDescription().getName() + "] Config Created!");
            } 
            catch (IOException e)
            {
                reportError(e, "IOError while creating config file");
            }

            config.save();
        }        
        
    }

	public boolean checkpermissions(Player player, String string, Boolean standard)
	{
		return ((player.isOp() == true) || (usingpermissions ? Permissions.has(player,string) : standard));
	}	
	

	// Error Reporting
    public void reportError(Exception e, String message)
    {
        reportError(e, message, true);
    }

    public void reportError(Exception e, String message, boolean dumpStackTrace)
    {
        log.severe("[" + getDescription().getName() + "] " + message);
        if (dumpStackTrace)
            e.printStackTrace();
    }
    
    public void checkWorldList(Player player, String playerFromWorld, String playerToWorld)
	{
    	int TickConvertedTime = WorldRestoreDelay * 20;
    	
    	// Set minimum for random disconnects/reconnects
    	if(WorldRestoreDelay <= 10)
    	{
    		TickConvertedTime = 10;
    	}
    	
    	String data = WordRestoreList;
		if(data != null)
		{
			
			//Check if the world is an PersonalChest world
	        String[] worlds = data.split(",");
	        
	        for (final String world : worlds)
	        {	 
	        	if(!playerToWorld.equalsIgnoreCase(world))
	        	{
		        	if(debug)
	    			{ 
	    				log.info("["+getDescription().getName()+"] World Check  From: "+ world +" To:"+ playerToWorld + "Not the same");
	    			}
		    		
	        	   	if(playerFromWorld.equalsIgnoreCase(world))
		        	{
			        	if(debug)
		    			{ 
		    				log.info("["+getDescription().getName()+"] World Check  Listed: "+ world +" From:"+ playerToWorld + "The the same");
		    			}
			    		
			    		if(TeleportQuit)
			    		{ 
			    			Location TeleportLoc = new Location(getServer().getWorld(TeleportQuitLoc_World), TeleportQuitLoc_X, TeleportQuitLoc_Y, TeleportQuitLoc_Z);
			    		
			    			player.teleport(TeleportLoc);
			    		}
	        	
			    		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	
			    		    public void run() {
			    		    	checkWorldPlayerList(world);
			    		    }
			    		}, TickConvertedTime);
			    		
			    		return;			    		
		        	}        		
	        	}
	        }
		}
	}
    
    public void checkWorldPlayerList(String world)
	{
		// Delay and then continue		    		
		World checkWorld = getServer().getWorld(world);
    	
    	List<Player> worldPlayerList = checkWorld.getPlayers();
    	 
		if(worldPlayerList.size() == 0)
		{			
			if(WorldRestoreUnload(world))
			{
				WorldRestoreReload(world);
			}
		}
		else
		{
			if(debug)
			{
				log.info("["+getDescription().getName()+"] World Player Check: " + world +" " + worldPlayerList);
			}
		}
	}
    
    public boolean WorldRestoreUnload(String world)
    {
    	if(debug)
		{
			log.info("["+getDescription().getName()+"] "+ world +": Player Check: Empty");
		}
		
		// Unload Chunks
		int countChunkList = 0;
		int countChunkUnloaded = 0;
		
		Chunk[] ChunkList = getServer().getWorld(world).getLoadedChunks();
        
        for (final Chunk chunkBlock : ChunkList)
        { 
        	countChunkList++;
        	if(getServer().getWorld(world).unloadChunk(chunkBlock.getX(), chunkBlock.getZ(), false, false))
        	{
        		countChunkUnloaded++;
        	}
        	
        }
		
        // Report Chunks Unloaded
		if(debug)
		{
			log.info("[" + getDescription().getName() + "] " + countChunkUnloaded +" Chunks Unloaded of Total: " + countChunkList +"  "+ world);
		}
		
		// Unload the world
		if(getServer().unloadWorld(world, false))
		{
			if(debug)
			{
				log.info("[" + getDescription().getName() + "] "+ world +" deactivated.");
			}
			
			return true;
		}
		else
		{
			if(debug)
			{
				log.info("[" + getDescription().getName() + "] "+ world +" not deactivated, some one still in there?");
			}
			
			return false;
		}		
    }
    
    public boolean WorldRestoreReload(String world)
    {
		getServer().createWorld(world,Environment.SKYLANDS);

		if(debug)
		{
			log.info("[" + getDescription().getName() + "] "+ world +" activated");
		}
		return true;
    }
    
    
}
