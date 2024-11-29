package pd.guimx.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pd.guimx.Permadeath;
import pd.guimx.utils.Miscellaneous;

import java.util.HashMap;
import java.util.List;

public class MainConfigManager {
    private CustomConfig configFile;
    private Permadeath permadeath;
    private int hour;
    private int minPlayersSleep;
    private int deathTrainSeconds;
    private HashMap<String,String> messages = new HashMap<>();
    private List<String> discordWebhooks;
    private int startingLives;
    private int rabiesSeconds;
    private int gracePeriod;

    public MainConfigManager(Permadeath permadeath){
        this.permadeath = permadeath;
        this.configFile = new CustomConfig("config.yml",null,permadeath);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig(){
        FileConfiguration config = configFile.getConfig();
        this.hour = config.getInt("config.hour");
        this.minPlayersSleep = config.getInt("config.minimum_players_sleep_needed");
        this.deathTrainSeconds = config.getInt("config.death_train_seconds");
        this.rabiesSeconds = config.getInt("config.rabies_seconds");
        gracePeriod = config.getInt("config.grace_period");
        messages.put("prefix",config.getString("messages.prefix"));
        messages.put("player_banned",config.getString("messages.player_banned"));
        messages.put("player_joined",config.getString("messages.player_joined"));
        messages.put("texture_pack",config.getString("messages.texture_pack"));
        messages.put("texture_pack_denied",config.getString("messages.texture_pack_denied"));
        messages.put("current_day",config.getString("messages.current_day"));
        messages.put("permadeath_title",config.getString("messages.permadeath_title"));
        messages.put("permadeath_subtitle",config.getString("messages.permadeath_subtitle"));
        messages.put("death_train_enabled",config.getString("messages.death_train_enabled"));
        messages.put("permadeath_kick_reason",config.getString("messages.permadeath_kick_reason"));
        messages.put("death_train",config.getString("messages.death_train"));
        messages.put("sleeping_disabled",config.getString("messages.sleeping_disabled"));
        messages.put("sleeping_disabled_deathtrain",config.getString("messages.sleeping_disabled_deathtrain"));
        messages.put("upgrade_netherite_failed",config.getString("messages.upgrade_netherite_failed"));
        messages.put("totem_failed",config.getString("messages.totem_failed"));
        messages.put("totem_worked",config.getString("messages.totem_worked"));
        messages.put("bed_anchor_disabled_end",config.getString("messages.bed_anchor_disabled_end"));
        discordWebhooks = config.getStringList("config.discord_webhooks");
        messages.put("discord_webhook_died",(config.getString("messages.discord_webhook_died")));
        messages.put("discord_webhook_totem",(config.getString("messages.discord_webhook_totem")));
        startingLives = config.getInt("config.starting_lives");
        messages.put("remaining_lifes",(config.getString("messages.remaining_lifes")));
        messages.put("motd",config.getString(getDay() > 29 ? "messages.motd_day_30" : "messages.motd_before_day_30"));
        messages.put("dragon_heal",config.getString("messages.dragon_heal"));
        messages.put("jump_disabled",config.getString("messages.jump_disabled"));
        messages.put("rabies_infected",config.getString("messages.rabies_infected"));
        messages.put("rabies_timer",config.getString("messages.rabies_timer"));
        messages.put("rabies_cured",config.getString("messages.rabies_cured"));
        messages.put("extra_heart_gained",config.getString("messages.extra_heart_gained"));
        messages.put("extra_heart_limit_reached",config.getString("messages.extra_heart_limit_reached"));
        messages.put("jesus_totem",config.getString("messages.jesus_totem"));
        messages.put("jesus_totem_used",config.getString("messages.jesus_totem_used"));
        messages.put("discord_webhook_jesus_totem",config.getString("messages.discord_webhook_jesus_totem"));
        messages.put("grace_period_removed",config.getString("messages.grace_period_removed"));
        Bukkit.setMotd(Miscellaneous.translateColor(messages.get("motd")));
    }

    public void reloadConfig(){
        configFile.reloadConfig();
        loadConfig();
    }

    public void setHour(int hour){
        configFile.getConfig().set("config.hour",hour);
        configFile.saveConfig();
        reloadConfig();
    }

    public int getDay() {
        return hour/24;
    }

    public int getGracePeriod() {
        return gracePeriod;
    }

    public int getHour() {return hour;}
    public int getMinPlayersSleep() {
        return minPlayersSleep;
    }
    public int getDeathTrainSeconds() {
        return deathTrainSeconds;
    }
    public HashMap<String,String> getMessages(){
        return messages;
    }
    public List<String> getDiscordWebhooks(){
        return discordWebhooks;
    }
    public int getStartingLives(){
        return startingLives;
    }

    public int getRabiesSeconds() {
        return rabiesSeconds;
    }
}
