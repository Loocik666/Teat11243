package com.example.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileExist {
   private static final String DIRECTORY_PATH = "plugins/seenPlugin/data/players";

   public JsonObject checkPlayerFile(String player) {
      String fileName = player + ".json";
      Path filePath = Paths.get("plugins/seenPlugin/data/players", fileName);
      if (!Files.exists(filePath, new LinkOption[0])) {
         JsonObject defaultJson = new JsonObject();
         defaultJson.add("eventList", new JsonArray());

         try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, defaultJson.toString(), new OpenOption[0]);
            return defaultJson;
         } catch (IOException var6) {
            var6.printStackTrace();
            return new JsonObject();
         }
      } else {
         try {
            String content = Files.readString(filePath);
            return JsonParser.parseString(content).getAsJsonObject();
         } catch (IOException var7) {
            var7.printStackTrace();
            return new JsonObject();
         }
      }
   }

   public void savePlayerFile(String player, JsonObject newData) {
      String fileName = player + ".json";
      Path filePath = Paths.get("plugins/seenPlugin/data/players", fileName);
      Gson gson = (new GsonBuilder()).setPrettyPrinting().serializeNulls().create();

      try {
         String jsonString = gson.toJson(newData);
         Files.writeString(filePath, jsonString, new OpenOption[0]);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

   }
}
