package com.example.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.time.Instant;
import javax.annotation.Nonnull;

public class SaveConnect {
   public SaveConnect(@Nonnull PlayerRef playerRef, boolean connectOrDisconnect) {
      JsonObject jsonObj = (new PlayerFileManager()).checkPlayerFile(playerRef.getUsername());
      JsonArray eventList = jsonObj.getAsJsonArray("eventList");
      JsonObject newEvent = new JsonObject();
      long timestamp = Instant.now().getEpochSecond();
      newEvent.addProperty("timestamp", timestamp);
      newEvent.addProperty("connect", connectOrDisconnect);
      eventList.add(newEvent);
      (new PlayerFileManager()).savePlayerFile(playerRef.getUsername(), jsonObj);
   }
}
