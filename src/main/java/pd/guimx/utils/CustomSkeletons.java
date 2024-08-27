package pd.guimx.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class CustomSkeletons {
    public static LivingEntity spawnSkellyFullDiamond(World world, Location location) {
        LivingEntity rider = (LivingEntity) world.spawn(location, EntityType.SKELETON.getEntityClass(), spawnedEntity -> {
            Skeleton skelly = (Skeleton) spawnedEntity;
            skelly.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            skelly.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            skelly.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            skelly.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        });
        return rider;
    }
    public static LivingEntity spawnSkellyWitherBow(World world, Location location) {
        LivingEntity rider = (LivingEntity) world.spawn(location, EntityType.WITHER_SKELETON.getEntityClass(), spawnedEntity -> {
            WitherSkeleton skelly = (WitherSkeleton) spawnedEntity;
            skelly.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
            skelly.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            skelly.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            skelly.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
            ItemStack bow = new ItemStack(Material.BOW);
            bow.addUnsafeEnchantment(Enchantment.PUNCH,20);
            skelly.getEquipment().setItemInMainHand(bow);
            skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            skelly.setHealth(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        });
        return rider;
    }
}
