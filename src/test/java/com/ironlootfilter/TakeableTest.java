package com.ironlootfilter;

import static net.runelite.api.TileItem.OWNERSHIP_GROUP;
import static net.runelite.api.TileItem.OWNERSHIP_NONE;
import static net.runelite.api.TileItem.OWNERSHIP_OTHER;
import static net.runelite.api.TileItem.OWNERSHIP_SELF;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TakeableTest
{
	private static final int IRONMAN = 1;
	private static final int ULTIMATE_IRONMAN = 2;
	private static final int HARDCORE_IRONMAN = 3;
	private static final int GROUP_IRONMAN = 4;
	private static final int HARDCORE_GROUP_IRONMAN = 5;
	private static final int UNRANKED_GROUP_IRONMAN = 6;

	@Test
	public void normalAccountsAreNotRestricted()
	{
		assertFalse(Takeable.isRestricted(Takeable.NORMAL_ACCOUNT));
	}

	@Test
	public void everyIronmanVariantIsRestricted()
	{
		assertTrue(Takeable.isRestricted(IRONMAN));
		assertTrue(Takeable.isRestricted(ULTIMATE_IRONMAN));
		assertTrue(Takeable.isRestricted(HARDCORE_IRONMAN));
		assertTrue(Takeable.isRestricted(GROUP_IRONMAN));
		assertTrue(Takeable.isRestricted(HARDCORE_GROUP_IRONMAN));
		assertTrue(Takeable.isRestricted(UNRANKED_GROUP_IRONMAN));
	}

	@Test
	public void unrestrictedAccountsTakeAnything()
	{
		assertTrue(Takeable.canTake(false, OWNERSHIP_OTHER, true));
		assertTrue(Takeable.canTake(false, OWNERSHIP_OTHER, false));
	}

	@Test
	public void otherPlayersDropsAreBlocked()
	{
		assertFalse(Takeable.canTake(true, OWNERSHIP_OTHER, true));
		assertFalse(Takeable.canTake(true, OWNERSHIP_OTHER, false));
	}

	@Test
	public void ownDropsAreAllowed()
	{
		assertTrue(Takeable.canTake(true, OWNERSHIP_SELF, true));
	}

	@Test
	public void unownedItemsAreAllowed()
	{
		// Spawns, and ashes left behind by a fire that burnt out.
		assertTrue(Takeable.canTake(true, OWNERSHIP_NONE, true));
		assertTrue(Takeable.canTake(true, OWNERSHIP_NONE, false));
	}

	@Test
	public void groupItemsFollowTheSetting()
	{
		assertTrue(Takeable.canTake(true, OWNERSHIP_GROUP, true));
		assertFalse(Takeable.canTake(true, OWNERSHIP_GROUP, false));
	}
}
