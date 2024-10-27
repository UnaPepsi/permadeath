package pd.guimx;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import pd.guimx.commands.MainCommand;
import pd.guimx.config.MainConfigManager;
import pd.guimx.listeners.EntityListener;
import pd.guimx.listeners.PlayerListener;
import pd.guimx.utils.ManageDatabase;
import pd.guimx.utils.Miscellaneous;

import java.sql.SQLException;

public class Permadeath extends JavaPlugin {

    public String prefix;
    public String version = getDescription().getVersion();
    private ManageDatabase db;

    private MainConfigManager mainConfigManager;
    private PlayerListener playerListener;
    private ProtocolManager protocolManager;

    public void onEnable(){
        this.playerListener = new PlayerListener(this);
        this.mainConfigManager = new MainConfigManager(this);
        this.prefix = mainConfigManager.getMessages().get("prefix");
        generateVoidWorld();
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.db = new ManageDatabase();
        db.createTable();
        worldRules();
        registerCommands();
        registerEvents();
        startCounting();
        Bukkit.getConsoleSender().sendMessage(Miscellaneous.translateColor(prefix+"&ahas been enabled!"));
        Bukkit.setMotd(Miscellaneous.translateColor(mainConfigManager.getMessages().get("motd")));
    }

    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage(Miscellaneous.translateColor(prefix+"&chas been disabled!"));
        try {
            db.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerCommands(){
        this.getCommand("permadeath").setExecutor(new MainCommand(this));
    }
    public void registerEvents(){
        getServer().getPluginManager().registerEvents(playerListener,this);
        getServer().getPluginManager().registerEvents(new EntityListener(this),this);
    }

    public void worldRules(){
        Bukkit.getWorlds().forEach(w -> {
            w.setDifficulty(Difficulty.HARD);
            w.setHardcore(true);
            if (mainConfigManager.getDay() > 10){
                w.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE,200); //"vanilla" sleeping is disabled
            }
        });
    }

    private void generateVoidWorld(){
        if (Bukkit.getWorld("pd_void") == null) {
            Bukkit.getLogger().info(Miscellaneous.translateColor(prefix+"&epd_void world not found! Creating..."));
            WorldCreator worldCreator = new WorldCreator("pd_void");
            worldCreator.environment(World.Environment.NORMAL);
            worldCreator.type(WorldType.FLAT);
            worldCreator.generatorSettings("{\"layers\":[]}");
            worldCreator.createWorld();
        }
    }

    public ManageDatabase getDb() {
        return db;
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public PlayerListener getPlayerListener(){
        return playerListener;
    }
    private void startCounting(){
        Bukkit.getScheduler().runTaskTimer(this, () -> mainConfigManager.setHour(mainConfigManager.getHour()+1),0,3600*20); //every hour
    }
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
