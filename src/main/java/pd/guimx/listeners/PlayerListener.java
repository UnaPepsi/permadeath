package pd.guimx.listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.DragonBattle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import pd.guimx.Permadeath;
import pd.guimx.config.MainConfigManager;
import pd.guimx.utils.Miscellaneous;
import pd.guimx.utils.Webhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static pd.guimx.listeners.EntityListener.random;


public class PlayerListener implements Listener{

    Permadeath permadeath;
    private boolean isDeathTrain = false;
    private int[] deathTrainSecondsRemaining = {-1};
    private final HashMap<Player, Boolean> playerElytraState = new HashMap<>();
    private HashMap<Player,Integer> playersWithRabies = new HashMap<>(); // LMFAO its a funny idea ok? 😭
    private HashMap<Player,Long> playersWithProtection = new HashMap<>();
    public PlayerListener(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    public boolean isDeathTrain(){
        return isDeathTrain;
    }

    public int getDeathTrainSecondsRemaining() {
        return deathTrainSecondsRemaining[0];
    }

    public void setDeathTrainSecondsRemaining(int deathTrainSecondsRemaining) {
        this.deathTrainSecondsRemaining[0] = deathTrainSecondsRemaining;
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e){
        if (permadeath.getDb().userBanned(e.getName())){
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("player_banned")));
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        permadeath.getDb().addUser(player.getName(),permadeath.getMainConfigManager().getStartingLives());
        e.setJoinMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("player_joined"),
                player.getName(),permadeath.getDb().getLifes(player.getName())));
        player.setResourcePack("https://fun.guimx.me/r/Permadeath%203.0.zip?compress=false",
              HexFormat.of().parseHex("da280498815c4de85c21942a31c746fb97a1f5e7"), Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("texture_pack")),false);
        player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("current_day"),
                permadeath.getMainConfigManager().getDay()));
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(
                player.getAttribute(Attribute.JUMP_STRENGTH).getDefaultValue()
        );
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
        player.getAttribute(Attribute.SCALE).setBaseValue(1);
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent e){ //yes i suck at naming i know
        Status status = e.getStatus();
        if (status == Status.DECLINED){
            e.getPlayer().kickPlayer(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("texture_pack_denied")));
        }
    }

    @EventHandler
    public void onDisconect(PlayerQuitEvent e){
        if (playersWithRabies.containsKey(e.getPlayer())) {
            e.getPlayer().clearActivePotionEffects();
            e.getPlayer().damage(5000, DamageSource.builder(DamageType.MAGIC).build());
        }

        //When players with grace period time disconnected they kept their infinite invis and glowing effect
        //I may use another fix but for now this should cover most cases
        if (!e.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)){ //dont fire if player has invis potion effect, bug still applies if player has grace period time and drinks an invis pot, but who's gonna do that?
            e.getPlayer().setInvisible(false);
        }
        if (!e.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)){ //same for glowing
            e.getPlayer().setGlowing(false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        playersWithRabies.remove(player);
        this.isDeathTrain = true;
        Location location = player.getLocation();

        String coordinates = String.format("x: %d, y: %d, z: %d",
                location.getBlockX(),location.getBlockY(),location.getBlockZ());
        e.setDeathMessage(e.getDeathMessage()+" | "+coordinates);

        Block block = location.getBlock();
        block.setType(Material.NETHER_BRICK_FENCE);

        location.setY(location.getBlockY()+1);
        block = location.getBlock();
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();
        skull.setOwnerProfile(player.getPlayerProfile());
        skull.update();

        location.setY(location.getBlockY()-2);
        location.getBlock().setType(Material.BEDROCK);

        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            player.spigot().respawn();

            Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+
                            permadeath.getMainConfigManager().getMessages().get("death_train_enabled"),
                        permadeath.getMainConfigManager().getDeathTrainSeconds()/3600));
            String title = Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_title"));
            String subtitle = String.format(permadeath.getMainConfigManager().getMessages().get("permadeath_subtitle"),player.getName());
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendTitle(title,subtitle,10,100,10);
                //p.playSound(p, Sound.ENTITY_SKELETON_HORSE_DEATH,5,1);
                p.playSound(p,"custom:permadeath",5,1);
            });

            String reason = Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("permadeath_kick_reason"));
            int newLifes = permadeath.getDb().getLifes(player.getName());
            permadeath.getDb().setLifes(player.getName(),newLifes-1);

            if(newLifes-1 <= 0) {
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getScheduler().runTaskLater(permadeath, () -> player.kickPlayer(reason), 150L);
            }else{
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("remaining_lifes"),
                        newLifes-1));
                int gracePeriod = permadeath.getMainConfigManager().getGracePeriod();
                long currentMili = System.currentTimeMillis();
                playersWithProtection.put(player,currentMili);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,20*gracePeriod, 1,true));
                player.setGlowing(true);
                player.setInvisible(true);
                ArrayList<LivingEntity> armorStands = new ArrayList<>();
                ItemStack jesusCross = new ItemStack(Material.CLOCK);
                ItemMeta jesusCrossMeta = jesusCross.getItemMeta();
                jesusCrossMeta.setItemModel(NamespacedKey.minecraft("jesus_totem"));
                jesusCross.setItemMeta(jesusCrossMeta);
                for (int i = 0; i < 20; i++){
                    armorStands.add(player.getWorld().spawn(player.getLocation().add(0,1,0), ArmorStand.class, spawnedArmorStand ->{
                        spawnedArmorStand.setNoPhysics(true);
                        spawnedArmorStand.setCollidable(false);
                        spawnedArmorStand.setInvulnerable(true);
                        spawnedArmorStand.setGravity(false);
                        spawnedArmorStand.setSilent(true);
                        spawnedArmorStand.getAttribute(Attribute.SCALE).setBaseValue(spawnedArmorStand.getAttribute(Attribute.SCALE).getValue() * 0.2);
                        spawnedArmorStand.setGravity(true);
                        spawnedArmorStand.setInvisible(true);
                        spawnedArmorStand.setNoPhysics(true);
                        spawnedArmorStand.getEquipment().setHelmet(jesusCross);
                    }));
                }
                Miscellaneous.orbitEntitiesAroundEntity(permadeath,player,armorStands,() -> player.isConnected() && playersWithProtection.containsKey(player));
                Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                    if (playersWithProtection.containsKey(player) && playersWithProtection.get(player) == currentMili) { //if a player dies multiple times before this fires, this should stop it from firing multiple times
                        playersWithProtection.remove(player);
                        player.setGlowing(false);
                        player.setInvisible(false);
                        player.sendMessage(Miscellaneous.translateColor(permadeath.prefix + permadeath.getMainConfigManager().getMessages().get("grace_period_removed")));
                    }
                },20L*gracePeriod);
            }
        }, 1L);

        World world = Objects.requireNonNull(Bukkit.getWorld("world"));
        if (deathTrainSecondsRemaining[0] > 0) {
            deathTrainSecondsRemaining[0] = permadeath.getMainConfigManager().getDeathTrainSeconds();
            Bukkit.getScheduler().runTaskLater(permadeath, () -> this.isDeathTrain = true,40L);
        }else {
            deathTrainSecondsRemaining[0] = permadeath.getMainConfigManager().getDeathTrainSeconds();
            Bukkit.getScheduler().runTaskTimer(permadeath, a -> {
                if (deathTrainSecondsRemaining[0] < 0) {
                    this.isDeathTrain = false;
                    world.setStorm(false);
                    world.setThundering(false);
                    a.cancel();
                    return;
                }
                if (world.isClearWeather()){
                    world.setStorm(true);
                    world.setThundering(true);
                    world.setWeatherDuration(deathTrainSecondsRemaining[0]);
                    world.setThunderDuration(deathTrainSecondsRemaining[0]);
                }
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Miscellaneous.translateColor(
                            permadeath.getMainConfigManager().getMessages().get("death_train"),
                            deathTrainSecondsRemaining[0] / 3600, (deathTrainSecondsRemaining[0] % 3600) / 60, deathTrainSecondsRemaining[0] % 60)
                    ));
                });
                deathTrainSecondsRemaining[0]--;
            }, 0, 20);
        }
        Bukkit.getScheduler().runTaskAsynchronously(permadeath, () -> {
            for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_died"),player.getName()),
                        e.getDeathMessage(), player.getName(),
                        permadeath.getMainConfigManager().getDay(), true);
            }
        });
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e){
        if (permadeath.getMainConfigManager().getDay() > 19){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("sleeping_disabled")));
            return;
        }
        if (isDeathTrain){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("sleeping_disabled_deathtrain")));
            return;
        }
        //Fix this for the love of god this is awful
        //Replying to the note above: im lazy as sh*t, too bad, it works for now
        if (permadeath.getMainConfigManager().getDay() < 10){
            return;
        }
        AtomicBoolean sleep = new AtomicBoolean(true);
        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            if (!e.getPlayer().isSleeping()){
                sleep.set(false);
            }
        },1L);
        if (!sleep.get()){
            return;
        }
        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
            int[] sleepingPlayers = {0};
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (player.isSleeping()){
                    sleepingPlayers[0]++;
                }
            });
            if (sleepingPlayers[0] >= permadeath.getMainConfigManager().getMinPlayersSleep()) {
                Objects.requireNonNull(Bukkit.getWorld("world")).setTime(1000);
            }
        },100L);
    }

    @EventHandler
    public void onInventoryInteract(SmithItemEvent e){
        SmithingInventory inventory = e.getInventory();
        if (inventory.contains(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) &&
            (inventory.contains(Material.DIAMOND_HELMET) ||
            inventory.contains(Material.DIAMOND_CHESTPLATE) ||
            inventory.contains(Material.DIAMOND_LEGGINGS) ||
            inventory.contains(Material.DIAMOND_BOOTS))){
            e.getWhoClicked().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("upgrade_netherite_failed")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e){
        if (e.isCancelled() || !(e.getEntity() instanceof Player player) || e.getHand() == null){
            return;
        }
        if (player.getInventory().getItem(e.getHand()).getItemMeta().getItemModel() != null &&
                player.getInventory().getItem(e.getHand()).getItemMeta().getItemModel().getKey().equals("jesus_totem") &&
                player.getInventory().getItem(e.getHand()).getType() == Material.CLOCK){
            player.playSound(player,"custom:bell",5,1);
            ItemStack helmet = player.getEquipment().getHelmet();
            if (helmet != null){ //only nullable to players :)
                Equippable.Builder equippable = helmet.getData(DataComponentTypes.EQUIPPABLE).toBuilder()
                        .cameraOverlay(Key.key(NamespacedKey.minecraft("jesus_overlay"),"overlay/jesus2"));
                helmet.setData(DataComponentTypes.EQUIPPABLE,equippable);
                Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                   equippable.cameraOverlay(null);
                    helmet.setData(DataComponentTypes.EQUIPPABLE,equippable);
                },30);
                AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.minecraft("adventure/totem_of_undying")));
                for (String criteria : progress.getRemainingCriteria()){
                    progress.awardCriteria(criteria);
                }
            }
            Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("jesus_totem_used"),player.getName()));
            for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                String jesusTotem = String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_jesus_totem"), e.getEntity().getName());
                Bukkit.getScheduler().runTaskAsynchronously(permadeath, () -> {
                    Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_totem"),player.getName()),
                        jesusTotem.replaceAll("&.",""), player.getName(),
                        permadeath.getMainConfigManager().getDay(), false);
                });
            }
            return;
        }
        int day = permadeath.getMainConfigManager().getDay();
        if (day > 29){
            int randomInt = random.nextInt(0,100);
            int probability = day > 33 ? 3 : 1;
            if (randomInt < probability){
                e.setCancelled(true);
                Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+
                                permadeath.getMainConfigManager().getMessages().get("totem_failed"),
                        player.getName()));
            }else{
                String totemWorked = String.format(permadeath.getMainConfigManager().getMessages().get("totem_worked"),
                        e.getEntity().getName(),randomInt+1,probability);
                Bukkit.broadcastMessage(Miscellaneous.translateColor(permadeath.prefix+totemWorked));
                Bukkit.getScheduler().runTaskAsynchronously(permadeath,() -> {
                    for (String webhook : permadeath.getMainConfigManager().getDiscordWebhooks()) {
                        Webhook.sendMessage(webhook, String.format(permadeath.getMainConfigManager().getMessages().get("discord_webhook_totem"),player.getName()),
                                totemWorked.replaceAll("&.",""), player.getName(),
                                permadeath.getMainConfigManager().getDay(), false);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Location loc = e.getTo().clone();
        loc.setY(e.getTo().getY()-0.1);
        Player player = e.getPlayer();
        if (loc.getBlock().getType() == Material.BEDROCK && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)){

            //For some reason, even with a higher y vector, the velocity is the same, just that for some reason,
            //when I take damage, if I set the Vector to have a higher y the velocity applies multiple times.
            //My theory is that because I didn't reach the Vector I set, I get pushed until the number is reached
            player.setVelocity(new Vector(0,10,0));
        }else if ((loc.add(0,2,0).getBlock().getType() == Material.WATER ||
                    loc.add(0,-1,0).getBlock().getType() == Material.WATER && player.isSwimming()) &&
                    //even without the potion checks the player wouldn't drown but their bubbles would pop and that looks kinda weird
                    !player.hasPotionEffect(PotionEffectType.WATER_BREATHING) &&
                    !player.hasPotionEffect(PotionEffectType.CONDUIT_POWER) &&
                    permadeath.getMainConfigManager().getDay() > 19){
            player.setRemainingAir(player.getRemainingAir() >= 0 ? (int) (player.getRemainingAir() * 0.75) : player.getRemainingAir());
        }
        if (permadeath.getMainConfigManager().getDay() > 29){
            EntityEquipment equipment = player.getEquipment();
            if (equipment.getHelmet() != null){
                return;
            }
            RayTraceResult result = player.getWorld().rayTraceBlocks(player.getLocation(),new Vector(0,385-player.getY(),0),385);
            if ((result == null || result.getHitBlock() == null) && !player.getWorld().isClearWeather() && isDeathTrain && random.nextFloat() < 0.05){
                player.damage(0.05);
            }
        }
    }

    @EventHandler
    public void onRightCick(BlockPlaceEvent e){
        BlockData blockData = e.getBlockPlaced().getBlockData();
        if ((blockData instanceof Bed || blockData instanceof RespawnAnchor)
            && "world_the_end".equalsIgnoreCase(e.getPlayer().getWorld().getName())){
            e.getPlayer().sendMessage(Miscellaneous.translateColor(permadeath.prefix+
                    permadeath.getMainConfigManager().getMessages().get("bed_anchor_disabled_end")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e){
        World world = e.getTo().getWorld();
        if ("world_the_end".equals(world.getName())){
            DragonBattle dragonBattle = world.getEnderDragonBattle();
            if (dragonBattle == null){
                return;
            }
            EnderDragon dragon = dragonBattle.getEnderDragon();
            if (dragon == null || dragon.getBossBar() == null){
                return;
            }
            if (!"&6&lPERMADEATH DEMON".equalsIgnoreCase(dragon.getBossBar().getTitle())){
                if (dragon.getAttribute(Attribute.MAX_HEALTH).getValue()/dragon.getHealth() < 2){
                    dragon.getBossBar().setColor(BarColor.BLUE);
                }
                dragon.customName(Component.text(Miscellaneous.translateColor("&6&lPERMADEATH DEMON")));
            }
       }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e){
        Player player = e.getPlayer();
        e.setCancelled(true);
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        Team team = player.getScoreboard().getEntityTeam(player);
        String teamPrefix = team != null ? team.getDisplayName() : "";
        Bukkit.broadcastMessage(Miscellaneous.translateColor("%s&r%s: %s",teamPrefix,player.getName(),serializer.serialize(e.originalMessage())));
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e){
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (item.getItemMeta().getItemModel() != null && item.getItemMeta().getItemModel().getKey().equals("heart") &&
            item.getType() == Material.CLOCK &&
            item.getItemMeta().getFood().getNutrition() == 0
        ){
            if (maxHealth < 24) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth + 2);
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix + permadeath.getMainConfigManager().getMessages().get("extra_heart_gained")));
            }else{
                player.sendMessage(Miscellaneous.translateColor(permadeath.prefix + permadeath.getMainConfigManager().getMessages().get("extra_heart_limit_reached")));
                e.setCancelled(true);
                Miscellaneous.endermanPlayer(player, permadeath.getProtocolManager()); //xd
                Miscellaneous.guardianJumpscare(permadeath,player); //xd
            }
        }
    }

    @EventHandler
    public void onInventoryUpdate(PlayerInventorySlotChangeEvent e){
        Player player = e.getPlayer();
        ItemStack chestplate = player.getEquipment().getChestplate();
        boolean isWearingFireResistantElytra = chestplate != null
                && chestplate.getType() == Material.ELYTRA
                && chestplate.getItemMeta().isFireResistant();
        Boolean previousState = playerElytraState.get(player);
        //chestplate.getItemMeta().getAttributeModifiers().containsKey("fire_resistant")
        if (isWearingFireResistantElytra && (previousState == null || !previousState)){
            playerElytraState.put(player,true);
            player.getAttribute(Attribute.GRAVITY).setBaseValue(0.04);
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE).setBaseValue(Integer.MAX_VALUE);
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE).setBaseValue(Integer.MAX_VALUE);
            ArrayList<LivingEntity> armorStands = new ArrayList<>();
            for (int i = 0; i < 20; i++){
                armorStands.add(player.getWorld().spawn(player.getLocation().add(0,1,0), ArmorStand.class, spawnedArmorStand ->{
                    spawnedArmorStand.setNoPhysics(true);
                    spawnedArmorStand.setCollidable(false);
                    spawnedArmorStand.setInvulnerable(true);
                    spawnedArmorStand.setGravity(false);
                    spawnedArmorStand.setSilent(true);
                    spawnedArmorStand.getAttribute(Attribute.SCALE).setBaseValue(spawnedArmorStand.getAttribute(Attribute.SCALE).getValue() * 0.2);
                    spawnedArmorStand.setGravity(true);
                    spawnedArmorStand.setInvisible(true);
                    spawnedArmorStand.setNoPhysics(true);
                    spawnedArmorStand.setItemInHand(new ItemStack(Material.GOLD_BLOCK));
                }));
            }
            Miscellaneous.orbitEntitiesAroundEntity(permadeath,player,armorStands,() -> {
                ItemStack chestplateCheck = player.getEquipment().getChestplate();
                return chestplateCheck != null && chestplateCheck.getItemMeta().isFireResistant() && chestplateCheck.getType() == Material.ELYTRA &&
                        player.isConnected();
            });
        }else if (!isWearingFireResistantElytra && (previousState == null || previousState)){
            playerElytraState.remove(player);
            player.getAttribute(Attribute.GRAVITY).setBaseValue(0.08);
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE).setBaseValue(3.0);
        }
    }

    @EventHandler
    public void onPreparedCraft(PrepareItemCraftEvent e){
        if (e.getInventory().getMaxStackSize() < 9) {return;}
        ItemStack[] items = e.getInventory().getMatrix();
        if (items.length < 9) {return;}
        for (int i = 0; i < 3; i++){
            if (items[i] == null || items[i].getType() != Material.FEATHER || items[8-i] == null || items[8-i].getType() != Material.FEATHER){
                return;
            }
        }
        if (items[3] == null || items[3].getType() != Material.FEATHER || items[5] == null || items[5].getType() != Material.FEATHER || items[4] == null || items[4].getType() != Material.ELYTRA){
            return;
        }
        ItemMeta resultMeta = items[4].getItemMeta();
        resultMeta.setFireResistant(true);
        resultMeta.setDisplayName(Miscellaneous.translateColor("&6Super Elytra"));
        ItemStack result = items[4].clone();
        result.setItemMeta(resultMeta);
        e.getInventory().setResult(result);
    }

    @EventHandler
    public void onRightClick(PlayerArmorStandManipulateEvent e){
        ArmorStand stand = e.getRightClicked();
        if (stand.isInvisible() && stand.hasNoPhysics()){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e){
        if (e.getDamageSource().getCausingEntity() instanceof Player player && playersWithProtection.containsKey(player)){
            playersWithProtection.remove(player);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.setGlowing(false);
            player.setInvisible(false);
            player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("grace_period_removed")));
        }
        if (permadeath.getMainConfigManager().getDay() > 20 && e.getEntity() instanceof Player player) {
            if (player.getWorld().isUltraWarm()){
                player.setFireTicks(20*10);
            }
            Entity damager = e.getDamageSource().getCausingEntity();
            if (!(damager instanceof Monster || damager instanceof Phantom || damager instanceof Slime)) {
                player.sendActionBar(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("jump_disabled")));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
                        Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(
                                    player.getAttribute(Attribute.JUMP_STRENGTH).getDefaultValue()
                            );
                            cancel();
                        }, 2 * 20);
                    }
                }.runTaskTimer(permadeath, 0, 1);
            }
            if (damager instanceof Goat goat) {
                player.setVelocity(goat.getLocation().getDirection().setY(0).normalize().multiply(20));
            }else if (damager instanceof Slime slime && !slime.getPersistentDataContainer().has(NamespacedKey.minecraft("mimic_slime"),PersistentDataType.STRING)){
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        player.getAttribute(Attribute.SCALE).setBaseValue(0.2);
                        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.05);
                        Bukkit.getScheduler().runTaskLater(permadeath,() -> {
                            player.getAttribute(Attribute.SCALE).setBaseValue(1);
                            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                            cancel();
                        },5*20);
                    }
                }.runTaskTimer(permadeath,0,1);
            }else if (damager instanceof Wolf){
                if (playersWithRabies.get(player) == null){
                    playersWithRabies.put(player,permadeath.getMainConfigManager().getRabiesSeconds());
                    player.sendMessage(Miscellaneous.translateColor(permadeath.prefix+permadeath.getMainConfigManager().getMessages().get("rabies_infected"),
                            permadeath.getMainConfigManager().getRabiesSeconds()/60));
                    Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                        if (playersWithRabies.get(player) != null){
                            player.clearActivePotionEffects(); //just in case they have resistance 5 or sum
                            player.damage(Integer.MAX_VALUE, DamageSource.builder(DamageType.MAGIC).build());
                            playersWithRabies.remove(player);
                        }
                    }, 20L *playersWithRabies.get(player));
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            if (playersWithRabies.get(player) == null){
                                player.sendActionBar(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("rabies_cured")));
                                cancel();
                                return;
                            }
                            player.sendActionBar(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("rabies_timer"),
                                    playersWithRabies.get(player)/3600,(playersWithRabies.get(player)%3600)/60,playersWithRabies.get(player)%60));
                            playersWithRabies.put(player, playersWithRabies.getOrDefault(player,-1)-1);
                        }
                    }.runTaskTimer(permadeath,0,20);
                }
            }
        }
    }
    @EventHandler
    public void onAnyDamage(EntityDamageEvent e){
        if (!(e.getEntity() instanceof Player player)){
            return;
        }
        if (playersWithProtection.containsKey(player)){
            e.setCancelled(true);
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.DROWNING && permadeath.getMainConfigManager().getDay() > 19){
            e.setDamage(e.getDamage()*10);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e){
        if (e.getItem().getType() == Material.MILK_BUCKET && playersWithRabies.get(e.getPlayer()) != null){
            playersWithRabies.remove(e.getPlayer());
        }
    }

    //Doesn't handle projectiles :(
    //@EventHandler
    //public void onPlayerAttack(PrePlayerAttackEntityEvent e){
    //    Player player = e.getPlayer();
    //    if (playersWithProtection.containsKey(player)) {
    //        playersWithProtection.remove(player);
    //        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    //        player.sendMessage(Miscellaneous.translateColor(permadeath.getMainConfigManager().getMessages().get("grace_period_removed")));
    //    }
    //}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        //Bukkit.getLogger().info("asd"+ e.hasBlock()+" "+e.getClickedBlock()+" "+(e.getClickedBlock()instanceof Chest));
        if (e.hasBlock() && e.getClickedBlock().getState() instanceof Chest chest && chest.hasLootTable() && !e.isCancelled() && random.nextFloat() <= 0.2){
            e.getPlayer().getWorld().playSound(e.getClickedBlock().getLocation(),"minecraft:item.wolf_armor.crack",2f,0.6f);
            e.setCancelled(true);
            //we have to wait since loot isnt generated yet
            Inventory[] chestInventory = new Inventory[1];
            Bukkit.getScheduler().runTaskLater(permadeath, () -> {
                chestInventory[0] = chest.getBlockInventory();
                e.getClickedBlock().setType(Material.AIR);
                ItemStack mimicChestItem = new ItemStack(Material.CHEST);
                mimicChestItem.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("pd_mimic_8").build());
                ItemMeta mimicChestItemMeta = mimicChestItem.getItemMeta();
                mimicChestItemMeta.setItemModel(NamespacedKey.minecraft("mimic"));
                //mimicChestItemMeta.getCustomModelDataComponent().setStrings(List.of("pd_mimic_5"));
                mimicChestItem.setItemMeta(mimicChestItemMeta);
                ArmorStand armorStand = e.getClickedBlock().getWorld().spawn(e.getClickedBlock().getLocation(), ArmorStand.class);
                armorStand.getEquipment().setHelmet(mimicChestItem);

                armorStand.setNoPhysics(true); //through blocks
                armorStand.setMarker(true);
                armorStand.setInvisible(true);
                armorStand.getAttribute(Attribute.SCALE).setBaseValue(1.5);

                //to differentiate between other slimes that are affected by this plugin
                Slime slime = e.getClickedBlock().getWorld().spawn(e.getClickedBlock().getLocation(),Slime.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
                slime.getPersistentDataContainer().set(NamespacedKey.minecraft("mimic_slime"), PersistentDataType.STRING,"true");
                slime.setInvisible(true);
                slime.setSilent(true);
                slime.setSize(4); //1.5 hearts with full iron
                slime.getAttribute(Attribute.SCALE).setBaseValue(0.35);
                slime.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                MainConfigManager mainConfigManager = permadeath.getMainConfigManager();
                slime.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mainConfigManager.getDay() > 9 ? mainConfigManager.getMimicBaseSpeedDay10() : mainConfigManager.getMimicBaseSpeed());
                slime.setHealth(20);
                slime.setCustomName("Mimic");
                slime.setCustomNameVisible(false);

                final int[] mimicState = {0};
                final boolean[] mimicOpeningMouth = {true};
                Bukkit.getScheduler().runTaskTimer(permadeath, runnable -> {
                    if (slime.isDead()){
                        Bukkit.getLogger().info("mimic ded");
                        slime.getWorld().playSound(slime.getLocation(),"minecraft:entity.creaking.death",2f,2f);
                        chestInventory[0].forEach(item -> {
                            if (item == null){return;}
                            //Bukkit.getLogger().info(item+"");
                            armorStand.getWorld().dropItemNaturally(armorStand.getLocation(),item);
                        });
                        runnable.cancel();
                        armorStand.remove();
                        return;
                    }
                    armorStand.teleport(slime.getLocation().add(0,-1.95,0).addRotation(180,0)); //the chest is backwards so we rotate 180
                    mimicOpeningMouth[0] = mimicState[0] < 10 && mimicOpeningMouth[0] || mimicState[0] <= 0;
                    mimicChestItem.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("pd_mimic_"+mimicState[0]).build());
                    armorStand.getEquipment().setHelmet(mimicChestItem);
                    if (mimicOpeningMouth[0]) {
                        mimicState[0]++;
                    }else{
                        mimicState[0]--;
                    }
                },0,1);
            },1);
        }
    }
}
