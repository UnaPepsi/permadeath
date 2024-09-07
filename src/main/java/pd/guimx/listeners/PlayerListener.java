package pd.guimx.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
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
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;
import pd.guimx.utils.Webhook;

import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static pd.guimx.listeners.EntityListener.random;


public class PlayerListener implements Listener{

    Permadeath permadeath;
    public boolean isDeathTrain = false;
    private int[] deathTrainSecondsRemaining = {-1};
    public PlayerListener(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e){
        if (permadeath.getDb().userBanned(e.getName())){
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(MessageUtils.translateColor(permadeath.getMainConfigManager().getMessages().get("player_banned")));
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        e.setJoinMessage(MessageUtils.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("player_joined"),
                player.getName()));
        permadeath.getDb().addUser(player.getName());
        player.setResourcePack("https://fun.guimx.me/r/permadeath.zip?compress=false",
              HexFormat.of().parseHex("abfaa1a8b810e81f85aa542166aaa8950f19c7c7"),MessageUtils.translateColor(permadeath.getMainConfigManager().getMessages().get("texture_pack")),false);
        player.sendMessage(MessageUtils.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("current_day"),
                permadeath.getMainConfigManager().getDay()));
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED){
            e.getPlayer().kickPlayer(MessageUtils.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("texture_pack_declined")));
        }


    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        this.isDeathTrain = true;
        Player player = e.getEntity();

        Location location = player.getLocation();
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
            player.setGameMode(GameMode.SPECTATOR);

            Bukkit.broadcastMessage(MessageUtils.translateColor(permadeath.prefix+
                            permadeath.getMainConfigManager().getMessages().get("death_train_enabled"),
                        permadeath.getMainConfigManager().getDeathTrainSeconds()/3600));
            String title = MessageUtils.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_title"));
            String subtitle = String.format(permadeath.getMainConfigManager().getMessages().get("permadeath_subtitle"),player.getName());
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendTitle(title,subtitle,10,100,10);
                //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                p.playSound(p,"custom:permadeath",5,1);
            });

            String reason = MessageUtils.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_kick_reason"));
            permadeath.getDb().banOrUnbanPlayer(player.getName(),true);
            Bukkit.getScheduler().runTaskLater(permadeath, () -> player.kickPlayer(reason), 150L);
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
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MessageUtils.translateColor(
                            permadeath.getMainConfigManager().getMessages().get("death_train"),
                            deathTrainSecondsRemaining[0] / 3600, (deathTrainSecondsRemaining[0] % 3600) / 60, deathTrainSecondsRemaining[0] % 60)
                    ));
                });
                deathTrainSecondsRemaining[0]--;
            }, 0, 20);
        }
        Bukkit.getScheduler().runTaskAsynchronously(permadeath, () -> {
            for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getDiscordWebhookDied(),player.getName()),
                        e.getDeathMessage(), player.getName(),
                        permadeath.getMainConfigManager().getDay(), true);
            }
        });
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e){
        if (permadeath.getMainConfigManager().getDay() > 19){
            e.setCancelled(true);
            e.getPlayer().sendMessage(MessageUtils.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("sleeping_disabled")));
            return;
        }
        if (isDeathTrain){
            e.setCancelled(true);
            e.getPlayer().sendMessage(MessageUtils.translateColor(permadeath.prefix+
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
            e.getWhoClicked().sendMessage(MessageUtils.translateColor(permadeath.prefix+
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
                Bukkit.broadcastMessage(MessageUtils.translateColor(permadeath.prefix+
                                permadeath.getMainConfigManager().getMessages().get("totem_failed"),
                        player.getName()));
            }else{
                String totemWorked = String.format(permadeath.getMainConfigManager().getMessages().get("totem_worked"),
                        e.getEntity().getName(),randomInt+1,probability);
                Bukkit.broadcastMessage(MessageUtils.translateColor(permadeath.prefix+totemWorked));
                Bukkit.getScheduler().runTaskAsynchronously(permadeath,() -> {
                    for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                        Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getDiscordWebhookTotem(),player.getName()),
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
            loc.add(0,60,0);

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
            e.getPlayer().sendMessage(MessageUtils.translateColor(permadeath.prefix+
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
            if (dragon != null && !"&6&lPERMADEATH DEMON".equalsIgnoreCase(dragon.getBossBar().getTitle())){
                if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/dragon.getHealth() < 2){
                    dragon.getBossBar().setColor(BarColor.BLUE);
                }
                dragon.customName(Component.text(MessageUtils.translateColor("&6&lPERMADEATH DEMON")));
            }
       }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e){
        Player player = e.getPlayer();
        e.setCancelled(true);
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        Bukkit.broadcastMessage(MessageUtils.translateColor("%s: %s",player.getName(),serializer.serialize(e.originalMessage())));
    }

}
