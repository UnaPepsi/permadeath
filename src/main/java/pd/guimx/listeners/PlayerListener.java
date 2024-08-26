package pd.guimx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;

import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;


public class PlayerListener implements Listener{

    Permadeath permadeath;
    public PlayerListener(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e){
        OfflinePlayer player = Bukkit.getOfflinePlayer(e.getUniqueId());
        if (player.isBanned()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(MessageUtils.translateColor("&c&lPERMADEATH!&r\nYou have died and are no longer allowed to join back"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.getPlayer().setResourcePack("https://fun.guimx.me/r/permadeath.zip?compress=false",
              HexFormat.of().parseHex("4d3fc2dc8ed02584f88320a6dcc7081a2881e1a5"),"This is used for death sounds. Please accept :)",false);
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED || status == Status.DISCARDED){
            e.getPlayer().kickPlayer(MessageUtils.translateColor(Permadeath.prefix+"please accept the resource pack."));
        }


    }
    @EventHandler
    public void onDeath(EntityDamageEvent e){
        if (e.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                if (player.isDead()) {
                    player.spigot().respawn();
                    player.setGameMode(GameMode.SPECTATOR);
                    String title = MessageUtils.translateColor("&cPERMADEATH!");
                    String subtitle = player.getName()+" has died!";
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.sendTitle(title,subtitle,10,100,10);
                        //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                        p.playSound(p,"custom:permadeath",5,1);
                    });
                    String reason = MessageUtils.translateColor("&c&lPERMADEATH&r\nYou have died. GGWP");
                    Instant time = Instant.ofEpochSecond(Instant.now().getEpochSecond()+(86400*365*50));
                    Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                        player.ban(reason,time,null,true);
                    }, 150L);
                }
            }, 1L);
        }
    }
}
