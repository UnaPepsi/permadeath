package pd.guimx;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.DeathProtection;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pd.guimx.commands.MainCommand;
import pd.guimx.config.MainConfigManager;
import pd.guimx.listeners.EntityListener;
import pd.guimx.listeners.PlayerListener;
import pd.guimx.utils.ManageDatabase;
import pd.guimx.utils.Miscellaneous;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        if (getMainConfigManager().getDay() > 29) {
            registerCustomRecipes();
        }
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

    private void registerCustomRecipes(){
        ItemStack heartItem = new ItemStack(Material.CLOCK);
        ItemMeta heartMeta = heartItem.getItemMeta();
        heartMeta.setDisplayName(Miscellaneous.translateColor("&cHeart"));
        heartMeta.setRarity(ItemRarity.EPIC);
        heartMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        heartMeta.setItemModel(NamespacedKey.minecraft("heart"));
        heartItem.setItemMeta(heartMeta);
        FoodProperties.Builder food = FoodProperties.food()
                .canAlwaysEat(true)
                .nutrition(0)
                .saturation(0);
        Consumable.Builder consumable = Consumable.consumable()
                .consumeSeconds(5)
                .sound(Key.key("minecraft:block.beacon.activate"));
        heartItem.setData(DataComponentTypes.FOOD,food);
        heartItem.setData(DataComponentTypes.CONSUMABLE,consumable);
        heartItem.setLore(new ArrayList<>(){{
            add(Miscellaneous.translateColor("&c+1 ❤"));
        }});

        ShapedRecipe heart = new ShapedRecipe(new NamespacedKey(this,"heart"),heartItem);

        //3 diamond blocks on top, diamond block-redstone-diamond block second layer, 3 diamond blocks final layer
        heart.shape("DDD","DRD","DDD");
        heart.setIngredient('D',Material.DIAMOND_BLOCK);
        heart.setIngredient('R',Material.REDSTONE);

        Bukkit.addRecipe(heart,true);

        /* Doesn't keep enchantments so... just hardcoded the craft lul
        ItemStack buffedElytraItem = new ItemStack(Material.ELYTRA);
        ItemMeta buffedElytraMeta = buffedElytraItem.getItemMeta();
        //buffedElytraMeta.addAttributeModifier(Attribute.GENERIC_GRAVITY,new AttributeModifier(new NamespacedKey(this,"buffed_elytra_gravity"),-0.07,AttributeModifier.Operation.ADD_NUMBER));
        //buffedElytraMeta.addAttributeModifier(Attribute.GENERIC_SAFE_FALL_DISTANCE,new AttributeModifier(new NamespacedKey(this,"buffed_elytra_fall"),3.0,AttributeModifier.Operation.ADD_NUMBER));
        buffedElytraMeta.setRarity(ItemRarity.EPIC);
        buffedElytraMeta.setDisplayName(Miscellaneous.translateColor("&6Super Elytra"));
        buffedElytraMeta.setFireResistant(true);
        buffedElytraItem.setItemMeta(buffedElytraMeta);

        ShapedRecipe buffedElytra = new ShapedRecipe(new NamespacedKey(this,"buffed_elytra"),buffedElytraItem);
        buffedElytra.shape("FFF","FEF","FFF");
        buffedElytra.setIngredient('F',Material.FEATHER);
        buffedElytra.setIngredient('E',Material.ELYTRA);

        Bukkit.addRecipe(buffedElytra,true);
         */
        ItemStack jesusTotemItem = new ItemStack(Material.CLOCK); //not a totem because it could be used to craft yet another totem
        DeathProtection.Builder jesusTotemDeathProtection = DeathProtection.deathProtection()
                        .addEffects(List.of(
                                ConsumeEffect.clearAllStatusEffects(),
                                ConsumeEffect.applyStatusEffects(List.of(
                                     new PotionEffect(PotionEffectType.ABSORPTION,20*5,1),
                                     new PotionEffect(PotionEffectType.REGENERATION,20*45,1),
                                     new PotionEffect(PotionEffectType.FIRE_RESISTANCE,20*40,0),
                                     new PotionEffect(PotionEffectType.SPEED,20*10,0) //why not
                                ),1)));
        jesusTotemItem.setData(DataComponentTypes.DEATH_PROTECTION,jesusTotemDeathProtection);
        ItemMeta jesusTotemMeta = jesusTotemItem.getItemMeta();
        jesusTotemMeta.setMaxStackSize(1);
        jesusTotemMeta.setDisplayName(Miscellaneous.translateColor("&dJesus Totem"));
        jesusTotemMeta.setLore(List.of(Miscellaneous.translateColor(getMainConfigManager().getMessages().get("jesus_totem"))));
        jesusTotemMeta.setItemModel(NamespacedKey.minecraft("jesus_totem"));
        jesusTotemMeta.setRarity(ItemRarity.EPIC);
        jesusTotemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        jesusTotemItem.setItemMeta(jesusTotemMeta);
        ShapelessRecipe jesusTotem = new ShapelessRecipe(new NamespacedKey(this,"jesus_totem_craft"),jesusTotemItem);
        jesusTotem.addIngredient(Material.TOTEM_OF_UNDYING);
        jesusTotem.addIngredient(Material.TOTEM_OF_UNDYING);

        Bukkit.addRecipe(jesusTotem,true);
    }
}
