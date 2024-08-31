package pd.guimx.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String translateColor(String message, Object... args){
        return ChatColor.translateAlternateColorCodes('&',String.format(message,args));
    }
}
