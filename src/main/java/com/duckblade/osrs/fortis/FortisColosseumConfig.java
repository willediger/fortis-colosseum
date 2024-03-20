package com.duckblade.osrs.fortis;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FortisColosseumConfig.CONFIG_GROUP)
public interface FortisColosseumConfig extends Config
{

	String CONFIG_GROUP = "fortiscolosseum";

	@ConfigItem(
		keyName = "leftClickBankAll",
		name = "Left-Click Bank-All",
		description = "Switch the two-click Bank-All to a single click in the loot chest interface.",
		position = 1
	)
	default boolean leftClickBankAll()
	{
		return true;
	}

}
