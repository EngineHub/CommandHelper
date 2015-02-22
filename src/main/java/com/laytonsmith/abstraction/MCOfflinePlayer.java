
package com.laytonsmith.abstraction;

import java.util.UUID;

/**
 *
 * 
 */
public interface MCOfflinePlayer extends MCAnimalTamer{
    public long getFirstPlayed();

    public long getLastPlayed();

    /**
     * Returns the name of this player
     *
     * @return Player name
     */
	@Override
    public String getName();

    /**
     * Gets a {@link Player} object that this represents, if there is one
     * <p>
     * If the player is online, this will return that player. Otherwise,
     * it will return null.
     *
     * @return Online player
     */
    public MCPlayer getPlayer();

    public boolean hasPlayedBefore();

    /**
     * Checks if this player is banned or not
     *
     * @return true if banned, otherwise false
     */
    public boolean isBanned();

    /**
     * Checks if this player is currently online
     *
     * @return true if they are online
     */
    public boolean isOnline();

    /**
     * Checks if this player is whitelisted or not
     *
     * @return true if whitelisted
     */
    public boolean isWhitelisted();

    /**
	 * Bans or unbans this player.
	 * Depending on how the server retrieved the player, this may or may not work.
	 *
     * @param banned true if banned
     */
    public void setBanned(boolean banned);

	/**
	 * Sets if this player is whitelisted or not.
	 * Depending on how the server retrieved the player, this may or may not work.
	 *
     * @param value true if whitelisted
     */
    public void setWhitelisted(boolean value);

	/**
     * Gets the Location where the player will spawn at their bed, null if they have not slept in one or their current bed spawn is invalid.
     *
     * @return Bed Spawn Location if bed exists, otherwise null.
     */
	public MCLocation getBedSpawnLocation();

	public UUID getUniqueID();
}
