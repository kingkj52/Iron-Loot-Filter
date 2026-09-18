package com.ironlootfilter;

import net.runelite.api.TileItem;

/**
 * Pickup rules, kept away from the client so they can be tested.
 * <p>
 * The server sends an ownership flag with every ground item, and that flag is the same thing the
 * server consults when it decides whether a pickup is allowed. Enumerating the sources of loot
 * instead - player drops, monster drops, raid supplies, ashes from a fire that burnt out - would
 * mean reimplementing that decision badly and rechecking it after every game update, so nothing
 * here looks at item IDs or locations.
 */
final class Takeable
{
    static final int NORMAL_ACCOUNT = 0;

    private Takeable() { }

    static boolean isRestricted(int accountType)
    { return accountType != NORMAL_ACCOUNT; }

    static boolean canTake(boolean restricted, int ownership, boolean groupDropsTakeable)
    {
        if (!restricted) return true;
        switch (ownership)
        {
            case TileItem.OWNERSHIP_OTHER: return false;
            case TileItem.OWNERSHIP_GROUP: return groupDropsTakeable;
            // OWNERSHIP_SELF, plus OWNERSHIP_NONE for world spawns and anything the server never
            // attributed to a player.
            default: return true;
        }
    }
}
