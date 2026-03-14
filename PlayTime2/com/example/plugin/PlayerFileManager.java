package com.example.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerFileManager {
   private static final String PLAYER_DATA_DIR = "plugins/seenPlugin/data/players";

   public JsonObject readPlayerFile(String player) {
      Path filePath = this.getPlayerFilePath(player);
      if (!Files.exists(filePath)) {
         return null;
      }

      try {
         String content = Files.readString(filePath);
         JsonObject parsed = JsonParser.parseString(content).getAsJsonObject();
         return this.ensureEventList(parsed);
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public JsonObject getOrCreatePlayerFile(String player) {
      JsonObject existing = this.readPlayerFile(player);
      if (existing != null) {
         return existing;
      }

      JsonObject defaultJson = this.createDefaultPlayerData();
      this.savePlayerFile(player, defaultJson);
      return defaultJson;
   }

   public void savePlayerFile(String player, JsonObject newData) {
      Path filePath = this.getPlayerFilePath(player);
      Gson gson = (new GsonBuilder()).setPrettyPrinting().serializeNulls().create();

      try {
         Files.createDirectories(filePath.getParent());
         String jsonString = gson.toJson(this.ensureEventList(newData));
         Files.writeString(filePath, jsonString, new OpenOption[0]);
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   private Path getPlayerFilePath(String player) {
      String fileName = player + ".json";
      return Paths.get(PLAYER_DATA_DIR, fileName);
   }

   private JsonObject createDefaultPlayerData() {
      JsonObject defaultJson = new JsonObject();
      defaultJson.add("eventList", new JsonArray());
      return defaultJson;
   }

   private JsonObject ensureEventList(JsonObject jsonObject) {
      if (!jsonObject.has("eventList") || !jsonObject.get("eventList").isJsonArray()) {
         jsonObject.add("eventList", new JsonArray());
      }

      return jsonObject;
   }
}
