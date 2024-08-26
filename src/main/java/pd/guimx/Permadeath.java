package pd.guimx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pd.guimx.commands.MainCommand;
import pd.guimx.listeners.PlayerListener;

public class Permadeath extends JavaPlugin {

    public static String prefix = "&8[&4&lPermadeath&r&8]&r ";
    public String version = getDescription().getVersion();

    public void onEnable(){
        registerCommands();
        registerEvents();
        Bukkit.getConsoleSender().sendMessage("Permadeath has been enabled!");
    }

    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage("Permadeath has been disabled!");
    }

    public void registerCommands(){
        this.getCommand("permadeath").setExecutor(new MainCommand(this));
    }
    public void registerEvents(){
        getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
    }
}
