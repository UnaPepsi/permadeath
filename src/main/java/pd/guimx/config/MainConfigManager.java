package pd.guimx.config;

import org.bukkit.configuration.file.FileConfiguration;
import pd.guimx.Permadeath;

public class MainConfigManager {
    private CustomConfig configFile;
    private Permadeath permadeath;
    private int day;
    private int minPlayersSleep;

    public MainConfigManager(Permadeath permadeath){
        this.permadeath = permadeath;
        this.configFile = new CustomConfig("config.yml",null,permadeath);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig(){
        FileConfiguration config = configFile.getConfig();
        this.day = config.getInt("config.day");
        this.minPlayersSleep = config.getInt("config.minimum_players_sleep_needed");
    }

    public void reloadConfig(){
        configFile.reloadConfig();
        loadConfig();
    }

    public void setDay(int day){
        configFile.getConfig().set("config.day",day);
        configFile.saveConfig();
        reloadConfig();
    }

    public int getDay() {
        return day;
    }
    public int getMinPlayersSleep() {
        return minPlayersSleep;
    }
}
