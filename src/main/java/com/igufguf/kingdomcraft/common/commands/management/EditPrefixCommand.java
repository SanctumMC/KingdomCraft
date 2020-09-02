package com.igufguf.kingdomcraft.common.commands.management;

import com.igufguf.kingdomcraft.api.KingdomCraftPlugin;
import com.igufguf.kingdomcraft.api.command.CommandSender;
import com.igufguf.kingdomcraft.api.domain.Kingdom;
import com.igufguf.kingdomcraft.api.domain.Player;
import com.igufguf.kingdomcraft.common.command.DefaultCommandBase;

public class EditPrefixCommand extends DefaultCommandBase {

    public EditPrefixCommand(KingdomCraftPlugin plugin) {
        super(plugin, "edit prefix", 1, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if ( !sender.hasPermission("kingdom.edit.prefix") ) {
            plugin.getMessageManager().send(sender, "noPermission");
            return;
        }

        Player player = sender.getPlayer();
        Kingdom kingdom = player.getKingdom();
        if ( kingdom == null ) {
            plugin.getMessageManager().send(sender, "cmdDefaultSenderNoKingdom");
            return;
        }

        kingdom.setPrefix(args[0]);
        plugin.getKingdomManager().saveKingdom(kingdom);

        plugin.getMessageManager().send(sender, "cmdEditSuccess", "prefix", args[0], kingdom.getName());
    }
}