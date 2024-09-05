package pd.guimx.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomEntities {
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
            skelly.getEquipment().setItemInMainHandDropChance(0);
        });
        return rider;
    }

    public static LivingEntity spawnSkellyIronAxe(World world, Location location){
        LivingEntity rider = (LivingEntity) world.spawn(location, EntityType.SKELETON.getEntityClass(), spawnedEntity -> {
            Skeleton skelly = (Skeleton) spawnedEntity;
            skelly.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            skelly.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            skelly.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            skelly.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
            ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
            axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT,2);
            skelly.getEquipment().setItemInMainHand(axe);
        });
        return rider;
    }
    public static LivingEntity spawnSkellyCrossBow(World world, Location location){
        LivingEntity rider = (LivingEntity) world.spawn(location, EntityType.SKELETON.getEntityClass(), spawnedEntity -> {
            Skeleton skelly = (Skeleton) spawnedEntity;
            skelly.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            skelly.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            skelly.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
            skelly.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
            ItemStack crossbow = new ItemStack(Material.CROSSBOW);
            crossbow.addUnsafeEnchantment(Enchantment.SHARPNESS,20);
            skelly.getEquipment().setItemInMainHand(crossbow);
            skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            skelly.setHealth(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            skelly.getEquipment().setItemInMainHandDropChance(0);
        });
        return rider;
    }
    public static LivingEntity spawnSkellyGodBow(World world, Location location){
        LivingEntity rider = (LivingEntity) world.spawn(location, EntityType.SKELETON.getEntityClass(), spawnedEntity -> {
            Skeleton skelly = (Skeleton) spawnedEntity;
            skelly.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            skelly.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            skelly.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            skelly.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            ItemStack crossbow = new ItemStack(Material.BOW);
            crossbow.addUnsafeEnchantment(Enchantment.PUNCH,10);
            skelly.getEquipment().setItemInMainHand(crossbow);
            skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            skelly.setHealth(skelly.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            skelly.getEquipment().setItemInMainHandDropChance(0);
        });
        return rider;
    }

    public static Skeleton getSkelly30FullDiamond(Skeleton skelly){
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        helmet.addUnsafeEnchantment(Enchantment.PROTECTION,4);
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION,4);
        leggings.addUnsafeEnchantment(Enchantment.PROTECTION,4);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION,4);
        skelly.getEquipment().setHelmet(helmet);
        skelly.getEquipment().setChestplate(chestplate);
        skelly.getEquipment().setLeggings(leggings);
        skelly.getEquipment().setBoots(boots);
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta potionMeta = (PotionMeta) arrow.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE,1,1),true);
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION,20*5,1),true); //thanks tokaua for the idea lol
        arrow.setItemMeta(potionMeta);
        skelly.getEquipment().setItemInOffHand(arrow);
        return skelly;
    }

    public static Skeleton getSkelly30FireAxe(Skeleton skelly){
        skelly.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        skelly.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        skelly.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        skelly.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT,10);
        skelly.getEquipment().setItemInMainHand(axe);
        return skelly;
    }
    public static Skeleton getSkelly30Crossbow(Skeleton skelly){
        skelly.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        skelly.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        skelly.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        skelly.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        ItemStack crossbow = new ItemStack(Material.BOW);
        crossbow.addUnsafeEnchantment(Enchantment.SHARPNESS,25);
        skelly.getEquipment().setItemInMainHand(crossbow);
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta potionMeta = (PotionMeta) arrow.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE,1,1),true);
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION,20*5,1),true); //thanks tokaua for the idea lol
        arrow.setItemMeta(potionMeta);
        skelly.getEquipment().setItemInOffHand(arrow);
        return skelly;
    }

    public static WitherSkeleton getSkelly30PunchAndPower(WitherSkeleton skelly){
        skelly.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        skelly.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        skelly.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        skelly.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.PUNCH,30);
        bow.addUnsafeEnchantment(Enchantment.POWER,25);
        skelly.getEquipment().setItemInMainHand(bow);
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta potionMeta = (PotionMeta) arrow.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE,1,1),true);
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION,20*5,1),true); //thanks tokaua for the idea lol
        arrow.setItemMeta(potionMeta);
        skelly.getEquipment().setItemInOffHand(arrow);
        return skelly;
    }
    public static WitherSkeleton getSkelly30SuperPower(WitherSkeleton skelly){
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = ((LeatherArmorMeta) helmet.getItemMeta());
        meta.setColor(Color.RED);
        helmet.setItemMeta(meta);
        chestplate.setItemMeta(meta);
        leggings.setItemMeta(meta);
        boots.setItemMeta(meta);
        skelly.getEquipment().setHelmet(helmet);
        skelly.getEquipment().setChestplate(chestplate);
        skelly.getEquipment().setLeggings(leggings);
        skelly.getEquipment().setBoots(boots);
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.POWER,50);
        skelly.getEquipment().setItemInMainHand(bow);
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta potionMeta = (PotionMeta) arrow.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE,1,1),true);
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION,20*5,1),true); //thanks tokaua for the idea lol
        arrow.setItemMeta(potionMeta);
        skelly.getEquipment().setItemInOffHand(arrow);
        return skelly;
    }

    public static void setPigManFullDiamond(PigZombie pigman){
        pigman.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        pigman.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        pigman.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        pigman.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

}
