package pd.guimx.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;

import java.util.ArrayList;
import java.util.Arrays;

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

    public static void rotateEntitiesInLocation(Permadeath plugin, ArrayList<Entity> entities, Location target, double radius, long duration){
        ArrayList<Entity> entitiesCopy = new ArrayList<>(entities);
        for (int i = 0; i < entitiesCopy.size(); i++) {
            final double[] start = {i};
            final int finalI = i;
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    start[0] += 0.1;
                    entitiesCopy.get(finalI).setVelocity(new Vector(
                            Math.cos(start[0])*radius,
                            0,
                            Math.sin(start[0])*radius
                    ).normalize().multiply(radius / 50));
                }
            };
            runnable.runTaskTimer(plugin,0,1L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                runnable.cancel();
                //Vector direction = target.toVector().subtract(entitiesCopy.get(finalI).getLocation().toVector()).normalize();
                Vector direction = target.toVector().subtract(entitiesCopy.get(finalI).getLocation().toVector()).multiply(2).normalize().setY(0.5); //looks somewhat good and I have no idea why
                entitiesCopy.get(finalI).setVelocity(direction);
                Bukkit.getScheduler().runTaskLater(plugin, () -> entitiesCopy.get(finalI).remove(), 20L);},duration);
            }
        }
    public static void rotateArmorStandsInLocation3d(Permadeath plugin, Location target, int maxRadius, int amountPerEach){
        //doesn't work at all
        ArrayList<Entity> tempArray = new ArrayList<>();
        for (int i = maxRadius; i > 0; i--){
            for (int j = 1; j < amountPerEach; j++){
                tempArray.add(target.getWorld().spawn(target.add(0, i*0.01,0),EntityType.ARMOR_STAND.getEntityClass(), spawnedEntity -> {
                    ArmorStand armorStand = (ArmorStand) spawnedEntity;
                    //armorStand.setMarker(true); //this makes armor stands not be able to have velocity
                    armorStand.setNoPhysics(true);
                    armorStand.setInvisible(true);
                    armorStand.setItemInHand(new ItemStack(Material.DIAMOND_BLOCK));
                }));
            }
            rotateEntitiesInLocation(plugin,tempArray,target,i,10*20);
            tempArray.clear();
        }

        for (int i = maxRadius; i > 0; i--){
            for (int j = 1; j < amountPerEach; j++){
                tempArray.add(target.getWorld().spawn(target.subtract(0, i*0.01,0),EntityType.ARMOR_STAND.getEntityClass(), spawnedEntity -> {
                    ArmorStand armorStand = (ArmorStand) spawnedEntity;
                    //armorStand.setMarker(true); //this makes armor stands not be able to have velocity
                    armorStand.setNoPhysics(true);
                    armorStand.setInvisible(true);
                    armorStand.setItemInHand(new ItemStack(Material.DIAMOND_BLOCK));
                }));
            }
            rotateEntitiesInLocation(plugin,tempArray,target,i,10*20);
            tempArray.clear();
        }
    }


}