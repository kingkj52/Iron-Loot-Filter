package com.ironlootfilter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class IronLootFilterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IronLootFilterPlugin.class);
		RuneLite.main(args);
	}
}
