package pd.guimx.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pd.guimx.Permadeath;
import pd.guimx.utils.Miscellaneous;

import java.util.ArrayList;


public class MainCommand implements CommandExecutor {

    public Permadeath permadeath;
    public MainCommand(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sender.sendMessage(Miscellaneous.translateColor(helpCommand()));
            return true;
        } else if ("version".equalsIgnoreCase(args[0])) {
            sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "currently in version " + permadeath.version));
        } else if ("setday".equalsIgnoreCase(args[0]) || "tpworld".equalsIgnoreCase(args[0]) ||
                "setlifes".equalsIgnoreCase(args[0]) || "test".equalsIgnoreCase(args[0]) || "deathtrain".equalsIgnoreCase(args[0])){
            subCommandHandler(sender, args[0], args);
        } else if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("pd.reload")) {
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&cYou need &7pd.reload &cpermissions to run this command"));
                return true;
            }
            permadeath.getMainConfigManager().reloadConfig();
            sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&aReloaded config!"));
        }else if ("hour".equalsIgnoreCase(args[0])) {
            sender.sendMessage("hour " + permadeath.getMainConfigManager().getHour());
        }else{
            sender.sendMessage(Miscellaneous.translateColor(helpCommand()));
        }
        return true;
    }

    private void subCommandHandler(CommandSender sender, String command, String[] args){
        if ("setday".equalsIgnoreCase(command)) {
            if (!sender.hasPermission("pd.set")){
                sender.sendMessage(Miscellaneous.translateColor("&cYou need &7pd.set &cpermissions to run this command"));
                return;
            }
            if (args.length < 2 || args.length > 3){
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&cusage: /permadeath setday <day>"));
            }else {
                int day;
                try {
                    day = Integer.parseInt(args[1]);
                    if (day < 0) {
                        throw new NumberFormatException();
                    }
                    permadeath.getMainConfigManager().setHour(day*24);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&cday must be a valid Integer"));
                    return;
                }
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&aNow on day: &c" + permadeath.getMainConfigManager().getDay()));
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix+"Please remember that if you wish to change how mobs spawn you must change so " +
                        "manually and restart the server"));
            }
        }else if ("tpworld".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.tpworld")){
                sender.sendMessage(Miscellaneous.translateColor("&cYou need &7pd.tpworld &cpermissions to run this command"));
                return;
            }
            if (sender instanceof Player player){
                Location location = new Location(Bukkit.getWorld(args[1]),0,100,0);
                player.teleport(location);
            }
        }else if ("setlifes".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.setlifes")){
                sender.sendMessage(Miscellaneous.translateColor("&cYou need &7pd.tpworld &cpermissions to run this command"));
                return;
            }
            if (args.length < 3 || args.length > 4){
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&cusage: /permadeath setlifes <player> <lifes>"));
            }else{
                int lifes;
                try{
                    lifes = Integer.parseInt(args[2]);
                } catch (NumberFormatException e){
                    sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix + "&clifes must be a valid Integer"));
                    return;
                }
                if (!permadeath.getDb().setLifes(args[1],lifes)){
                    sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix+"&cuser doesn't exist"));
                }else{
                    sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix+"&aset lifes of %s to %d",args[1],lifes));
                }
            }
        }else if ("test".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.test")){
                sender.sendMessage(Miscellaneous.translateColor("&cYou need &7pd.test &cpermissions to run this command"));
                return;
            }
            if (!(sender instanceof Player player)){
                sender.sendMessage(Miscellaneous.translateColor("&cPlayer only command"));
                return;
            }
            PacketContainer packetContainer;
            switch (args[1]){
                case "death":
                    packetContainer = permadeath.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_COMBAT_KILL);
                    packetContainer.getIntegers().write(0,player.getEntityId());
                    packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText("sucks"));
                    permadeath.getProtocolManager().sendServerPacket(player,packetContainer);
                    break;
                case "enderman":
                    Enderman enderman = (Enderman) player.getWorld().spawn(player.getLocation(), EntityType.ENDERMAN.getEntityClass());
                    packetContainer = permadeath.getProtocolManager().createPacket(PacketType.Play.Server.CAMERA);
                    packetContainer.getIntegers().write(0,enderman.getEntityId());
                    permadeath.getProtocolManager().sendServerPacket(player,packetContainer);
                    enderman.remove();
                    Location location = player.getEyeLocation();
                    if ("kill".equalsIgnoreCase(args[2])) {
                        player.setGameMode(GameMode.SURVIVAL);
                        //ItemStack[] itemStacks = player.getInventory().getContents().clone(); It's better to just have keepInventory in the PlayerDeathEvent
                        //player.getInventory().clear();
                        //player.setHealth(0);
                        player.damage(Integer.MAX_VALUE, player); //a player can't just hit themselves so with this I'm able to not trigger what would normally happen in the PlayerDeathEvent
                        player.spigot().respawn();
                        //player.getInventory().setContents(itemStacks);
                        player.teleport(location);
                        if (player.getPreviousGameMode() == null) {
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                        player.setGameMode(player.getPreviousGameMode());
                    }else if ("world".equalsIgnoreCase(args[2])){
                        player.teleport(new Location(Bukkit.getWorld("pd_void"),0,100,0));
                        player.teleport(location);
                    }
                    break;
                case "zawarudo": //xd
                    if (args.length < 3){
                        player.sendMessage("ticks needed");
                        return;
                    }
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"tick freeze");
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        double speed = p.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
                        double jumpSrength = p.getAttribute(Attribute.JUMP_STRENGTH).getValue();
                        double entityReach = p.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue();
                        double blockReach = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
                        p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
                        p.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
                        p.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(0);
                        p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(0);
                        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                            p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
                            p.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpSrength);
                            p.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(entityReach);
                            p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(blockReach);
                        },Long.parseLong(args[2])*20);
                    });
                    Bukkit.getScheduler().runTaskLater(permadeath, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"tick unfreeze"),
                            Long.parseLong(args[2])*20);
                    break;
                case "rotate":
                    //Miscellaneous.rotateBlocksInEntity(permadeath, player, BlockType.DIAMOND_BLOCK,Integer.parseInt(args[2]),Long.parseLong(args[4]),Double.parseDouble(args[3]));
                    ArrayList<Entity> entities = new ArrayList<>();
                    for (int i = 0; i < 50; i++){
                        ArmorStand stand = (ArmorStand) player.getWorld().spawn(player.getLocation(),EntityType.ARMOR_STAND.getEntityClass(), a -> {
                            ArmorStand armorStand = (ArmorStand) a;
                            //armorStand.setMarker(true); //this makes armor stands not be able to have velocity
                            armorStand.setNoPhysics(true);
                            armorStand.setInvisible(true);
                            armorStand.setItemInHand(new ItemStack(Material.DIAMOND_BLOCK));
                        });
                        entities.add(stand);
                    }
                    Miscellaneous.rotateEntitiesInLocation(permadeath,entities,player.getLocation(),Double.parseDouble(args[2]),10*20);
                    break;
                case "push":
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20 * 5, 1));
                    Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                        player.getWorld().spawn(player.getLocation(), EntityType.AREA_EFFECT_CLOUD.getEntityClass(), spawnedCloud -> {
                            AreaEffectCloud areaEffectCloud = (AreaEffectCloud) spawnedCloud;
                            areaEffectCloud.setRadius(3);
                            areaEffectCloud.setParticle(Particle.RAID_OMEN);
                            areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 10, 2), true);
                            areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 10, 5), true);
                            areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 255),true);
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    Location areaLocation = spawnedCloud.getLocation();
                                    areaLocation.getNearbyPlayers(10).forEach(ppp -> {
                                        ppp.setVelocity(areaLocation.toVector().subtract(ppp.getLocation().toVector()).normalize().multiply(0.1));
                                    });
                                    Bukkit.getScheduler().runTaskLater(permadeath, this::cancel, 20 * 7);
                                }
                            }.runTaskTimer(permadeath,40,2); //0.5 & 10, 0.2 & 5, 0.1 & 2
                        });
                    }, 20 * 5);
                    break;
                case "pumpkin":
                    Inventory inv = Bukkit.createInventory(null,9,"ola");
                    player.openInventory(inv);
                    break;
            }
        }else if ("deathtrain".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.deathtrain")){
                sender.sendMessage(Miscellaneous.translateColor("&cYou need &7pd.deathtrain &cpermissions to run this command"));
                return;
            }
            if (args.length != 2){
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix+"&cusage: /permadeath deathtrain <seconds>"));
            }else if (permadeath.getPlayerListener().isDeathTrain()){
                permadeath.getPlayerListener().setDeathTrainSecondsRemaining(Integer.parseInt(args[1]));
            }else{
                sender.sendMessage(Miscellaneous.translateColor(permadeath.prefix+"&cDeathTrain is not curently active"));
            }
        }
    }

    private String helpCommand(){
        return "&8&m----------------------------------------\n" +
                "&4&lPermadeath &8&l| &7Help\n" +
                "&8&m----------------------------------------\n" +
                "&7/permadeath help &8- &7Displays this help message\n" +
                "&7/permadeath version &8- &7Displays the plugin version\n" +
                "&7/permadeath setlifes &8- &7Manually sets the lifes of a player\n" +
                "&7/permadeath setday &8- &7Manually sets the current day\n" +
                "&7/permadeath reload &8- &7Reloads the plugin's config.yml\n" +
                "&8&m----------------------------------------";
    }
}
