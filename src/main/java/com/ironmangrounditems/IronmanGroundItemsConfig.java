package com.ironmangrounditems;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Display preferences, stored by RuneLite so they survive a restart.
 * <p>
 * Nothing here changes what the account is allowed to pick up; the settings only decide how much
 * of what the server has already refused is drawn or offered.
 */
@ConfigGroup(IronmanGroundItemsConfig.GROUP)
public interface IronmanGroundItemsConfig extends Config
{
	String GROUP = "ironmangrounditems";

	@ConfigItem(
		keyName = "hideModels",
		name = "Hide the item models",
		description = "Stop drawing piles that hold nothing you can pick up. A pile holding one of"
			+ " your own items stays visible until that item is gone",
		position = 0
	)
	default boolean hideModels()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideMenuEntries",
		name = "Remove the take options",
		description = "Drop Take entries for items you cannot pick up, so left click falls through to"
			+ " whatever is underneath and they never appear on right click",
		position = 1
	)
	default boolean hideMenuEntries()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideExamine",
		name = "Remove examine too",
		description = "Also drop the Examine entry for those items",
		position = 2
	)
	default boolean hideExamine()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupDropsTakeable",
		name = "Group drops are takeable",
		description = "Treat group owned items as yours. Turn this off if you are not a group ironman"
			+ " and still see group owned items you cannot take",
		position = 3
	)
	default boolean groupDropsTakeable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "testOnNormalAccount",
		name = "Filter on a normal account",
		description = "Apply the ironman rules on an account that has no restrictions. Only useful for"
			+ " checking the plugin works before taking it to an ironman",
		position = 4
	)
	default boolean testOnNormalAccount()
	{
		return false;
	}
}
