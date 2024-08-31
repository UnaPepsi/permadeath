package pd.guimx.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.SmithingInventory;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;

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
            e.setKickMessage(MessageUtils.translateColor("&c&lPERMADEATH!&r\nYou have died and are no longer allowed to join back"));
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        e.setJoinMessage(MessageUtils.translateColor(Permadeath.prefix+"&c"+player.getName()+" has joined Hell."));
        permadeath.getDb().addUser(player.getName());
        player.setResourcePack("https://fun.guimx.me/r/permadeath.zip?compress=false",
              HexFormat.of().parseHex("abfaa1a8b810e81f85aa542166aaa8950f19c7c7"),"This is used for death sounds. Please accept :)",false);
        player.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&aCurrently in day: &c"+permadeath.getMainConfigManager().getDay()));
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED){
            e.getPlayer().kickPlayer(MessageUtils.translateColor(Permadeath.prefix+"please accept the resource pack."));
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

            String title = MessageUtils.translateColor("&cPERMADEATH!");
            String subtitle = player.getName()+" has died!";
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendTitle(title,subtitle,10,100,10);
                //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                p.playSound(p,"custom:permadeath",5,1);
                Bukkit.broadcastMessage(MessageUtils.translateColor(Permadeath.prefix+"&cDEATH TRAIN ACTIVE FOR %d HOUR(S)",
                        permadeath.getMainConfigManager().getDeathTrainSeconds()/3600));
            });

            String reason = MessageUtils.translateColor("&c&lPERMADEATH&r\nYou have died. GGWP");
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
                            String.format("&7Death Train: %02d:%02d:%02d", deathTrainSecondsRemaining[0] / 3600, (deathTrainSecondsRemaining[0] % 3600) / 60, deathTrainSecondsRemaining[0] % 60)
                    )));
                    deathTrainSecondsRemaining[0]--;
                });
            }, 0, 20);
        }

    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e){
        if (permadeath.getMainConfigManager().getDay() > 19){
            e.setCancelled(true);
            e.getPlayer().sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&csleeping is disabled"));
            return;
        }
        if (isDeathTrain){
            e.setCancelled(true);
            e.getPlayer().sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cyou can't sleep. It's Death Train"));
            return;
        }
        //Fix this for the love of god this is awful
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
        if (inventory.contains(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)){
            e.getWhoClicked().sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cyou can't do that!"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e){
        if (e.isCancelled()){
            return;
        }
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 29){
            int randomInt = random.nextInt(0,100);
            int probability = day > 33 ? 3 : 1;
            if (randomInt < probability){
                e.setCancelled(true);
                Bukkit.broadcastMessage(MessageUtils.translateColor(Permadeath.prefix+"&cHow unfortunate. %s's totem did not work. Oh well, too bad",
                        e.getEntity().getName()));
            }else{
                Bukkit.broadcastMessage(MessageUtils.translateColor(
                        Permadeath.prefix+"&7%s activated a Totem of Undying. Luckily, they have survived. (%d > %d)",
                        e.getEntity().getName(),randomInt+1,probability));
            }
        }
    }
}
