package pd.guimx.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Miscellaneous {
    public static String translateColor(String message, Object... args){
        return ChatColor.translateAlternateColorCodes('&',String.format(message,args));
    }

    public static void rotateBlocksInEntity(Permadeath plugin, Entity entity, BlockType blockType, int blockAmount, long ticksDuration, double radius){
        // blockAmount of 20 looks good
        BlockType[] blockTypes = new BlockType[blockAmount];
        Arrays.fill(blockTypes, blockType);
        rotateBlocksInEntity(plugin,entity,blockTypes,ticksDuration,radius);
    }

    public static void rotateBlocksInEntity(Permadeath plugin, Entity entity, BlockType[] blockTypes, long ticksDuration, double radius){
        Entity[] entities =  new Entity[blockTypes.length];
        for (int i = 0; i < blockTypes.length; i++) {
            int finalI = i; //no clue why I have to do this
            entities[i] = entity.getWorld().spawn(entity.getLocation(), EntityType.FALLING_BLOCK.getEntityClass(), spawnedEntity -> {
                FallingBlock fallingBlock = (FallingBlock) spawnedEntity;
                fallingBlock.setGravity(false);
                fallingBlock.setBlockData(blockTypes[finalI].createBlockData());
                //fallingBlock.setVelocity(new Vector(Math.cos(finalI), 0, Math.sin(finalI)).normalize().multiply(radius / 50)); //50 works and I have no idea why
                fallingBlock.shouldAutoExpire(false); //Block doesn't destroy itself

                final double[] start = {finalI};
                Location center = entity.getLocation();
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        start[0] += 0.1;
                        fallingBlock.setVelocity(new Vector(
                                Math.cos(start[0])*radius,
                                0,
                                Math.sin(start[0])*radius
                        ).normalize().multiply(radius / 50));
                    }
                };
                runnable.runTaskTimer(plugin,0,1L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    runnable.cancel();
                    Vector direction = center.toVector().subtract(fallingBlock.getLocation().toVector()).normalize();
                    fallingBlock.setVelocity(direction);
                    //EntityMoveEvent teleportEvent = new EntityMoveEvent(fallingBlock,fallingBlock.getLocation(),);
                    //Bukkit.getPluginManager().callEvent(teleportEvent);
                    Bukkit.getScheduler().runTaskLater(plugin, fallingBlock::remove, 20L);
                },ticksDuration);
            });
        }
    }

    public static void rotateEntitiesInLocation(Permadeath plugin, List<Entity> entities, Location target, double radius, long duration) {
        List<Entity> entitiesCopy = new ArrayList<>(entities);

        for (int i = 0; i < entitiesCopy.size(); i++) {
            final double[] start = {i};
            final int finalI = i;

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    start[0] += 0.1;
                    Vector velocity = new Vector(
                            Math.cos(start[0]) * radius,
                            0,
                            Math.sin(start[0]) * radius
                    ).normalize().multiply(Math.max(radius, 0.0001) / 50);

                    if (Double.isFinite(velocity.getX()) && Double.isFinite(velocity.getY()) && Double.isFinite(velocity.getZ())) {
                        Entity entity = entitiesCopy.get(finalI);
                        if (entity != null && entity.isValid()) {
                            entity.setVelocity(velocity);
                        } else {
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            };
            runnable.runTaskTimer(plugin, 0, 1L);

            if (duration != -1) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    runnable.cancel();
                    Entity entity = entitiesCopy.get(finalI);
                    if (entity != null && entity.isValid()) {
                        Vector direction = target.toVector().subtract(entity.getLocation().toVector())
                                .multiply(2).normalize().setY(0.5);
                        entity.setVelocity(direction);
                        Bukkit.getScheduler().runTaskLater(plugin, entity::remove, 20L);
                    }
                }, duration);
            }
        }
    }

    public static void orbitEntitiesFollowEntity(Permadeath plugin, Entity entity, EntityType entityType, int amount, double scaleMultiplier, Supplier<Boolean> check){
        ArrayList<Entity> entities = new ArrayList<>();
        for (int i = 0; i < amount; i++){
            entities.add(entity.getWorld().spawn(entity.getLocation().add(0,1,0),entityType.getEntityClass(),spawnedEntity -> {
                if (spawnedEntity instanceof  LivingEntity livingEntity) {
                    livingEntity.setNoPhysics(true);
                    livingEntity.setCollidable(false);
                    livingEntity.setInvulnerable(true);
                    livingEntity.setGravity(false);
                    livingEntity.setSilent(true);
                    livingEntity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(livingEntity.getAttribute(Attribute.GENERIC_SCALE).getValue() * scaleMultiplier);
                    if (spawnedEntity instanceof ArmorStand armorStand){
                        armorStand.setGravity(true);
                        armorStand.setInvisible(true);
                        armorStand.setNoPhysics(true);
                        armorStand.setItemInHand(new ItemStack(Material.GOLD_BLOCK));
                    }
                }
                //livingEntity.setAI(false);
            }));
            final double angleOffset = i * (2 * Math.PI / amount);
            final int finalI = i;
            BukkitRunnable runnable = new BukkitRunnable() {
                double angle = angleOffset;
                @Override
                public void run() {
                    if (!check.get()){
                        entities.get(finalI).remove();
                        cancel();
                    }
                    angle += 0.05;
                    //entities.get(finalI).setVelocity(new Vector(
                    //        Math.cos(angle)*(entity.getWidth()+0.5),
                    //        0,
                    //        Math.sin(angle)*(entity.getWidth()+0.5)
                    //).normalize());
                    //double offset = entities.get(finalI) instanceof ArmorStand ? -0.1 : 0;
                    Vector orbitVector = new Vector(
                            Math.cos(angle)*(entity.getWidth()/2),
                            0,
                            Math.sin(angle)*(entity.getWidth()/2)).subtract(entities.get(finalI).getLocation().add(0,-1.9,0).toVector().subtract(entity.getLocation().toVector()));
                    entities.get(finalI).setVelocity(orbitVector);
                }
            };
            runnable.runTaskTimer(plugin,0,1);
        }
    }

    public static void endermanPlayer(Player player,ProtocolManager protocolManager) {
        Enderman enderman = (Enderman) player.getWorld().spawn(player.getLocation(), EntityType.ENDERMAN.getEntityClass());
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.CAMERA);
        packetContainer.getIntegers().write(0, enderman.getEntityId());
        protocolManager.sendServerPacket(player, packetContainer);
        enderman.remove();
        Location location = player.getEyeLocation();
        player.teleport(new Location(Bukkit.getWorld("pd_void"), 0, 100, 0));
        player.teleport(location);
    }

    public static void guardianJumpscare(Permadeath plugin, Player player){
        for (int i = 0; i < 50; i++){
            Bukkit.getScheduler().runTaskLater(plugin,() -> {
                player.spawnParticle(Particle.ELDER_GUARDIAN,player.getLocation(),10);
                player.playSound(player,Sound.ENTITY_ELDER_GUARDIAN_CURSE,1,1);
            },i);
        }
    }
}