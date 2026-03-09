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
      super("playtime", Lang.get("commands.playtime.description"));
      this.usernameArg = this.withRequiredArg("username", Lang.get("commands.arguments.username"), ArgTypes.STRING);
      this.startDateArg = this.withOptionalArg("start", Lang.get("commands.arguments.start_date"), ArgTypes.STRING);
      this.endDateArg = this.withOptionalArg("end", Lang.get("commands.arguments.end_date"), ArgTypes.STRING);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      String targetName = (String)context.get(this.usernameArg);
      String startStr = (String)context.get(this.startDateArg);
      String endStr = (String)context.get(this.endDateArg);
      long minTs = startStr != null ? this.parseDateToUnix(startStr) : 0L;
      long maxTs = endStr != null ? this.parseDateToUnix(endStr) : Long.MAX_VALUE;
      JsonObject jsonObj = (new FileExist()).checkPlayerFile(targetName);
      if (jsonObj != null && jsonObj.has("eventList")) {
         JsonArray eventList = jsonObj.getAsJsonArray("eventList");
         long totalSeconds = this.calculateTime(eventList, minTs, maxTs);
         playerRef.sendMessage(Message.raw(Lang.get("commands.playtime.result", targetName, this.formatDuration(totalSeconds))).bold(true).color(Color.GREEN));
      } else {
         playerRef.sendMessage(Message.raw(Lang.get("commands.generic.player_not_found", targetName)).bold(true).color(Color.RED));
      }
   }

   private long calculateTime(JsonArray eventList, long minTs, long maxTs) {
      long total = 0L;
      Long sessionStart = null;

      long s;
      for(int i = 0; i < eventList.size(); ++i) {
         JsonObject ev = eventList.get(i).getAsJsonObject();
         s = ev.get("timestamp").getAsLong();
         boolean isConnect = ev.get("connect").getAsBoolean();
         if (isConnect) {
            sessionStart = s;
         } else if (sessionStart != null) {
            long s = Math.max(sessionStart, minTs);
            long e = Math.min(s, maxTs);
            if (e > s) {
               total += e - s;
            }

            sessionStart = null;
         }
      }

      if (sessionStart != null) {
         long now = Instant.now().getEpochSecond();
         s = Math.max(sessionStart, minTs);
         long e = Math.min(now, maxTs);
         if (e > s) {
            total += e - s;
         }
      }

      return total;
   }

   private long parseDateToUnix(String input) {
      try {
         return LocalDateTime.parse(input, DATE_TIME_FORMAT).atZone(ZoneId.systemDefault()).toEpochSecond();
      } catch (Exception var5) {
         try {
            return LocalDate.parse(input, DATE_ONLY_FORMAT).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
         } catch (Exception var4) {
            return 0L;
         }
      }
   }

   private String formatDuration(long s) {
      return String.format("%dh %dm %ds", s / 3600L, s % 3600L / 60L, s % 60L);
   }
}
