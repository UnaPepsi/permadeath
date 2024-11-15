package pd.guimx.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;
import pd.guimx.utils.Miscellaneous;
import pd.guimx.utils.Webhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static pd.guimx.listeners.EntityListener.random;


public class PlayerListener implements Listener{

    Permadeath permadeath;
    private boolean isDeathTrain = false;
    private int[] deathTrainSecondsRemaining = {-1};
    private final HashMap<Player, Boolean> playerElytraState = new HashMap<>();
    public PlayerListener(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    public boolean isDeathTrain(){
        return isDeathTrain;
    }
    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e){
        if (permadeath.getDb().userBanned(e.getName())){
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("player_banned")));
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        permadeath.getDb().addUser(player.getName(),permadeath.getMainConfigManager().getStartingLives());
        e.setJoinMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("player_joined"),
                player.getName(),permadeath.getDb().getLifes(player.getName())));
        player.setResourcePack("https://fun.guimx.me/r/permadeath.zip?compress=false",
              HexFormat.of().parseHex("abfaa1a8b810e81f85aa542166aaa8950f19c7c7"), Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("texture_pack")),false);
        player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("current_day"),
                permadeath.getMainConfigManager().getDay()));
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED){
            e.getPlayer().kickPlayer(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("texture_pack_denied")));
        }


    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        this.isDeathTrain = true;
        Location location = player.getLocation();

        String coordinates = String.format("x: %d, y: %d, z: %d",
                location.getBlockX(),location.getBlockY(),location.getBlockZ());
        e.setDeathMessage(e.getDeathMessage()+" | "+coordinates);

        Block block = location.getBlock();
        block.setType(Material.NETHER_BRICK_FENCE);

        location.setY(location.getBlockY()+1);
        block = location.getBlock();
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();
        skull.setOwnerProfile(player.getPlayerProfile());
        skull.update();

        location.setY(location.getBlockY()-2);
        location.getBlock().setType(Material.BEDROCK);

        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            player.spigot().respawn();

            Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+
                            permadeath.getMainConfigManager().getMessages().get("death_train_enabled"),
                        permadeath.getMainConfigManager().getDeathTrainSeconds()/3600));
            String title = Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_title"));
            String subtitle = String.format(permadeath.getMainConfigManager().getMessages().get("permadeath_subtitle"),player.getName());
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendTitle(title,subtitle,10,100,10);
                //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                p.playSound(p,"custom:permadeath",5,1);
            });

            String reason = Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_kick_reason"));
            int newLifes = permadeath.getDb().getLifes(player.getName());
            permadeath.getDb().setLifes(player.getName(),newLifes-1);

            if(newLifes-1 <= 0) {
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getScheduler().runTaskLater(permadeath, () -> player.kickPlayer(reason), 150L);
            }else{
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("remaining_lifes"),
                        newLifes-1));
            }
        }, 1L);

        World world = Objects.requireNonNull(Bukkit.getWorld("world"));
        if (deathTrainSecondsRemaining[0] > 0) {
            deathTrainSecondsRemaining[0] = permadeath.getMainConfigManager().getDeathTrainSeconds();
            Bukkit.getScheduler().runTaskLater(permadeath, () -> this.isDeathTrain = true,40L);
        }else {
            deathTrainSecondsRemaining[0] = permadeath.getMainConfigManager().getDeathTrainSeconds();
            Bukkit.getScheduler().runTaskTimer(permadeath, a -> {
                if (deathTrainSecondsRemaining[0] < 0) {
                    this.isDeathTrain = false;
                    world.setStorm(false);
                    world.setThundering(false);
                    a.cancel();
                    return;
                }
                if (world.isClearWeather()){
                    world.setStorm(true);
                    world.setThundering(true);
                    world.setWeatherDuration(deathTrainSecondsRemaining[0]);
                    world.setThunderDuration(deathTrainSecondsRemaining[0]);
                }
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Miscellaneous.translateColor(
                            permadeath.getMainConfigManager().getMessages().get("death_train"),
                            deathTrainSecondsRemaining[0] / 3600, (deathTrainSecondsRemaining[0] % 3600) / 60, deathTrainSecondsRemaining[0] % 60)
                    ));
                });
                deathTrainSecondsRemaining[0]--;
            }, 0, 20);
        }
        Bukkit.getScheduler().runTaskAsynchronously(permadeath, () -> {
            for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_died"),player.getName()),
                        e.getDeathMessage(), player.getName(),
                        permadeath.getMainConfigManager().getDay(), true);
            }
        });
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e){
        if (permadeath.getMainConfigManager().getDay() > 19){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("sleeping_disabled")));
            return;
        }
        if (isDeathTrain){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("sleeping_disabled_deathtrain")));
            return;
        }
        //Fix this for the love of god this is awful
        //Replying to the note above: im lazy as sh*t, too bad, it works for now
        if (permadeath.getMainConfigManager().getDay() < 10){
            return;
        }
        AtomicBoolean sleep = new AtomicBoolean(true);
        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            if (!e.getPlayer().isSleeping()){
                sleep.set(false);
            }
        },1L);
        if (!sleep.get()){
            return;
        }
        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            int[] sleepingPlayers = {0};
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (player.isSleeping()){
                    sleepingPlayers[0]++;
                }
            });
            if (sleepingPlayers[0] >= permadeath.getMainConfigManager().getMinPlayersSleep()) {
                Objects.requireNonNull(Bukkit.getWorld("world")).setTime(1000);
            }
        },100L);
    }

    @EventHandler
    public void onInventoryInteract(SmithItemEvent e){
        SmithingInventory inventory = e.getInventory();
        if (inventory.contains(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) &&
            (inventory.contains(Material.DIAMOND_HELMET) ||
            inventory.contains(Material.DIAMOND_CHESTPLATE) ||
            inventory.contains(Material.DIAMOND_LEGGINGS) ||
            inventory.contains(Material.DIAMOND_BOOTS))){
            e.getWhoClicked().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("upgrade_netherite_failed")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e){
        if (e.isCancelled() || !(e.getEntity() instanceof Player player)){
            return;
        }
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 29){
            int randomInt = random.nextInt(0,100);
            int probability = day > 33 ? 3 : 1;
            if (randomInt < probability){
                e.setCancelled(true);
                Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+
                                permadeath.getMainConfigManager().getMessages().get("totem_failed"),
                        player.getName()));
            }else{
                String totemWorked = String.format(permadeath.getMainConfigManager().getMessages().get("totem_worked"),
                        e.getEntity().getName(),randomInt+1,probability);
                Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+totemWorked));
                Bukkit.getScheduler().runTaskAsynchronously(permadeath,() -> {
                    for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                        Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_totem"),player.getName()),
                                totemWorked.replaceAll("&.",""), player.getName(),
                                permadeath.getMainConfigManager().getDay(), false);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Location loc = e.getTo().clone();
        loc.setY(e.getTo().getY()-0.1);
        if (loc.getBlock().getType() == Material.BEDROCK){

            //For some reason, even with a higher y vector, the velocity is the same, just that for some reason,
            //when I take damage, if I set the Vector to have a higher y the velocity applies multiple times.
            //My theory is that because I didn't reach the Vector I set, I get pushed until the number is reached
            e.getPlayer().setVelocity(new Vector(0,10,0));
        }
    }

    @EventHandler
    public void onRightCick(BlockPlaceEvent e){
        BlockData blockData = e.getBlockPlaced().getBlockData();
        if ((blockData instanceof Bed || blockData instanceof RespawnAnchor)
            && "world_the_end".equalsIgnoreCase(e.getPlayer().getWorld().getName())){
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("bed_anchor_disabled_end")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e){
        World world = e.getTo().getWorld();
        if ("world_the_end".equals(world.getName())){
            DragonBattle dragonBattle = world.getEnderDragonBattle();
            if (dragonBattle == null){
                return;
            }
            EnderDragon dragon = dragonBattle.getEnderDragon();
            if (dragon == null || dragon.getBossBar() == null){
                return;
            }
            if (!"&6&lPERMADEATH DEMON".equalsIgnoreCase(dragon.getBossBar().getTitle())){
                if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/dragon.getHealth() < 2){
                    dragon.getBossBar().setColor(BarColor.BLUE);
                }
                dragon.customName(Component.text(Miscellaneous.translateColor("&6&lPERMADEATH DEMON")));
            }
       }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e){
        Player player = e.getPlayer();
        e.setCancelled(true);
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        Team team = player.getScoreboard().getEntityTeam(player);
        String teamPrefix = team != null ? team.getDisplayName() : "";
        Bukkit.broadcastMessage(Miscellaneous.translateColor("%s&r%s: %s",teamPrefix,player.getName(),serializer.serialize(e.originalMessage())));
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e){
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (item.getItemMeta().getCustomModelData() == 69 &&
            item.getType() == Material.CLOCK &&
            item.getItemMeta().getFood().getEatSeconds() == 5
        ){
            if (maxHealth < 24) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth + 2);
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&ayou've gained 1 extra heart"));
            }else{
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&cyou've exceeded the amount of times you can consume this"));
                e.setCancelled(true);
                Miscellaneous.endermanPlayer(player, permadeath.getProtocolManager()); //xd
                Miscellaneous.guardianJumpscare(permadeath,player); //xd
            }
        }
    }

    @EventHandler
    public void onInventoryUpdate(PlayerInventorySlotChangeEvent e){
        Player player = e.getPlayer();
        ItemStack chestplate = player.getEquipment().getChestplate();
        boolean isWearingFireResistantElytra = chestplate != null
                && chestplate.getType() == Material.ELYTRA
                && chestplate.getItemMeta().isFireResistant();
        Boolean previousState = playerElytraState.get(player);
        //chestplate.getItemMeta().getAttributeModifiers().containsKey("fire_resistant")
        if (isWearingFireResistantElytra && (previousState == null || !previousState)){
            playerElytraState.put(player,true);
            player.getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(0.04);
            player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(Integer.MAX_VALUE);
            player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(Integer.MAX_VALUE);
            Miscellaneous.orbitEntitiesFollowEntity(permadeath,player, EntityType.ARMOR_STAND,16,0.2,() -> {
                ItemStack chestplateCheck = player.getEquipment().getChestplate();
                return chestplateCheck != null && chestplateCheck.getItemMeta().isFireResistant() && chestplateCheck.getType() == Material.ELYTRA;
            });
        }else if (!isWearingFireResistantElytra && (previousState == null || previousState)){
            playerElytraState.remove(player);
            player.getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(0.08);
            player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(3.0);
        }
    }

    @EventHandler
    public void onPreparedCraft(PrepareItemCraftEvent e){
        if (e.getInventory().getMaxStackSize() < 9) {return;}
        ItemStack[] items = e.getInventory().getMatrix();
        if (items.length < 9) {return;}
        for (int i = 0; i < 3; i++){
            if (items[i].getType() != Material.FEATHER || items[8-i].getType() != Material.FEATHER){
                return;
            }
        }
        if (items[3].getType() != Material.FEATHER || items[5].getType() != Material.FEATHER || items[4].getType() != Material.ELYTRA){
            return;
        }
        ItemMeta resultMeta = items[4].getItemMeta();
        resultMeta.setFireResistant(true);
        resultMeta.setDisplayName(Miscellaneous.translateColor("&6Super Elytra"));
        items[4].setItemMeta(resultMeta);
        e.getInventory().setResult(items[4]);
    }

    @EventHandler
    public void onRightClick(PlayerArmorStandManipulateEvent e){
        Bukkit.getLogger().info("event");
        ArmorStand stand = e.getRightClicked();
        if (stand.isInvisible() && stand.hasNoPhysics()){
            Bukkit.getLogger().info("asdjk");
            e.setCancelled(true);
        }else{
            Bukkit.getLogger().info(""+stand.isInvisible()+""+stand.hasNoPhysics());
        }
    }
}
