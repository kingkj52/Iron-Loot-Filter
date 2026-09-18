package com.ironmangrounditems;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class IronmanGroundItemsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IronmanGroundItemsPlugin.class);
		RuneLite.main(args);
	}
}
