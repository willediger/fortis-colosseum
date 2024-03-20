package com.duckblade.osrs.fortis;

import com.duckblade.osrs.fortis.features.waves.EnemyNameMode;
import com.duckblade.osrs.fortis.features.waves.WaveOverlayMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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

	@ConfigSection(
		name = "Waves Overlay",
		description = "Show which enemies are in the current and next wave.",
		position = 100
	)
	String SECTION_WAVES_OVERLAY = "wavesOverlay";

	@ConfigItem(
		keyName = "wavesOverlayMode",
		name = "Display",
		description = "Enable or disable the waves display components.",
		position = 101,
		section = SECTION_WAVES_OVERLAY
	)
	default WaveOverlayMode wavesOverlayMode()
	{
		return WaveOverlayMode.BOTH;
	}

	@ConfigItem(
		keyName = "wavesOverlayNames",
		name = "Names",
		description = "Whether to use official NPC names (e.g. Serpent Shaman), or colloquial (e.g. Mage)",
		position = 102,
		section = SECTION_WAVES_OVERLAY
	)
	default EnemyNameMode wavesOverlayNames()
	{
		return EnemyNameMode.COLLOQUIAL;
	}

}
