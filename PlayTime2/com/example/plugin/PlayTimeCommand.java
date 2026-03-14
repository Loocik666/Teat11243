package com.example.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nonnull;

public class PlayTimeCommand extends AbstractPlayerCommand {
   private final RequiredArg<String> usernameArg;
   private final OptionalArg<String> startDateArg;
   private final OptionalArg<String> endDateArg;
   private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm");
   private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

   public PlayTimeCommand(String playtime, String description) {
      super(playtime, description);
      this.usernameArg = this.withRequiredArg("username", Lang.get("commands.arguments.username"), ArgTypes.STRING);
      this.startDateArg = this.withOptionalArg("start", Lang.get("commands.arguments.start_date"), ArgTypes.STRING);
      this.endDateArg = this.withOptionalArg("end", Lang.get("commands.arguments.end_date"), ArgTypes.STRING);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      String targetUsername = (String)context.get(this.usernameArg);
      String startStr = (String)context.get(this.startDateArg);
      String endStr = (String)context.get(this.endDateArg);
      Long parsedStart = startStr != null ? this.parseDateToUnix(startStr) : null;
      Long parsedEnd = endStr != null ? this.parseDateToUnix(endStr) : null;
      if (startStr != null && parsedStart == null) {
         playerRef.sendMessage(Message.raw(Lang.get("commands.playtime.invalid_date", "start", startStr)).bold(true).color(Color.RED));
         return;
      }

      if (endStr != null && parsedEnd == null) {
         playerRef.sendMessage(Message.raw(Lang.get("commands.playtime.invalid_date", "end", endStr)).bold(true).color(Color.RED));
         return;
      }

      long startTimestamp = parsedStart != null ? parsedStart : 0L;
      long endTimestamp = parsedEnd != null ? parsedEnd : Long.MAX_VALUE;
      if (startTimestamp > endTimestamp) {
         playerRef.sendMessage(Message.raw(Lang.get("commands.playtime.invalid_range")).bold(true).color(Color.RED));
         return;
      }

      JsonObject playerDataJson = (new PlayerFileManager()).readPlayerFile(targetUsername);
      if (playerDataJson != null && playerDataJson.has("eventList")) {
         JsonArray playerEventList = playerDataJson.getAsJsonArray("eventList");
         long totalPlaySeconds = this.calculateTime(playerEventList, startTimestamp, endTimestamp);
         playerRef.sendMessage(Message.raw(Lang.get("commands.playtime.result", targetUsername, this.formatDuration(totalPlaySeconds))).bold(true).color(Color.GREEN));
      } else {
         playerRef.sendMessage(Message.raw(Lang.get("commands.generic.player_not_found", targetUsername)).bold(true).color(Color.RED));
      }
   }

   private long calculateTime(JsonArray playerEventList, long startTimestamp, long endTimestamp) {
      long total = 0L;
      Long currentSessionStart = null;

      for(int i = 0; i < playerEventList.size(); ++i) {
         JsonObject ev = playerEventList.get(i).getAsJsonObject();
         long eventTimestamp = ev.get("timestamp").getAsLong();
         boolean isConnectEvent = ev.get("connect").getAsBoolean();
         if (isConnectEvent) {
            currentSessionStart = eventTimestamp;
         } else if (currentSessionStart != null) {
            long sessionStart = Math.max(currentSessionStart, startTimestamp);
            long sessionEnd = Math.min(eventTimestamp, endTimestamp);
            if (sessionEnd > sessionStart) {
               total += sessionEnd - sessionStart;
            }

            currentSessionStart = null;
         }
      }

      if (currentSessionStart != null) {
         long now = Instant.now().getEpochSecond();
         long sessionStart = Math.max(currentSessionStart, startTimestamp);
         long sessionEnd = Math.min(now, endTimestamp);
         if (sessionEnd > sessionStart) {
            total += sessionEnd - sessionStart;
         }
      }

      return total;
   }

   private Long parseDateToUnix(String input) {
      try {
         return LocalDateTime.parse(input, DATE_TIME_FORMAT).atZone(ZoneId.systemDefault()).toEpochSecond();
      } catch (Exception var5) {
         try {
            return LocalDate.parse(input, DATE_ONLY_FORMAT).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
         } catch (Exception var4) {
            return null;
         }
      }
   }

   private String formatDuration(long s) {
      return String.format("%dh %dm %ds", s / 3600L, s % 3600L / 60L, s % 60L);
   }
}
