package pd.guimx.utils;

//import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Webhook {
    public static void sendMessage(String webhookUrl, String message, String description, String playerName, int day, boolean tagEveryone){
        try {
            Gson gson = new Gson();
            HashMap<String, Object> body = new HashMap<>();
            List<Object> embeds = new ArrayList<>();
            HashMap<String,Object> embed = new HashMap<>();
            HashMap<String,String> image = new HashMap<>();
            HashMap<String,String> footer = new HashMap<>();
            image.put("url","https://mc-heads.net/avatar/"+playerName);
            footer.put("text","Day: "+day);

            embed.put("title",message);
            embed.put("description",description);
            embed.put("thumbnail",image);
            embed.put("color",16711680);
            embed.put("footer",footer);
            if (tagEveryone){
                body.put("content","@everyone");
            }
            embeds.add(embed);
            body.put("embeds",embeds);
            body.put("username","PERMADEATH");
            body.put("avatar_url","https://fun.guimx.me/r/j4bIdJE2VO.png?compress=false");
            String json = gson.toJson(body);

            URL url = new URL(webhookUrl+"?wait=true");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);

            conn.getResponseCode(); //why the hell do I have to use this for the request to send

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
