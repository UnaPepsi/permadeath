package pd.guimx.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;


public class MainCommand implements CommandExecutor {

    public Permadeath permadeath;
    public MainCommand(Permadeath permadeath){
        this.permadeath = permadeath;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sender.sendMessage(MessageUtils.translateColor(helpCommand()));
            return true;
        } else if ("version".equalsIgnoreCase(args[0])) {
            sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "currently in version " + permadeath.version));
        } else if ("setday".equalsIgnoreCase(args[0]) || "tpworld".equalsIgnoreCase(args[0]) ||
                "setlifes".equalsIgnoreCase(args[0])) {
            subCommandHandler(sender, args[0], args);
        } else if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("pd.reload")) {
                sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&cYou need &7pd.reload &cpermissions to run this command"));
                return true;
            }
            permadeath.getMainConfigManager().reloadConfig();
            sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&aReloaded config!"));
        }else if ("hour".equalsIgnoreCase(args[0])){
            sender.sendMessage("hour "+permadeath.getMainConfigManager().getHour());
        }else{
            sender.sendMessage(MessageUtils.translateColor(helpCommand()));
        }
        return true;
    }

    private void subCommandHandler(CommandSender sender, String command, String[] args){
        if ("setday".equalsIgnoreCase(command)) {
            if (!sender.hasPermission("pd.set")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.set &cpermissions to run this command"));
                return;
            }
            if (args.length < 2 || args.length > 3){
                sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&cusage: /permadeath setday <day>"));
            }else {
                int day;
                try {
                    day = Integer.parseInt(args[1]);
                    if (day < 0) {
                        throw new NumberFormatException();
                    }
                    permadeath.getMainConfigManager().setHour(day*24);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&cday must be a valid Integer"));
                    return;
                }
                sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&aNow on day: &c" + permadeath.getMainConfigManager().getDay()));
                sender.sendMessage(MessageUtils.translateColor(permadeath.prefix+"Please remember that if you wish to change how mobs spawn you must change so " +
                        "manually and restart the server"));
            }
        }else if ("tpworld".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.tpworld")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.tpworld &cpermissions to run this command"));
                return;
            }
            if (sender instanceof Player player){
                Location location = new Location(Bukkit.getWorld(args[1]),0,100,0);
                player.teleport(location);
            }
        }else if ("setlifes".equalsIgnoreCase(command)){
            if (!sender.hasPermission("pd.setlifes")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.tpworld &cpermissions to run this command"));
                return;
            }
            if (args.length < 3 || args.length > 4){
                sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&cusage: /permadeath setlifes <player> <lifes>"));
            }else{
                int lifes;
                try{
                    lifes = Integer.parseInt(args[2]);
                } catch (NumberFormatException e){
                    sender.sendMessage(MessageUtils.translateColor(permadeath.prefix + "&clifes must be a valid Integer"));
                    return;
                }
                if (!permadeath.getDb().setLifes(args[1],lifes)){
                    sender.sendMessage(MessageUtils.translateColor(permadeath.prefix+"&cuser doesn't exist"));
                }else{
                    sender.sendMessage(MessageUtils.translateColor(permadeath.prefix+"&aset lifes of %s to %d",args[1],lifes));
                }
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
