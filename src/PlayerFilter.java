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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author sk89q
 */
public abstract class PlayerFilter implements Iterable<Player> {
    /**
     * Evaluate the query, getting a list of players.
     *
     * @return
     */
    public List<Player> evaluate() {
        List<Player> players = new ArrayList<Player>();

        for (Player player : etc.getServer().getPlayerList()) {
            if (matches(player)) {
                players.add(player);
            }
        }

        return players;
    }

    /**
     * Returns true if this query matches this player.
     * 
     * @param player
     * @return
     */
    public abstract boolean matches(Player player);

    /**
     * Returns an iterator.
     * 
     * @return
     */
    public Iterator<Player> iterator() {
        return evaluate().iterator();
    }
}
