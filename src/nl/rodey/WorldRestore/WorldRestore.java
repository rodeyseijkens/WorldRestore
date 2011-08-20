package nl.rodey.WorldRestore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldRestore extends JavaPlugin
{
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public Boolean usingpermissions = false;
	public boolean debug = false;
	public String WordRestoreList = null;
	public int WorldRestoreDelay = 10;
	Calendar cal = new GregorianCalendar();
	public int pluginStartTime;
	public boolean teleportOnQuit = true;
	public Location playerTeleportLocation = null;

	private final WorldRestorePlayerListener playerListener = new WorldRestorePlayerListener(this);
	
	public void onEnable()
	{		
		// Load Console Message
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is loading...");
		
		pluginStartTime = (int) System.currentTimeMillis();
		log.info("[" + getDescription().getName() + "] Plugin start time: " + pluginStartTime);
		
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
        debug = config.getBoolean("debug", false);
        teleportOnQuit = config.getBoolean("teleportOnQuit", true);
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
        
        return;
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
    
    public boolean checkWorldList(Player player, String playerFromWorld, String playerToWorld)
    {
    	String data = WordRestoreList;
		if(data != null)
		{
			
			//Check if the world is in the config
	        String[] worlds = data.split(",");
	        
	        for (final String world : worlds)
	        {	 
	        	if( (playerFromWorld.equalsIgnoreCase(world)) &&  (!playerToWorld.equalsIgnoreCase(world)))
	        	{
	        		if(debug)
	    			{ 
	    				log.info("["+getDescription().getName()+"] "+ player.getName() +" was teleporting from: " + playerFromWorld + " to: " + playerToWorld);
	    			}
		    		
		    		return true;
	        	}
	        }
		}
		
		return false;
	}
    
    public void checkWorldPlayerList(String world)
	{
		// Delay and then continue		    		
		World checkWorld = getServer().getWorld(world);
    	
    	List<Player> worldPlayerList = checkWorld.getPlayers();
    	 
		if(worldPlayerList.size() == 0)
		{			
			WorldRestoreRestore(world);
		}
		else
		{
			if(debug)
			{
				log.info("["+getDescription().getName()+"] World Player Check: " + world +" " + worldPlayerList);
			}
		}
	}
    
    public boolean WorldRestoreRestore(String world)
    {
    	
    	if(debug)
		{
			log.info("[" + getDescription().getName() + "] Plugin start time: " + pluginStartTime);
			log.info("[" + getDescription().getName() + "] Current Time: " + (int) System.currentTimeMillis());
		}
    	
    	CommandSender cSender = new ConsoleCommandSender(getServer());
    	
    	LogBlock logblock = (LogBlock)getServer().getPluginManager().getPlugin("LogBlock");
    	QueryParams params = new QueryParams(logblock);
    	params.world = getServer().getWorld(world);
    	params.minutes = (((int) System.currentTimeMillis() - pluginStartTime) / 60000);
    	params.silent = true;
    	try {
    		logblock.getCommandsHandler().new CommandRollback(cSender, params, true);
    	    
    	    if(debug)
			{
				log.info("[" + getDescription().getName() + "] "+ world +" restored");
			}
    	    
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
    }
    
	public void setPlayerTeleportLoc(Player player, Location fromLocation) 
	{		
		File locDataFolder = new File(getDataFolder().getAbsolutePath(), "playerLocations");
		locDataFolder.mkdirs();
		
		try {
			File playerLocFile = new File(locDataFolder , player.getName() + ".loc");
			playerLocFile.createNewFile();
	
			final BufferedWriter out = new BufferedWriter(new FileWriter(playerLocFile));

        	out.write(fromLocation.getWorld().getName()+"#"+fromLocation.getX()+"#"+fromLocation.getY()+"#"+fromLocation.getZ());
        	
			out.close();

			if(debug)
			{ 
				log.info("[" + getDescription().getName() + "] Player Teleport Location File Created");
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Location getPlayerTeleportLoc(Player player) 
	{		
		File locDataFolder = new File(getDataFolder().getAbsolutePath(), "playerLocations");

		File playerLocFile = new File(locDataFolder , player.getName() + ".loc");
		
		if(debug)
		{ 
			log.info("[" + getDescription().getName() + "] Loading Player Teleport File");
		}
		
		try {
			
			final BufferedReader in = new BufferedReader(new FileReader(playerLocFile));

			String line;
			line = in.readLine();
			
			final String[] parts = line.split("#");
			in.close();		
			
			World world = getServer().getWorld(parts[0]);

			if(debug)
			{ 
				log.info("[" + getDescription().getName() + "] Player Teleport Location: " + world.getName() + " | " + parts[1] + " | " + parts[2] + " | "+ parts[3] + " | ");
			}
			
			playerTeleportLocation = new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));		
			
			return playerTeleportLocation;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
