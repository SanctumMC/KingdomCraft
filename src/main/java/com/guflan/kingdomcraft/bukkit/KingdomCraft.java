package com.guflan.kingdomcraft.bukkit;

import com.guflan.kingdomcraft.api.KingdomCraftBridge;
import com.guflan.kingdomcraft.bukkit.bridge.BukkitKingdomCraftBridge;
import com.guflan.kingdomcraft.bukkit.chat.ChatHandler;
import com.guflan.kingdomcraft.bukkit.command.BukkitCommandExecutor;
import com.guflan.kingdomcraft.bukkit.listeners.ConnectionListener;
import com.guflan.kingdomcraft.bukkit.placeholders.BukkitPlaceholderReplacer;
import com.guflan.kingdomcraft.common.storage.Storage;
import com.guflan.kingdomcraft.common.storage.implementation.EBeanStorageImplementation;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Copyrighted 2020 iGufGuf
 *
 * This file is part of KingdomCraft.
 *
 * Kingdomcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KingdomCraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with KingdomCraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
public class KingdomCraft extends JavaPlugin {

	public KingdomCraftBridge bridge;

	@Override
	public void onEnable() {
		// LOAD CONFIG

		YamlConfiguration config = new YamlConfiguration();

		File configFile = new File(this.getDataFolder(), "config.yml");
		if ( !configFile.exists() ) {
			saveResource("config.yml", true);
			//configFile = new File(this.getDataFolder(), "config.yml");
		}

		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			getLogger().warning("Database section not found in config.yml!");
			disable();
			return;
		}

		// DATABASE

		if ( !config.contains("database") ) {
			getLogger().warning("Database section not found in config.yml!");
			disable();
			return;
		}

        ConfigurationSection dbConfig = config.getConfigurationSection("database");
		EBeanStorageImplementation impl = new EBeanStorageImplementation(
				this,
				dbConfig.getString("url"),
                dbConfig.getString("driver"),
                dbConfig.getString("username"),
                dbConfig.getString("password")
		);
		Storage storage = new Storage(this, impl);

		this.bridge = new BukkitKingdomCraftBridge(this, storage);

		new BukkitPlaceholderReplacer(this);
		new ChatHandler(this);

		// commands
		BukkitCommandExecutor commandHandler = new BukkitCommandExecutor(this);
		PluginCommand command = getCommand("kingdomcraft");
		command.setExecutor(commandHandler);
		command.setTabCompleter(commandHandler);

		// listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ConnectionListener(this), this);
	}

	private void disable() {
		this.getPluginLoader().disablePlugin(this);
	}

	public KingdomCraftBridge getBridge() {
		return bridge;
	}
}