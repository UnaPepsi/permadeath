package pd.guimx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;

import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;


public class PlayerListener implements Listener{

    Permadeath permadeath;
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
              HexFormat.of().parseHex("4d3fc2dc8ed02584f88320a6dcc7081a2881e1a5"),"This is used for death sounds. Please accept :)",false);
        player.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&aCurrently in day: &c"+permadeath.getMainConfigManager().getDay()));
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED || status == Status.DISCARDED){
            e.getPlayer().kickPlayer(MessageUtils.translateColor(Permadeath.prefix+"please accept the resource pack."));
        }


    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            player.spigot().respawn();
            player.setGameMode(GameMode.SPECTATOR);
            String title = MessageUtils.translateColor("&cPERMADEATH!");
            String subtitle = player.getName()+" has died!";
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendTitle(title,subtitle,10,100,10);
                //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                p.playSound(p,"custom:permadeath",5,1);
            });
            World world = Objects.requireNonNull(Bukkit.getWorld("world"));
            world.setStorm(true);
            world.setThundering(true);
            world.setWeatherDuration(3600);
            world.setThunderDuration(3600);
            String reason = MessageUtils.translateColor("&c&lPERMADEATH&r\nYou have died. GGWP");
            permadeath.getDb().banOrUnbanPlayer(player.getName(),true);
            Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                player.kickPlayer(reason);}, 150L);
            }, 1L);
    }
}
