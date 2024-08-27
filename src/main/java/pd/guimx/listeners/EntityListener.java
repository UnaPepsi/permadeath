package pd.guimx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pd.guimx.Permadeath;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityListener implements Listener {
    private Random random;
    Permadeath permadeaht;
    private final List<PotionEffect> spiderEffects;
    public EntityListener(Permadeath permadeath){
        this.permadeaht = permadeath;
        this.random = new Random();
        spiderEffects = new ArrayList<>(){{
            add(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,3));
            add(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,4));
            add(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,4));
            add(new PotionEffect(PotionEffectType.JUMP_BOOST,Integer.MAX_VALUE,4));
            add(new PotionEffect(PotionEffectType.GLOWING,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.SLOW_FALLING,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.RESISTANCE,Integer.MAX_VALUE,3));
        }};
    }
    @EventHandler
    public void onSpawn(EntitySpawnEvent e){
        if (e.getEntity() instanceof Spider spider){

            List<PotionEffect> effects = new ArrayList<>();
            for (int i = 0; i < random.nextInt(1,4); i++){
                effects.add(spiderEffects.get(random.nextInt(spiderEffects.size())));
            }
            spider.addPotionEffects(effects);
            Bukkit.broadcastMessage(spider.getLocation().toString());
        }

    }
}
