package pd.guimx.listeners;

import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;
import pd.guimx.utils.CustomEntities;
import pd.guimx.utils.Miscellaneous;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityListener implements Listener {
    public static Random random = new Random();
    Permadeath permadeath;
    private final List<PotionEffect> spiderEffects;
    private final List<PotionEffect> day21Potions;
    private final List<PotionEffect> ravagerEffects;
    private List<Location> enderCrystalLocations = new ArrayList<>();
    private boolean isEnderDragonEnraged = false;
    public EntityListener(Permadeath permadeath){
        this.permadeath = permadeath;
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
        day21Potions = new ArrayList<>(){{
            add(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.RESISTANCE,Integer.MAX_VALUE,1));
        }};
        ravagerEffects = new ArrayList<>(){{
            add(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1));
            add(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,2));
        }};


    }
    @EventHandler
    public void onSpawn(CreatureSpawnEvent e){
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
                if (day > 19 && (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER)){
                    LivingEntity skelly;
                    switch (random.nextInt(1,5)){
                        case 1:
                            skelly = CustomEntities.spawnSkellyWitherBow(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skelly);
                            break;
                        case 2:
                            skelly = CustomEntities.spawnSkellyFullDiamond(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skelly);
                            break;
                        case 3:
                            skelly = CustomEntities.spawnSkellyIronAxe(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skelly);
                            break;
                        case 4:
                            skelly = CustomEntities.spawnSkellyGodBow(spider.getWorld(),spider.getLocation());
                            spider.addPassenger(skelly);
                            break;
                    }
                }
            }
        }else if (entity instanceof Phantom phantom && day > 19){
            phantom.setSize(9);
            phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            phantom.setHealth(phantom.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }else if (entity instanceof MagmaCube magmaCube){
            if (day > 20) {
                magmaCube.setSize(16);
            }
        }else if (entity instanceof Slime slime){
            if (day > 20) {
                slime.setSize(15);
                slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
                slime.setHealth(slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
        }else if (entity instanceof Ravager ravager){
            if (day > 20) {
                ravager.addPotionEffects(ravagerEffects);
            }
        }else if (entity instanceof Ghast ghast){
            int baseHealth;
            if ("world_the_end".equalsIgnoreCase(ghast.getWorld().getName())){
                baseHealth = 100;
            }else{
                baseHealth = random.nextInt(40,61);
            }
            ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth);
            ghast.setHealth(ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }else if (entity instanceof Squid squid){
            if (day > 29) {
                AtomicBoolean spawn = new AtomicBoolean(true);
                squid.getNearbyEntities(30,30,30).forEach(nearbyEntity -> {
                    if(nearbyEntity.getType() == EntityType.GUARDIAN){
                        spawn.set(false);
                    }
                });
                if (spawn.get()) {
                    squid.getWorld().spawn(
                            squid.getLocation(), EntityType.GUARDIAN.getEntityClass(), spawned -> {
                                Guardian tempGuardian = (Guardian) spawned;
                                tempGuardian.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                            }
                    );
                }
                e.setCancelled(true);
            }
        }else if (entity instanceof Bat bat){
            if (day > 29) {
                AtomicBoolean spawn = new AtomicBoolean(true);
                bat.getNearbyEntities(30,30,30).forEach(nearbyEntity -> {
                    if(nearbyEntity.getType() == EntityType.BLAZE){
                        spawn.set(false);
                    }
                });
                if (spawn.get()) {
                    bat.getWorld().spawn(
                            bat.getLocation(), EntityType.BLAZE.getEntityClass(), spawned -> {
                                Blaze tempGuardian = (Blaze) spawned;
                                tempGuardian.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2));
                            }
                    );
                }
                e.setCancelled(true);
            }

        }else if (entity instanceof Pillager pillager){
            if (day > 29) {
                pillager.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,1));
                pillager.getInventory().remove(Material.CROSSBOW);
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE,10);
                pillager.getEquipment().setItemInMainHand(crossbow);
                pillager.getEquipment().setItemInMainHandDropChance(0);
            }
        }else if (entity instanceof Creeper creeper){
            if (day > 29) {
                creeper.setPowered(true);
            }
        }else if (entity instanceof Skeleton skelly){
            if (day > 29){
                switch (random.nextInt(1,4)){
                    case 1:
                        CustomEntities.getSkelly30FullDiamond(skelly);
                        break;
                    case 2:
                        CustomEntities.getSkelly30FireAxe(skelly);
                        break;
                    case 3:
                        CustomEntities.getSkelly30Crossbow(skelly);
                        break;
                }
            }
        }else if (entity instanceof WitherSkeleton skelly){
            if (day > 29){
                switch (random.nextInt(1,3)){
                    case 1:
                        CustomEntities.getSkelly30PunchAndPower(skelly);
                        break;
                    case 2:
                        CustomEntities.getSkelly30SuperPower(skelly);
                        break;
                }
            }
        }else if (entity instanceof PigZombie pigZombie){
            if (day > 29) {
                CustomEntities.setPigManFullDiamond(pigZombie);
            }
        }else if (entity instanceof IronGolem ironGolem){
            if (day > 29){
                ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,4));
            }
        }else if (entity instanceof Enderman enderman){
            if (day > 29) {
                enderman.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2));
                if (!"world_the_end".equals(enderman.getWorld().getName())) {
                    return;
                }
                int randInt = random.nextInt(0, 100);
                if (randInt < 2) {
                    AtomicBoolean spawnGhast = new AtomicBoolean(true);
                    enderman.getNearbyEntities(60,60,60).forEach(entityNear -> {
                        if (entityNear.getType() == EntityType.GHAST){
                            spawnGhast.set(false);
                        }
                    });
                    if (spawnGhast.get()) {
                        enderman.getWorld().spawn(enderman.getLocation(), EntityType.GHAST.getEntityClass(), spawnedEntity -> {
                            Location loc = spawnedEntity.getLocation().clone().add(0, 40, 0);
                            spawnedEntity.teleport(loc);
                        });
                        e.setCancelled(true);
                    }
                }else if (randInt < 15){
                    enderman.getWorld().spawn(enderman.getLocation(),EntityType.CREEPER.getEntityClass(), spawnedEntity -> {
                        Creeper creeper = (Creeper) spawnedEntity;
                        creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,1));
                    });
                    e.setCancelled(true);
                }
            }
        }else if (entity instanceof Silverfish silverfish) {
            if (day > 29) {
                for (int i = 0; i < 5; i++) {
                    silverfish.addPotionEffect(spiderEffects.get(random.nextInt(spiderEffects.size())));
                }
            }
        }else if (entity instanceof Endermite endermite){
            if (day > 29) {
                for (int i = 0; i < 5; i++) {
                    endermite.addPotionEffect(spiderEffects.get(random.nextInt(spiderEffects.size())));
                }
            }
        }else if (entity instanceof Zombie zombie){
            if (zombie.isBaby() && day > 29){
                zombie.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(zombie.getAttribute(Attribute.GENERIC_SCALE).getValue()/2);
            }
        }

        if (day > 20 && entity instanceof Enemy enemy && permadeath.getPlayerListener().isDeathTrain()){
            enemy.addPotionEffects(day21Potions);
        }

    }

    @EventHandler
    public void onDeath(EntityDeathEvent e){
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 19){
            Entity entity = e.getEntity();
            if (entity instanceof IronGolem ||
                entity instanceof PigZombie ||
                entity instanceof Guardian ||
                entity instanceof Enderman ||
                entity instanceof Witch ||
                entity instanceof WitherSkeleton ||
                entity instanceof Evoker ||
                entity instanceof Phantom ||
                entity instanceof Drowned ||
                entity instanceof Blaze){
                e.getDrops().clear();
            }else if (entity instanceof Ravager){
                if (random.nextInt(0,100) < (day > 20 ? 20 : 1)) {
                    e.getDrops().add(new ItemStack(Material.TOTEM_OF_UNDYING));
                }
            }else if (entity instanceof Slime){
                e.getDrops().clear();
                if (day > 20 && day < 30) {
                    if (entity instanceof MagmaCube) {
                        e.getDrops().add(new ItemStack(Material.NETHERITE_CHESTPLATE));
                    }else{
                        e.getDrops().add(new ItemStack(Material.NETHERITE_LEGGINGS));
                    }
                }
            }else if (entity instanceof Ghast){
                e.getDrops().clear();
                if (day > 20 && day < 30){
                    e.getDrops().add(new ItemStack(Material.NETHERITE_HELMET));
                }
            }else if (entity instanceof CaveSpider){
                e.getDrops().clear();
                if (day > 20 && day < 30){
                    e.getDrops().add(new ItemStack(Material.NETHERITE_BOOTS));
                }
            }else if (entity instanceof Shulker){
                entity.getWorld().createExplosion(entity.getLocation(),4,false);
                if (random.nextInt(0,100) > 19){
                    e.getDrops().clear();
                }
            }
        }
    }

    @EventHandler
    public void onProjectile(ProjectileHitEvent e){
        int day = permadeath.getMainConfigManager().getDay();
        if (!(e.getEntity().getShooter() instanceof Entity shooter)){
            return;
        }
        if (shooter instanceof Ghast){
            if (day > 20) {
                World world;
                Location location;
                if (e.getHitBlock() != null) {
                    world = e.getHitBlock().getWorld();
                    location = e.getHitBlock().getLocation();
                } else {
                    world = e.getHitEntity().getWorld();
                    location = e.getHitEntity().getLocation();
                }
                world.createExplosion(location, 4, true);
            }
        }
        if (shooter instanceof EnderDragon){
            Location location;
            if (e.getHitBlock() != null){
                location = e.getHitBlock().getLocation();
                location.getBlock().setType(Material.BEDROCK);
                if (isEnderDragonEnraged) {
                    location.add(6,5,0); //move the spider away from the dragon's breath
                    location.getWorld().spawn(location, EntityType.SPIDER.getEntityClass());
                }
            }
        }
    }
    @EventHandler
    public void onMove(EntityMoveEvent e) {
        if (permadeath.getMainConfigManager().getDay() < 20){
            return;
        }
        if (e.getEntity() instanceof Enderman){
            return;
        }
        if (e.getEntity() instanceof Creeper creeper){
            if ("world_the_end".equals(creeper.getWorld().getName())) {
                creeper.getWorld().spawnParticle(Particle.PORTAL, creeper.getEyeLocation(), 10);
            }
        }
        if (e.getEntity() instanceof Mob mob) {
            List<Player> nearbyPlayers = mob.getLocation().getNearbyPlayers(25,pred -> {
                EntityEquipment armor = pred.getEquipment();
                return ((pred.getGameMode() == GameMode.SURVIVAL || pred.getGameMode() == GameMode.ADVENTURE) &&
                        (!pred.hasPotionEffect(PotionEffectType.INVISIBILITY) ||
                         (armor.getHelmet() != null || armor.getChestplate() != null ||
                                armor.getLeggings() != null || armor.getBoots() != null)));
            }).stream().toList();
            if (nearbyPlayers.isEmpty()){
                return;
            }
            Player player = nearbyPlayers.getFirst();
            if (mob.getBoundingBox().overlaps(player.getBoundingBox()) && !(mob instanceof Monster)) {
                player.damage(2,mob);
            }

            if (mob instanceof Monster){
                if (mob instanceof PigZombie pigZombie){
                    pigZombie.setTarget(player);
                    pigZombie.setAngry(true);
                }
                return;
            }else if (mob instanceof Golem golem){
                if (golem instanceof IronGolem ironGolem){
                    if (ironGolem.isPlayerCreated()){
                        return;
                    }
                }
                golem.setTarget(player);
                golem.setAggressive(true);
            }
            mob.getPathfinder().moveTo(player);
            /*
            if (distance < 25) {
                mob.getPathfinder().moveTo(player);
            }else{
                mob.getPathfinder().stopPathfinding();
            }*/
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent e){
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 20){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        Entity entity = e.getEntity();
        if (entity instanceof EnderCrystal crystal && "world_the_end".equals(crystal.getWorld().getName()) && crystal.getWorld().getEnderDragonBattle() != null){
            enderCrystalLocations.add(crystal.getLocation());
            Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                entity.getWorld().spawn(entity.getLocation(), EntityType.GHAST.getEntityClass());
            },1);
            return;
        }
        if (entity instanceof EnderDragon dragon){
            if (dragon.getBossBar() == null){
                return;
            }
            if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/dragon.getHealth() >= 2){
                dragon.getBossBar().setColor(BarColor.RED);
                dragon.customName(Component.text(Miscellaneous.translateColor("&c&lENRAGED PERMADEATH DEMON")));
                this.isEnderDragonEnraged = true;
            }else{
                dragon.getBossBar().setColor(BarColor.BLUE);
                dragon.customName(Component.text(Miscellaneous.translateColor("&6&lPERMADEATH DEMON")));
                this.isEnderDragonEnraged = false;
            }
        }
        int day =  permadeath.getMainConfigManager().getDay();
        if (!(entity instanceof Creeper creeper) || creeper.getHealth() <= 0){
            return;
        }
        if (!"world_the_end".equalsIgnoreCase(entity.getWorld().getName())){
            return;
        }
        if (day > 29){
            if (random.nextDouble() < 0.33 || e.getDamageSource().getDamageType() == DamageType.ARROW) {
                Location loc = creeper.getLocation();
                Location teleportLocation;
                Entity attacker = e.getDamageSource().getCausingEntity();
                if (attacker != null && e.getDamageSource().getDamageType() == DamageType.ARROW && random.nextDouble() < 0.2){
                    teleportLocation = attacker.getLocation();
                    if (attacker instanceof Player player){
                        player.playSound(player,"custom:scream",5,1); //xd
                    }
                }else {
                    teleportLocation = loc.clone().add(random.nextInt(-30,30), 0, random.nextInt(-30,30));
                    teleportLocation = creeper.getWorld().getHighestBlockAt(teleportLocation).getLocation();
                    teleportLocation.setY(teleportLocation.getY() + 1);
                }

                if (teleportLocation.getBlockY() <= -64){ //void
                    return;
                }

                EntityTeleportEvent teleportEvent = new EntityTeleportEvent(creeper, loc, teleportLocation);
                Bukkit.getPluginManager().callEvent(teleportEvent);

                if (!teleportEvent.isCancelled()) {
                    //Bukkit.broadcastMessage(teleportLocation.getBlock().toString()+teleportLocation.getBlockY());
                    e.setCancelled(true);
                    creeper.getWorld().playSound(creeper.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,5,1);
                    creeper.getWorld().spawnParticle(Particle.PORTAL,creeper.getEyeLocation(),100);
                    creeper.teleport(teleportEvent.getTo());
                }
            }
        }
    }

    @EventHandler
    public void onDragonPhaseChange(EnderDragonChangePhaseEvent e) {
        EnderDragon dragon = e.getEntity();
        if (dragon.getDragonBattle() == null && dragon.getDragonBattle().getEndPortalLocation() == null){
            return;
        }
        EnderDragon.Phase phase = e.getCurrentPhase();
        if (phase == EnderDragon.Phase.LAND_ON_PORTAL) {
            //e.getEntity().getDragonBattle().resetCrystals();
            if (!enderCrystalLocations.isEmpty() && random.nextFloat() <= 0.1) {
                for (Location location : enderCrystalLocations) {
                    dragon.getWorld().spawn(location, EntityType.END_CRYSTAL.getEntityClass());
                }
                enderCrystalLocations.clear();
            }else if (random.nextFloat() <= 0.2 && dragon.getHealth() < dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()*0.7){
                BukkitScheduler scheduler = Bukkit.getScheduler();
                ArrayList<Entity> entities = new ArrayList<>();
                for (int i = 0; i < 50; i++){
                        ArmorStand stand = (ArmorStand) dragon.getWorld().spawn(dragon.getDragonBattle().getEndPortalLocation().add(0,5,0),EntityType.ARMOR_STAND.getEntityClass(), a -> {
                            ArmorStand armorStand = (ArmorStand) a;
                            //armorStand.setMarker(true); //this makes armor stands not be able to have velocity
                            armorStand.setNoPhysics(true);
                            armorStand.setInvisible(true);
                            armorStand.setCollidable(false);
                            armorStand.setItemInHand(new ItemStack(Material.DIAMOND_BLOCK));
                        });
                        entities.add(stand);
                    scheduler.runTaskLater(permadeath, () -> {
                        if (!dragon.isDead() && dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() > dragon.getHealth()+1){
                            dragon.setHealth(dragon.getHealth()+1);
                            if (dragon.getBossBar() == null){
                                return;
                            }
                            if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/dragon.getHealth() >= 2){
                                dragon.getBossBar().setColor(BarColor.RED);
                                dragon.customName(Component.text(Miscellaneous.translateColor("&c&lENRAGED PERMADEATH DEMON")));
                                this.isEnderDragonEnraged = true;
                            }else{
                                dragon.getBossBar().setColor(BarColor.BLUE);
                                dragon.customName(Component.text(Miscellaneous.translateColor("&6&lPERMADEATH DEMON")));
                                this.isEnderDragonEnraged = false;
                            }
                        }
                    },i/4*20);
                }
                Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("dragon_heal")));
                Miscellaneous.rotateEntitiesInLocation(permadeath,entities,dragon.getDragonBattle().getEndPortalLocation().add(0,5,0),30,4*20);
            }else{
                dragon.getWorld().spawn(dragon.getLocation(), EntityType.ENDERMITE.getEntityClass());
            }
        }else if (phase == EnderDragon.Phase.CIRCLING) {
            float randFloat = random.nextFloat();
            if (randFloat < 0.20) {
                float anotherRandomFloat = random.nextFloat(); //shush
                if (anotherRandomFloat <= 0.5) {
                    dragon.getWorld().getPlayers().forEach(p -> {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 5, 1));
                        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                            p.getWorld().spawn(p.getLocation(), EntityType.AREA_EFFECT_CLOUD.getEntityClass(), spawnedCloud -> {
                                AreaEffectCloud areaEffectCloud = (AreaEffectCloud) spawnedCloud;
                                areaEffectCloud.setRadius(2.5f);
                                areaEffectCloud.setParticle(Particle.DAMAGE_INDICATOR);
                                areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2), true);
                            });
                        }, 20 * 5);
                    });
                }else{
                    dragon.getWorld().getPlayers().forEach(p -> {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20 * 5, 1));
                        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                            p.getWorld().spawn(p.getLocation(), EntityType.AREA_EFFECT_CLOUD.getEntityClass(), spawnedCloud -> {
                                AreaEffectCloud areaEffectCloud = (AreaEffectCloud) spawnedCloud;
                                areaEffectCloud.setRadius(3);
                                areaEffectCloud.setParticle(Particle.RAID_OMEN);
                                areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 10, 2), true);
                                areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 10, 5), true);
                                areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 255),true);
                            });
                        }, 20 * 5);
                    });
                }
            }else if (randFloat < 0.5) {
                List<Location> locations = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    if (i % 2 == 0) {
                        locations.add(dragon.getLocation().clone().add(i - 3, 0, 0));
                    } else {
                        locations.add(dragon.getLocation().clone().add(0, 0, i - 3));
                    }
                }
                for (Location location : locations) {
                    dragon.getWorld().spawn(location, EntityType.TNT.getEntityClass(), spawnedEntity -> {
                        TNTPrimed tntPrimed = (TNTPrimed) spawnedEntity;
                        tntPrimed.setSource(dragon);
                        tntPrimed.setYield(8);
                    });
                }
            } else {
                for (int i = 1; i < 11; i++) {
                    Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                        Location location = dragon.getLocation().clone();
                        location.setY(location.getWorld().getHighestBlockYAt(location));
                        dragon.getWorld().spawn(location, EntityType.LIGHTNING_BOLT.getEntityClass());
                        dragon.getWorld().spawn(location, EntityType.TNT.getEntityClass(),spawnedEntity -> {
                            TNTPrimed tntPrimed = (TNTPrimed) spawnedEntity;
                            tntPrimed.setSource(dragon);
                            tntPrimed.setYield(4);
                            tntPrimed.setFuseTicks(1);
                        });
                    },i*20);
                }
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e){
        Entity entity = e.getEntity();
        if (entity instanceof Creeper creeper){
            Bukkit.getScheduler().runTaskLater(permadeath,() -> {
                creeper.getNearbyEntities(5,5,5).forEach(nearbyEntity -> {
                    if (nearbyEntity instanceof AreaEffectCloud areaEffectCloud){
                        areaEffectCloud.remove();
                    }
                });
            },1);
            return;
        }
        if (entity instanceof TNTPrimed tntPrimed && tntPrimed.getSource() instanceof EnderDragon){
            List<Block> blockList = e.blockList();
            blockList.forEach(block -> {
                Location location;
                location = block.getLocation();
                block.getWorld().spawn(location, EntityType.FALLING_BLOCK.getEntityClass(),spawnedEntity -> {
                    FallingBlock fallingBlock = (FallingBlock) spawnedEntity;
                    fallingBlock.setBlockData(block.getBlockData());
                    fallingBlock.setVelocity(new Vector(random.nextFloat(-1,1),1,
                            random.nextFloat(-1,1)));
                });
            });
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent e){
        if (e.getEntity() instanceof EnderDragon dragon && dragon.getBossBar() != null){
            if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/dragon.getHealth() >= 2){
                dragon.getBossBar().setColor(BarColor.RED);
                dragon.customName(Component.text(Miscellaneous.translateColor("&c&lENRAGED PERMADEATH DEMON")));
                this.isEnderDragonEnraged = true;
            }else{
                dragon.getBossBar().setColor(BarColor.BLUE);
                dragon.customName(Component.text(Miscellaneous.translateColor("&6&lPERMADEATH DEMON")));
                this.isEnderDragonEnraged = false;
            }
        }
    }

    @EventHandler
    public void onDragonPush(EntityPushedByEntityAttackEvent e){
        if (e.getPushedBy() instanceof EnderDragon && e.getEntity() instanceof ArmorStand stand && stand.isInvisible()){
            e.setCancelled(true);
        }
    }
}
