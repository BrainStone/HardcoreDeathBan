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

package com.mstiles92.plugins.hardcoredeathban.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.mstiles92.plugins.commonutils.calendar.CalendarUtils;
import com.mstiles92.plugins.hardcoredeathban.HardcoreDeathBan;
import com.mstiles92.plugins.hardcoredeathban.tasks.KickRunnable;

/**
 * Bans is a class used to store and modify the ban length of each player.
 *
 * @author mstiles92
 */
public class Bans {
    public Set<String> deathClasses;

    private final HardcoreDeathBan plugin;
    private YamlConfiguration config;
    private File file;

    /**
     * The main constructor to be used with this class.
     *
     * @param plugin   the instance of the plugin
     * @param filename name of the file to save to disk
     * @throws Exception if there is an error while opening or creating the file
     */
    public Bans(HardcoreDeathBan plugin, String filename) throws Exception {
        this.plugin = plugin;
        load(filename);

        this.deathClasses = plugin.getConfig().getConfigurationSection("Death-Classes").getKeys(false);
        if (this.deathClasses.size() == 0) {
            plugin.log("No death classes found.");
        } else {
            for (String s : this.deathClasses) {
                plugin.log("Death class loaded: " + s);
            }
        }
    }

    private void load(String filename) throws Exception {
        file = new File(plugin.getDataFolder(), filename);
        config = new YamlConfiguration();

        if (!file.exists()) {
            file.createNewFile();
        }
        config.load(file);
    }

    /**
     * Save the config to a file.
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(ChatColor.RED + "Error occurred while saving bans config file.");
        }
    }

    /**
     * Get the date and time that the specified player is unbanned after.
     *
     * @param player name of the player to check
     * @return a Calendar object that specifies the date and time when the
     * player's ban is over, or null if the player is not banned
     */
    public Calendar getUnbanCalendar(UUID player) {
        if (player == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        final long ms = config.getLong(player.toString(), 0);
        if (ms == 0) {
            return null;
        }
        calendar.setTimeInMillis(ms);
        return calendar;
    }

    /**
     * Check if the specified player is currently banned.
     *
     * @param player the name of the player to check
     * @return true if the player is currently banned, false otherwise
     */
    public boolean checkPlayerIsBanned(UUID player) {
        final Calendar unban = getUnbanCalendar(player);
        final Calendar now = Calendar.getInstance();
        if (unban != null) {
            if (unban.after(now)) {
                return true;
            }
            unbanPlayer(player);
        }
        return false;
    }

    /**
     * Unban the specified player.
     *
     * @param player name of the player to be unbanned
     */
    public void unbanPlayer(UUID player) {
        plugin.log("Player unbanned: " + player);
        config.set(player.toString(), null);
        
        ImprovedOfflinePlayer offlinePlayer = new ImprovedOfflinePlayer(player);
		if (!offlinePlayer.exists())
			return;

		offlinePlayer.setAutoSave(false);

		offlinePlayer.setSleeping(true);
		offlinePlayer.setDeathTime(0);
		offlinePlayer.setAir(30);
		offlinePlayer.setFireTicks(-20);
		offlinePlayer.setHealth(20);
		offlinePlayer.setFoodLevel(20);
		offlinePlayer.setScore(0);
		offlinePlayer.setLevel(0);
		offlinePlayer.setTotalExperience(0);
		offlinePlayer.setFallDistance(0);
		offlinePlayer.setHealF(20.0F);
		offlinePlayer.setExp(0.0F);
		
		offlinePlayer.setLocation(offlinePlayer.getBedSpawnLocation());

		offlinePlayer.savePlayerData();
    }

    /**
     * Ban a player for their default time, taking possible death classes into account.
     *
     * @param player the player to ban
     */
    public void banPlayer(UUID player) {
		final Player p = plugin.getServer().getPlayer(player);
        if (p != null) {
            for (String s : this.deathClasses) {
                Permission perm = new Permission("deathban.class." + s);
                perm.setDefault(PermissionDefault.FALSE);
                if (p.hasPermission(perm)) {
                    banPlayer(player, plugin.getConfig().getString("Death-Classes." + s + ".Ban-Time"));
                    plugin.log("Death class " + s + " detected for " + player);
                    return;
                }
            }
        }
        plugin.log("No death class detected for " + player);
        banPlayer(player, plugin.getConfig().getString("Ban-Time"));
    }

    /**
     * Ban a player for a specified time.
     *
     * @param player the player to ban
     * @param time   the amount of time the player will be banned
     */
    public void banPlayer(UUID player, String time) {
		final Player p = plugin.getServer().getPlayer(player);
        try {
            final Calendar unbanDate = CalendarUtils.parseTimeDifference(time);

            if (p != null) {            // Player is online
                if (!p.hasPermission("deathban.ban.exempt")) {
                    config.set(player.toString(), unbanDate.getTimeInMillis());
                    save();
                    plugin.log("Player added to ban list: " + player);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new KickRunnable(plugin, player), plugin.getConfig().getInt("Tick-Delay"));
                }
            } else {                    // Player is offline
                config.set(player.toString(), unbanDate.getTimeInMillis());
                save();
                plugin.log("Offline player added to ban list: " + player);
            }
        } catch (Exception e) {
            plugin.log("Error occurred while banning player: " + player);
        }
    }
}
