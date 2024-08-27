package pd.guimx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pd.guimx.Permadeath;
import pd.guimx.utils.CustomSkeletons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityListener implements Listener {
    private Random random;
    Permadeath permadeath;
    private final List<PotionEffect> spiderEffects;
    public EntityListener(Permadeath permadeath){
        this.permadeath = permadeath;
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
        int day = permadeath.getMainConfigManager().getDay();
        Entity entity = e.getEntity();
        if (entity instanceof Spider spider){
            if (day > 9){
                List<PotionEffect> effects = new ArrayList<>();
                int randInt;
                if (day > 19){
                    randInt = random.nextInt(1,4);
                }else{
                    randInt = random.nextInt(3,6);
                }
                for (int i = 0; i < randInt; i++){
                    effects.add(spiderEffects.get(random.nextInt(spiderEffects.size())));
                }
                spider.addPotionEffects(effects);
                if (day > 19){
                    switch (random.nextInt(1,3)){
                        case 1:
                            LivingEntity skellyWitherBow = CustomSkeletons.spawnSkellyWitherBow(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skellyWitherBow);
                            break;
                        case 2:
                            LivingEntity skelly = CustomSkeletons.spawnSkellyFullDiamond(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skelly);
                            break;
                    }
                }
            }
        }else if (entity instanceof Phantom phantom && day > 19){
            phantom.setSize(9);
            phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            phantom.setHealth(phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e){
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 19){
            Entity entity = e.getEntity();
            if (entity instanceof IronGolem ||
                entity instanceof PigZombie ||
                entity instanceof Ghast ||
                entity instanceof Guardian ||
                //entity instanceof MagmaCube || this a slime
                entity instanceof Enderman ||
                entity instanceof Witch ||
                entity instanceof WitherSkeleton ||
                entity instanceof Evoker ||
                entity instanceof Phantom ||
                entity instanceof Slime ||
                entity instanceof Drowned ||
                entity instanceof Blaze){
                e.getDrops().clear();
            }else if (entity instanceof Ravager){
                if (random.nextInt(0,100)==1) {
                    e.getDrops().add(new ItemStack(Material.TOTEM_OF_UNDYING));
                }
            }
        }
    }
}
