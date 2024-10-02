package pd.guimx;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;
import pd.guimx.commands.MainCommand;
import pd.guimx.config.MainConfigManager;
import pd.guimx.listeners.EntityListener;
import pd.guimx.listeners.PlayerListener;
import pd.guimx.utils.ManageDatabase;
import pd.guimx.utils.MessageUtils;

import java.sql.SQLException;

public class Permadeath extends JavaPlugin {

    public String prefix;
    public String version = getDescription().getVersion();
    private ManageDatabase db;

    private MainConfigManager mainConfigManager;
    private PlayerListener playerListener;

    public void onEnable(){
        this.playerListener = new PlayerListener(this);
        this.mainConfigManager = new MainConfigManager(this);
        this.prefix = mainConfigManager.getMessages().get("prefix");
        this.db = new ManageDatabase();
        db.createTable();
        worldRules();
        registerCommands();
        registerEvents();
        startCounting();
        Bukkit.getConsoleSender().sendMessage(MessageUtils.translateColor(prefix+"&ahas been enabled!"));
    }

    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage(MessageUtils.translateColor(prefix+"&chas been disabled!"));
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
}
