package pd.guimx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pd.guimx.Permadeath;
import pd.guimx.utils.CustomSkeletons;
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
            sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"currently in version "+permadeath.version));
        } else if ("afkban".equalsIgnoreCase(args[0]) || "unban".equalsIgnoreCase(args[0]) ||
                    "setday".equalsIgnoreCase(args[0])) {
                        subCommandHandler(sender,args[0],args);
        } else if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("pd.reload")){
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cYou need &7pd.reload &cpermissions to run this command"));
                return true;
            }
            permadeath.getMainConfigManager().reloadConfig();
            sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&aReloaded config!"));
        }else{
            sender.sendMessage(MessageUtils.translateColor(helpCommand()));
        }
        return true;
    }

    private void subCommandHandler(CommandSender sender, String command, String[] args){
        if ("afkban".equalsIgnoreCase(command)) {
            if (!sender.hasPermission("pd.ban")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.ban &cpermissions to run this command"));
                return;
            }
            if (args.length < 2 || args.length > 3){
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix + "&cusage: /permadeath afkban <player>"));
            }else{
                Player player = Bukkit.getPlayer(args[1]);
                if(!permadeath.getDb().banOrUnbanPlayer(args[1],true)){
                    sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cplayer is already banned or doesn't exist"));
                    return;
                }
                if (player != null) {
                    String reason = MessageUtils.translateColor("&c&lPERMADEATH!&r\nYou have died for being AFK.");
                    player.kickPlayer(reason);
                }
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cbanned "+args[1]));
            }
        }else if ("unban".equalsIgnoreCase(command)) {
            if (!sender.hasPermission("pd.unban")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.unban &cpermissions to run this command"));
                return;
            }
            if (args.length < 2 || args.length > 3){
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix + "&cusage: /permadeath unban <player>"));
            }else{
                if(!permadeath.getDb().banOrUnbanPlayer(args[1],false)){
                    sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cplayer is already unbanned or doesn't exist"));
                    return;
                }
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&aunbanned "+args[1]));
            }
        } else if ("setday".equalsIgnoreCase(command)) {
            if (!sender.hasPermission("pd.set")){
                sender.sendMessage(MessageUtils.translateColor("&cYou need &7pd.set &cpermissions to run this command"));
                return;
            }
            if (args.length < 2 || args.length > 3){
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix + "&cusage: /permadeath setday <day>"));
            }else {
                int day;
                try {
                    day = Integer.parseInt(args[1]);
                    if (day < 0) {
                        throw new NumberFormatException();
                    }
                    permadeath.getMainConfigManager().setDay(day);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix + "&cday must be a valid Integer"));
                    return;
                }
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix + "&aNow on day: &c" + permadeath.getMainConfigManager().getDay()));
                sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"Please remember that if you wish to change how mobs spawn you must change so " +
                        "manually and restart the server"));
            }
        }
    }

    private String helpCommand(){
        return "&8&m----------------------------------------\n" +
                "&4&lPermadeath &8&l| &7Help\n" +
                "&8&m----------------------------------------\n" +
                "&7/permadeath help &8- &7Displays this help message\n" +
                "&7/permadeath version &8- &7Displays the plugin version\n" +
                "&7/permadeath afkban &8- &7Bans someone for being AFK\n" +
                "&8&m----------------------------------------";
    }
}
