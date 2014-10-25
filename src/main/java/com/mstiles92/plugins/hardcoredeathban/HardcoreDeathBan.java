/*
 * This document is a part of the source code and related artifacts for
 * HardcoreDeathBan, an open source Bukkit plugin for hardcore-type servers
 * where players are temporarily banned upon death.
 *
 * http://dev.bukkit.org/bukkit-plugins/hardcoredeathban/
 * http://github.com/mstiles92/HardcoreDeathBan
 *
 * Copyright (c) 2014 Matthew Stiles (mstiles92)
 *
 * Licensed under the Common Development and Distribution License Version 1.0
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the CDDL-1.0 License at
 * http://opensource.org/licenses/CDDL-1.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the license.
 */

package com.mstiles92.plugins.hardcoredeathban;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.mstiles92.plugins.commonutils.calendar.CalendarUtils;
import com.mstiles92.plugins.commonutils.commands.CommandRegistry;
import com.mstiles92.plugins.commonutils.updates.UpdateChecker;
import com.mstiles92.plugins.hardcoredeathban.commands.Credits;
import com.mstiles92.plugins.hardcoredeathban.commands.Deathban;
import com.mstiles92.plugins.hardcoredeathban.listeners.PlayerListener;
import com.mstiles92.plugins.hardcoredeathban.util.Bans;
import com.mstiles92.plugins.hardcoredeathban.util.RevivalCredits;

/**
 * HardcoreDeathBan is the main class of this Bukkit plugin. It handles enabling
 * and disabling of this plugin, loading config files, and other general methods
 * needed for this plugin's operation.
 * 
 * @author mstiles92
 */
public class HardcoreDeathBan extends JavaPlugin {
	private static HardcoreDeathBan instance;

	public static HardcoreDeathBan getInstance() {
		return instance;
	}

	private UpdateChecker updateChecker;

	private CommandRegistry commandRegistry;
	public RevivalCredits credits = null;

	public Bans bans = null;
	private final SimpleDateFormat TimeFormat = new SimpleDateFormat(
			"hh:mm a z");

	private final SimpleDateFormat DateFormat = new SimpleDateFormat(
			"MM/dd/yyyy");

	public UpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	public void log(String message) {
		if (getConfig().getBoolean("Verbose")) {
			getLogger().info(message);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return commandRegistry.handleCommand(sender, command, label, args);
	}

	@Override
	public void onDisable() {
		credits.save();
		bans.save();
		saveConfig();
	}

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		try {
			credits = new RevivalCredits(this, "credits.yml");
			bans = new Bans(this, "bans.yml");
		} catch (final Exception e) {
			getLogger()
					.warning(
							ChatColor.RED
									+ "Error opening a config file. Plugin will now be disabled.");
			getPluginLoader().disablePlugin(this);
		}

		if (getConfig().getBoolean("Check-for-Updates")) {
			updateChecker = new UpdateChecker("hardcoredeathban", getLogger(),
					getDescription().getVersion());
			getServer().getScheduler().runTaskTimer(this, updateChecker, 40,
					216000);
		}

		getServer().getPluginManager().registerEvents(new PlayerListener(this),
				this);

		commandRegistry = new CommandRegistry(this);
		commandRegistry.registerCommands(new Deathban());
		commandRegistry.registerCommands(new Credits());

		try {
			log(Class.forName("java.util.UUID").toString());
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			log(Class
					.forName(
							"com.mstiles92.plugins.hardcoredeathban.util.ImprovedOfflinePlayer")
					.toString());
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String replaceVariables(String msg, String name, UUID uuid) {
		final Calendar now = Calendar.getInstance();
		final Calendar unbanTime = bans.getUnbanCalendar(uuid);

		msg = msg.replaceAll("%server%", getServer().getServerName());
		if (name != null) {
			msg = msg.replaceAll("%player%", name);
		}

		msg = msg.replaceAll("%currenttime%", TimeFormat.format(now.getTime()));
		msg = msg.replaceAll("%currentdate%", DateFormat.format(now.getTime()));

		if (unbanTime != null) {
			msg = msg.replaceAll("%unbantime%",
					TimeFormat.format(unbanTime.getTime()));
			msg = msg.replaceAll("%unbandate%",
					DateFormat.format(unbanTime.getTime()));
			msg = msg.replaceAll("%bantimeleft%",
					CalendarUtils.buildTimeDifference(now, unbanTime));
		}
		return msg;
	}
}
