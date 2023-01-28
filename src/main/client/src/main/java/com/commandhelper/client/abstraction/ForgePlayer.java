package com.commandhelper.client.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldBorder;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPlayerStatistic;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.core.constructs.Target;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;

/**
 *
 */
public class ForgePlayer implements MCPlayer {

	private final LocalPlayer player;

	public ForgePlayer(LocalPlayer localPlayer) {
		this.player = localPlayer;
	}

	@Override
	public boolean canSee(MCPlayer p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void chat(String chat) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public InetSocketAddress getAddress() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean getAllowFlight() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCLocation getCompassTarget() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getDisplayName() {
		return this.player.getName().getString();
	}

	@Override
	public float getExp() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getFlySpeed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFlySpeed(float speed) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPlayerInventory getInventory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemStack getItemAt(Integer slot) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getLevel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPlayerListName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPlayerListHeader() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPlayerListFooter() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public long getPlayerTime() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCWeather getPlayerWeather() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getRemainingFireTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCScoreboard getScoreboard() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getTotalExperience() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getExpToLevel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getExpAtLevel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntity getSpectatorTarget() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getWalkSpeed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setWalkSpeed(float speed) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void giveExp(int xp) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSneaking() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSprinting() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void kickPlayer(String message) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeEffect(MCPotionEffectType type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void resetPlayerTime() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void resetPlayerWeather() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendResourcePack(String url) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendTitle(String title, String subtitle, int fadein, int stay, int fadeout) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setAllowFlight(boolean flight) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCompassTarget(MCLocation l) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setDisplayName(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setExp(float i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFlying(boolean flight) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setLevel(int xp) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListName(String listName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListHeader(String header) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListFooter(String footer) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerTime(Long time, boolean relative) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerWeather(MCWeather type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setRemainingFireTicks(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setScoreboard(MCScoreboard board) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSpectatorTarget(MCEntity entity) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTempOp(Boolean value) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTotalExperience(int total) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setVanished(boolean set, MCPlayer to) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isNewPlayer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getHost() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendBlockChange(MCLocation loc, MCBlockData data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendBlockDamage(MCLocation loc, double progress) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendSignTextChange(MCLocation loc, String[] lines) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playSound(MCLocation l, String sound, float volume, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playSound(MCEntity ent, MCSound sound, MCSoundCategory category, float volume, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void stopSound(MCSound sound) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void stopSound(String sound) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void stopSound(MCSound sound, MCSoundCategory category) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void stopSound(String sound, MCSoundCategory category) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void spawnParticle(MCLocation l, MCParticle pa, int count, double offsetX, double offsetY, double offsetZ, double velocity, Object data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getFoodLevel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFoodLevel(int f) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getSaturation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSaturation(float s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getExhaustion() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setExhaustion(float e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setBedSpawnLocation(MCLocation l, boolean forced) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendPluginMessage(String channel, byte[] message) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isOp() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setOp(boolean bln) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isFlying() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateInventory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat, MCEntityType type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat, MCMaterial type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, int amount) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, MCEntityType type, int amount) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, MCMaterial type, int amount) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCWorldBorder getWorldBorder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setWorldBorder(MCWorldBorder border) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendMessage(String string) {
		player.sendSystemMessage(MutableComponent.create(new LiteralContents(string)));
	}

	@Override
	public MCServer getServer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasPermission(String perm) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isPermissionSet(String perm) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getGroups() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean inGroup(String groupName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object getHandle() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void closeInventory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCGameMode getGameMode() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemStack getItemInHand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCItemStack getItemOnCursor() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getSleepTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isBlocking() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryView openEnchanting(MCLocation location, boolean force) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryView openInventory(MCInventory inventory) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryView getOpenInventory() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventory getEnderChest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryView openWorkbench(MCLocation loc, boolean force) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCInventoryView openMerchant(MCMerchant merchant, boolean force) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setGameMode(MCGameMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setItemInHand(MCItemStack item) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setItemOnCursor(MCItemStack item) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getCooldown(MCMaterial material) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCooldown(MCMaterial material, int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getAttackCooldown() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeEffects() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCEffect> getEffects() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void damage(double amount) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void damage(double amount, MCEntity source) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean getCanPickupItems() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntityEquipment getEquipment() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getEyeHeight() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getEyeHeight(boolean ignoreSneaking) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCLocation getEyeLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getHealth() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPlayer getKiller() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getLastDamage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntity getLeashHolder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCLivingEntity getTarget(Target t) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCBlock getTargetBlock(HashSet<MCMaterial> transparent, int maxDistance) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCBlock getTargetSpace(int maxDistance) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCBlock> getLineOfSight(HashSet<MCMaterial> transparent, int maxDistance) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasLineOfSight(MCEntity other) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getMaxHealth() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getMaximumAir() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getNoDamageTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getRemainingAir() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isGliding() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isLeashed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasAI() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void resetMaxHealth() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCanPickupItems(boolean pickup) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setRemoveWhenFarAway(boolean remove) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setHealth(double health) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setLastDamage(double damage) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setLeashHolder(MCEntity holder) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setMaxHealth(double health) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setMaximumAir(int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setRemainingAir(int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTarget(MCLivingEntity target, Target t) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setGliding(Boolean glide) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setAI(Boolean ai) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCollidable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCollidable(boolean collidable) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void kill() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isTameable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getAttributeValue(MCAttribute attr) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getAttributeDefault(MCAttribute attr) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getAttributeBase(MCAttribute attr) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setAttributeBase(MCAttribute attr, double base) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void resetAttributeBase(MCAttribute attr) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCAttributeModifier> getAttributeModifiers(MCAttribute attr) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addAttributeModifier(MCAttributeModifier modifier) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeAttributeModifier(MCAttributeModifier modifier) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSleeping() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean eject() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getFallDistance() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getFireTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntityDamageEvent getLastDamageCause() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCLocation getLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getMaxFireTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCEntity> getNearbyEntities(double x, double y, double z) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCEntity> getPassengers() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getTicksLived() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntityType getType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UUID getUniqueId() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCEntity getVehicle() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Vector3D getVelocity() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setVelocity(Vector3D v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCWorld getWorld() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isDead() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isInsideVehicle() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isOnGround() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean leaveVehicle() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void playEffect(MCEntityEffect type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFallDistance(float distance) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFireTicks(int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setLastDamageCause(MCEntityDamageEvent event) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean setPassenger(MCEntity passenger) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTicksLived(int value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean teleport(MCEntity destination) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean teleport(MCEntity destination, MCTeleportCause cause) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean teleport(MCLocation location) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean teleport(MCLocation location, MCTeleportCause cause) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCustomName(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getCustomName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCustomNameVisible(boolean visible) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCustomNameVisible() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isGlowing() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setGlowing(Boolean glow) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasGravity() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setHasGravity(boolean gravity) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSilent() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSilent(boolean silent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isInvulnerable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setInvulnerable(boolean invulnerable) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<String> getScoreboardTags() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addScoreboardTag(String tag) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeScoreboardTag(String tag) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getFreezingTicks() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFreezingTicks(int ticks) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MCMetadataValue> getMetadata(String metadataKey) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCProjectile launchProjectile(MCEntityType projectile) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCProjectile launchProjectile(MCEntityType projectile, Vector3D init) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public long getFirstPlayed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public long getLastPlayed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCPlayer getPlayer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasPlayedBefore() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isBanned() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isOnline() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isWhitelisted() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setWhitelisted(boolean value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MCLocation getBedSpawnLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UUID getUniqueID() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
