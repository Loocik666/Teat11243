package com.example.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Seen extends JavaPlugin {
   public Seen(@NonNullDecl JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      super.setup();
      Lang.init("en_us");
      this.getCommandRegistry().registerCommand(new SeenCommand("seen", Lang.get("commands.seen.description"), false));
      this.getCommandRegistry().registerCommand(new PlayTimeCommand("playtime", Lang.get("commands.playtime.description")));
      this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, (event) -> {
         PlayerRef playerRef = event.getPlayerRef();
         new SaveConnect(playerRef, true);
      });
      this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
         PlayerRef playerRef = event.getPlayerRef();
         new SaveConnect(playerRef, false);
      });
   }
}
