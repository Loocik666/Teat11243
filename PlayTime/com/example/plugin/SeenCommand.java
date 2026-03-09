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
      this.username = this.withRequiredArg("username", "Nom du joueur", ArgTypes.STRING);
   }

   protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      String targetName = (String)commandContext.get(this.username);
      JsonObject jsonObj = (new FileExist()).checkPlayerFile(targetName);
      if (jsonObj != null && jsonObj.has("eventList")) {
         JsonArray eventList = jsonObj.getAsJsonArray("eventList");
         if (eventList.size() == 0) {
            playerRef.sendMessage(Message.raw(Lang.get("commands.generic.no_events", targetName)).bold(true).color(Color.RED));
         } else {
            JsonObject lastEvent = eventList.get(eventList.size() - 1).getAsJsonObject();
            boolean isStillConnected = lastEvent.get("connect").getAsBoolean();
            if (isStillConnected) {
               playerRef.sendMessage(Message.raw(Lang.get("commands.seen.online", targetName)).bold(true).color(Color.GREEN));
            } else {
               long timestamp = lastEvent.get("timestamp").getAsLong();
               LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
               playerRef.sendMessage(Message.raw(Lang.get("commands.seen.offline", targetName, dateTime.format(formatter))).color(Color.GREEN));
            }

         }
      } else {
         playerRef.sendMessage(Message.raw(Lang.get("commands.generic.player_not_found", targetName)).bold(true).color(Color.RED));
      }
   }
}
