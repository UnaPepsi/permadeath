package pd.guimx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pd.guimx.Permadeath;
import pd.guimx.utils.MessageUtils;

import java.time.Instant;

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
        } else if ("afkban".equalsIgnoreCase(args[0])) {
            subCommandHandler(sender,"afkban",args);
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
                if (player == null){
                    sender.sendMessage(MessageUtils.translateColor(Permadeath.prefix+"&cplayer doesn't exist"));
                    return;
                }
                String reason = MessageUtils.translateColor("&c&lPERMADEATH!&r\nYou have died for being AFK.");
                Instant time = Instant.ofEpochSecond(Instant.now().getEpochSecond()+(86400*365*50));
                player.ban(reason,time, sender.getName(),true);
                Bukkit.getConsoleSender().sendMessage(String.valueOf(time));
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
