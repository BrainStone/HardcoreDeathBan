package com.mstiles92.plugins.hardcoredeathban.util;

/**
 * ImprovedOfflinePlayer, a library for Bukkit.
 * Copyright (C) 2012 one4me@github.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.v1_5_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_5_R3.NBTTagCompound;
import net.minecraft.server.v1_5_R3.NBTTagDouble;
import net.minecraft.server.v1_5_R3.NBTTagFloat;
import net.minecraft.server.v1_5_R3.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * @name ImprovedOfflinePlayer
 * @version 1.5.0
 * @author one4me
 */
public class ImprovedOfflinePlayer {
	private String player;
	private File file;
	private NBTTagCompound compound;
	private boolean exists = false;
	private boolean autosave = true;

	public ImprovedOfflinePlayer(OfflinePlayer offlineplayer) {
		exists = this.loadPlayerData(offlineplayer.getUniqueId());
	}

	public ImprovedOfflinePlayer(UUID uuid) {
		exists = this.loadPlayerData(uuid);
	}
	
	@SuppressWarnings("deprecation")
	public ImprovedOfflinePlayer(String player) {
		exists = this.loadPlayerData(Bukkit.getOfflinePlayer(player).getUniqueId());
	}

	@SuppressWarnings("deprecation")
	public static UUID getUUIDFromName(String player) {
		return Bukkit.getOfflinePlayer(player).getUniqueId();
	}

	public boolean exists() {
		return exists;
	}

	public boolean getAutoSave() {
		return autosave;
	}

	// public PlayerAbilities getAbilities() {
	// PlayerAbilities pa = new PlayerAbilities();
	// pa.a(this.compound);
	// return pa;
	// }
	// public void setAbilities(PlayerAbilities abilities) {
	// abilities.a(this.compound);
	// savePlayerData();
	// }
	public Location getBedSpawnLocation() {
		return new Location(Bukkit.getWorld(compound.getString("SpawnWorld")),
				compound.getInt("SpawnX"), compound.getInt("SpawnY"),
				compound.getInt("SpawnZ"));
	}

	public int getDeathTime() {
		return compound.getShort("Death Time");
	}

	public float getExhaustion() {
		return compound.getFloat("foodExhaustionLevel");
	}

	public float getExp() {
		return compound.getFloat("XpP");
	}

	public float getFallDistance() {
		return compound.getFloat("FallDistance");
	}

	public int getFireTicks() {
		return compound.getShort("Fire");
	}

	public float getFlySpeed() {
		return compound.getCompound("abilities").getFloat("flySpeed");
	}

	public int getFoodLevel() {
		return compound.getInt("foodLevel");
	}

	public int getFoodTickTimer() {
		return compound.getInt("foodTickTimer");
	}

	@SuppressWarnings("deprecation")
	public GameMode getGameMode() {
		return GameMode.getByValue(compound.getInt("playerGameType"));
	}

	public float getHealF() {
		return compound.getFloat("HealF");
	}

	public int getHealth() {
		return compound.getShort("Health");
	}

	public boolean getIsInvulnerable() {
		return compound.getBoolean("Invulnerable");
	}

	public boolean getIsOnGround() {
		return compound.getBoolean("OnGround");
	}

	public boolean getIsSleeping() {
		return compound.getBoolean("Sleeping");
	}

	public int getLevel() {
		return compound.getInt("XpLevel");
	}

	public Location getLocation() {
		final NBTTagList position = compound.getList("Pos");
		final NBTTagList rotation = compound.getList("Rotation");
		return new Location(
				Bukkit.getWorld(new UUID(compound.getLong("WorldUUIDMost"),
						compound.getLong("WorldUUIDLeast"))),
				((NBTTagDouble) position.get(0)).data,
				((NBTTagDouble) position.get(1)).data,
				((NBTTagDouble) position.get(2)).data,
				((NBTTagFloat) rotation.get(0)).data,
				((NBTTagFloat) rotation.get(1)).data);
	}

	public String getName() {
		return player;
	}

	@SuppressWarnings("deprecation")
	public ArrayList<PotionEffect> getPotionEffects() {
		final ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		if (compound.hasKey("ActiveEffects")) {
			final NBTTagList list = compound.getList("ActiveEffects");
			for (int i = 0; i < list.size(); i++) {
				final NBTTagCompound effect = (NBTTagCompound) list.get(i);
				final byte amp = effect.getByte("Amplifier");
				final byte id = effect.getByte("Id");
				final int time = effect.getInt("Duration");
				effects.add(new PotionEffect(PotionEffectType.getById(id),
						time, amp));
			}
		}
		return effects;
	}

	public int getRemainingAir() {
		return compound.getShort("Air");
	}

	public float getSaturation() {
		return compound.getFloat("foodSaturationLevel");
	}

	public short getTimeAttack() {
		return compound.getShort("AttackTime");
	}

	public short getTimeDeath() {
		return compound.getShort("DeathTime");
	}

	public short getTimeHurt() {
		return compound.getShort("HurtTime");
	}

	public short getTimeSleep() {
		return compound.getShort("SleepTimer");
	}

	public int getTotalExperience() {
		return compound.getInt("XpTotal");
	}

	public Vector getVelocity() {
		final NBTTagList list = compound.getList("Motion");
		return new Vector(((NBTTagDouble) list.get(0)).data,
				((NBTTagDouble) list.get(1)).data,
				((NBTTagDouble) list.get(2)).data);
	}

	public float getWalkSpeed() {
		return compound.getCompound("abilities").getFloat("walkSpeed");
	}

	private boolean loadPlayerData(UUID uuid) {
		try {
			player = uuid.toString();
			for (final World w : Bukkit.getWorlds()) {
				file = new File(w.getWorldFolder(), "playerdata" + File.separator
						+ player + ".dat");
				if (file.exists()) {
					compound = NBTCompressedStreamTools.a(new FileInputStream(
							file));
					player = file.getCanonicalFile().getName()
							.replace(".dat", "");
					return true;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void savePlayerData() {
		if (exists) {
			try {
				NBTCompressedStreamTools
						.a(compound, new FileOutputStream(file));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setAutoSave(boolean autosave) {
		this.autosave = autosave;
	}

	public void setBedSpawnLocation(Location location, Boolean override) {
		compound.setInt("SpawnX", (int) location.getX());
		compound.setInt("SpawnY", (int) location.getY());
		compound.setInt("SpawnZ", (int) location.getZ());
		compound.setString("SpawnWorld", location.getWorld().getName());
		compound.setBoolean("SpawnForced", override == null ? false : override);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setDeathTime(int input) {
		compound.setShort("DeathTime", (short) input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setExhaustion(float input) {
		compound.setFloat("foodExhaustionLevel", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setExp(float input) {
		compound.setFloat("XpP", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setFallDistance(float input) {
		compound.setFloat("FallDistance", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setFireTicks(int input) {
		compound.setShort("Fire", (short) input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setFlySpeed(float speed) {
		compound.getCompound("abilities").setFloat("flySpeed", speed);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setFoodLevel(int input) {
		compound.setInt("foodLevel", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setFoodTickTimer(int input) {
		compound.setInt("foodTickTimer", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	@SuppressWarnings("deprecation")
	public void setGameMode(GameMode input) {
		compound.setInt("playerGameType", input.getValue());
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setHealF(float healF) {
		compound.setFloat("HealF", healF);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setHealth(int input) {
		compound.setShort("Health", (short) input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setIsInvulnerable(boolean input) {
		compound.setBoolean("Invulnerable", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setIsOnGround(boolean input) {
		compound.setBoolean("OnGround", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setIsSleeping(boolean input) {
		compound.setBoolean("Sleeping", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setLevel(int input) {
		compound.setInt("XpLevel", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	@SuppressWarnings("deprecation")
	public void setLocation(Location location) {
		final World w = location.getWorld();
		final UUID uuid = w.getUID();
		compound.setLong("WorldUUIDMost", uuid.getMostSignificantBits());
		compound.setLong("WorldUUIDLeast", uuid.getLeastSignificantBits());
		compound.setInt("Dimension", w.getEnvironment().getId());
		final NBTTagList position = new NBTTagList();
		position.add(new NBTTagDouble(null, location.getX()));
		position.add(new NBTTagDouble(null, location.getY()));
		position.add(new NBTTagDouble(null, location.getZ()));
		compound.set("Pos", position);
		final NBTTagList rotation = new NBTTagList();
		rotation.add(new NBTTagFloat(null, location.getYaw()));
		rotation.add(new NBTTagFloat(null, location.getPitch()));
		compound.set("Rotation", rotation);
		if (autosave) {
			this.savePlayerData();
		}
	}

	@SuppressWarnings("deprecation")
	public void setPotionEffects(ArrayList<PotionEffect> effects) {
		if (effects.isEmpty()) {
			compound.remove("ActiveEffects");
			if (autosave) {
				this.savePlayerData();
			}
			return;
		}
		final NBTTagList activeEffects = new NBTTagList();
		for (final PotionEffect pe : effects) {
			final NBTTagCompound eCompound = new NBTTagCompound();
			eCompound.setByte("Amplifier", (byte) (pe.getAmplifier()));
			eCompound.setByte("Id", (byte) (pe.getType().getId()));
			eCompound.setInt("Duration", (pe.getDuration()));
			activeEffects.add(eCompound);
		}
		compound.set("ActiveEffects", activeEffects);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setRemainingAir(int input) {
		compound.setShort("Air", (short) input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setSaturation(float input) {
		compound.setFloat("foodSaturationLevel", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setTimeAttack(short input) {
		compound.setShort("AttackTime", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setTimeDeath(short input) {
		compound.setShort("DeathTime", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setTimeHurt(short input) {
		compound.setShort("HurtTime", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setTimeSleep(short input) {
		compound.setShort("SleepTimer", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setTotalExperience(int input) {
		compound.setInt("XpTotal", input);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setVelocity(Vector vector) {
		final NBTTagList motion = new NBTTagList();
		motion.add(new NBTTagDouble(null, vector.getX()));
		motion.add(new NBTTagDouble(null, vector.getY()));
		motion.add(new NBTTagDouble(null, vector.getZ()));
		compound.set("Motion", motion);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setWalkSpeed(float speed) {
		compound.getCompound("abilities").setFloat("walkSpeed", speed);
		if (autosave) {
			this.savePlayerData();
		}
	}

	public void setAir(int input) {
		compound.setShort("Air", (short) input);
		if (autosave) {
			this.savePlayerData();
		}
	}
	
	public short getAir() {
		return compound.getShort("Air");
	}

	public void setSleeping(boolean input) {
		compound.setBoolean("Sleeping", input);
		if (autosave) {
			this.savePlayerData();
		}
	}
	
	public boolean getSleeping() {
		return compound.getBoolean("Sleeping");
	}

	public void setScore(int input) {
		compound.setInt("Score", input);
		if (autosave) {
			this.savePlayerData();
		}
	}
	
	public int getScore() {
		return compound.getInt("Score");
	}
}
/*
 * Copyright (C) 2012 one4me@github.com
 */