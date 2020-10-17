/*
 * This file is part of KingdomCraft.
 *
 * KingdomCraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KingdomCraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KingdomCraft. If not, see <https://www.gnu.org/licenses/>.
 */

package com.guflan.kingdomcraft.bukkit;

import com.guflan.kingdomcraft.bukkit.chat.ChatHandler;
import com.guflan.kingdomcraft.bukkit.command.CommandHandler;
import com.guflan.kingdomcraft.bukkit.config.BukkitConfig;
import com.guflan.kingdomcraft.bukkit.friendlyfire.FriendlyFireListener;
import com.guflan.kingdomcraft.bukkit.listeners.ConnectionListener;
import com.guflan.kingdomcraft.bukkit.listeners.PlayerListener;
import com.guflan.kingdomcraft.bukkit.placeholders.BukkitPlaceholderReplacer;
import com.guflan.kingdomcraft.bukkit.scheduler.BukkitScheduler;
import com.guflan.kingdomcraft.common.KingdomCraftPlugin;
import com.guflan.kingdomcraft.common.config.KingdomCraftConfig;
import com.guflan.kingdomcraft.common.ebean.EBeanContext;
import com.guflan.kingdomcraft.common.scheduler.AbstractScheduler;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class KingdomCraftBukkitPlugin extends JavaPlugin implements KingdomCraftPlugin {

	private final BukkitScheduler scheduler;

	private KingdomCraftBukkit kdc;

	public KingdomCraftBukkitPlugin() {
		this.scheduler = new BukkitScheduler(this);
	}

	@Override
	public void onEnable() {
		// LOAD CONFIG

		YamlConfiguration config = new YamlConfiguration();

		File configFile = new File(this.getDataFolder(), "config.yml");
		if ( !configFile.exists() ) {
			saveResource("config.yml", true);
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


		// For some reason, required ebean classes are not in the classpath of the current classloader.
		// Fix this by changing the class loader to the one of the current class.
		ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		// create database
		ConfigurationSection dbConfig = config.getConfigurationSection("database");
		EBeanContext ec = new EBeanContext(this);
		boolean result = ec.init(
				dbConfig.getString("url"),
				dbConfig.getString("driver"),
				dbConfig.getString("username"),
				dbConfig.getString("password"));

		// revert class loader to original
		Thread.currentThread().setContextClassLoader(originalContextClassLoader);

		if ( !result ) {
			log("Error occured during database initialization. Shutting down plugin.", Level.SEVERE);
			disable();
			return;
		}

		// initialize handler
		KingdomCraftConfig cfg = new BukkitConfig(config);
		this.kdc = new KingdomCraftBukkit(this, cfg, ec);

		// placeholders
		new BukkitPlaceholderReplacer(this);

		// chat
		new ChatHandler(this);

		// commands
		CommandHandler commandHandler = new CommandHandler(this);
		PluginCommand command = getCommand("kingdomcraft");
		command.setExecutor(commandHandler);
		command.setTabCompleter(commandHandler);

		// listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new FriendlyFireListener(this), this);

		// commands
		// TODO
	}

	private void disable() {
		this.getPluginLoader().disablePlugin(this);
	}

	public KingdomCraftBukkit getKdc() {
		return kdc;
	}

	//

	@Override
	public AbstractScheduler getScheduler() {
		return scheduler;
	}

	@Override
	public void log(String msg) {
		getLogger().info(msg);
	}

	@Override
	public void log(String msg, Level level) {
		getLogger().log(level, msg);
	}
}
