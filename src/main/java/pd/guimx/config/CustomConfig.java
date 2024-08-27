package pd.guimx.config;


import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pd.guimx.Permadeath;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    private Permadeath permadeath;
    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String folderName;

    public CustomConfig(String fileName, String folderName, Permadeath permadeath){
        this.fileName = fileName;
        this.folderName = folderName;
        this.permadeath = permadeath;
    }

    public String getPath(){
        return this.fileName;
    }

    public void registerConfig(){
        if(folderName != null){
            file = new File(permadeath.getDataFolder() +File.separator + folderName,fileName);
        }else{
            file = new File(permadeath.getDataFolder(), fileName);
        }

        if(!file.exists()){
            if(folderName != null){
                permadeath.saveResource(folderName+File.separator+fileName, false);
            }else{
                permadeath.saveResource(fileName, false);
            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        if (fileConfiguration == null) {
            if(folderName != null){
                file = new File(permadeath.getDataFolder() +File.separator + folderName, fileName);
            }else{
                file = new File(permadeath.getDataFolder(), fileName);
            }

        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if(file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }
}
