// $Id$
/*
 * CommandHelper
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.commandhelper;

import java.util.Set;
import java.util.HashSet;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author sk89q
 */
public class SimplePlayerFilter extends PlayerFilter {
    /**
     * Store a list of groups to implicitly match a player against.
     */
    @SuppressWarnings("unused")
    private Set<String> groups;
    /**
     * Store a list of groups to explicitly match a player against.
     */
    @SuppressWarnings("unused")
    private Set<String> exclusiveGroups;
    /**
     * Store a list of player names to match against.
     */
    private Set<String> players;

    /**
     * Construct the object.
     *
     * @param groups
     * @param exclusiveGroups
     * @param players
     */
    private SimplePlayerFilter(Server server, Set<String> groups,
            Set<String> exclusiveGroups, Set<String> players) {
        super(server);
        this.groups = groups;
        this.exclusiveGroups = exclusiveGroups;
        this.players = players;
    }

    /**
     * Checks to see if a player matches this query.
     * 
     * @param player
     * @return
     */
    @Override
    public boolean matches(Player player) {
        String name = player.getName();
        
        if (players.contains(name)) {
            return true;
        }

        /*String[] playerGroups = player.getGroups();
        int numGroups = playerGroups.length;

        if (numGroups == 1) {
            if (exclusiveGroups.contains(playerGroups[0])) {
                return true;
            }
        }

        for (String group : playerGroups) {
            if (groups.contains(group)) {
                return true;
            }
        }*/

        return false;
    }

    /**
     * Parse a string.
     * @param server 
     * 
     * @param query
     * @return
     */
    public static PlayerFilter parse(Server server, String query) {
        if (query.equals("*")) {
            return new FriendlyFilter(server);
        }

        String[] parts = query.split(",");

        Set<String> groups = new HashSet<String>();
        Set<String> exclusiveGroups = new HashSet<String>();
        Set<String> players = new HashSet<String>();

        for (String part : parts) {
            if (part.length() == 0) {
                continue;

            }
            if (part.charAt(0) == '+') {
                exclusiveGroups.add(part);
            } else if (part.charAt(0) == '~') {
                players.add(part);
            } else {
                groups.add(part);
            }
        }

        return new SimplePlayerFilter(server, groups, exclusiveGroups, players);
    }
}
