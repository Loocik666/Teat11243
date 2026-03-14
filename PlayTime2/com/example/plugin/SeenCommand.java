package com.example.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nonnull;

public class SeenCommand extends AbstractPlayerCommand {
   private final RequiredArg<String> username;

   public SeenCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
      this.username = this.withRequiredArg("username", Lang.get("commands.arguments.username"), ArgTypes.STRING);
   }

   protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      String targetUsername = (String)commandContext.get(this.username);
      JsonObject playerDataJson = (new PlayerFileManager()).readPlayerFile(targetUsername);
      if (playerDataJson != null && playerDataJson.has("eventList")) {
         JsonArray playerEventList = playerDataJson.getAsJsonArray("eventList");
         if (playerEventList.size() == 0) {
            playerRef.sendMessage(Message.raw(Lang.get("commands.generic.no_events", targetUsername)).bold(true).color(Color.RED));
         } else {
            JsonObject latestEvent = playerEventList.get(playerEventList.size() - 1).getAsJsonObject();
            boolean isStillConnected = latestEvent.get("connect").getAsBoolean();
            if (isStillConnected) {
               playerRef.sendMessage(Message.raw(Lang.get("commands.seen.online", targetUsername)).bold(true).color(Color.GREEN));
            } else {
               long eventTimestamp = latestEvent.get("timestamp").getAsLong();
               LocalDateTime eventDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(eventTimestamp), ZoneId.systemDefault());
               DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
               playerRef.sendMessage(Message.raw(Lang.get("commands.seen.offline", targetUsername, eventDateTime.format(dateFormatter))).color(Color.GREEN));
            }

         }
      } else {
         playerRef.sendMessage(Message.raw(Lang.get("commands.generic.player_not_found", targetUsername)).bold(true).color(Color.RED));
      }
   }
}
