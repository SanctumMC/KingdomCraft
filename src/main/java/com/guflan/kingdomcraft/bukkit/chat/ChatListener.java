package com.guflan.kingdomcraft.bukkit.chat;

import com.guflan.kingdomcraft.api.entity.Player;
import com.guflan.kingdomcraft.bukkit.KingdomCraft;
import com.guflan.kingdomcraft.bukkit.entity.BukkitPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final KingdomCraft plugin;

    public ChatListener(KingdomCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = new BukkitPlayer(event.getPlayer());
        plugin.getBridge().getChatManager().handle(player, event.getMessage());
        event.setCancelled(true);
    }
}