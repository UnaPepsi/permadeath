package pd.guimx.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String translateColor(String message){
        return ChatColor.translateAlternateColorCodes('&',message);
    }
}
