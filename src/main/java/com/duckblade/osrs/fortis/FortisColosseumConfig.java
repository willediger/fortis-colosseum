package com.duckblade.osrs.fortis;

import com.duckblade.osrs.fortis.features.timetracking.SplitsFileWriter;
import com.duckblade.osrs.fortis.features.timetracking.SplitsOverlayMode;
import com.duckblade.osrs.fortis.features.waves.EnemyNameMode;
import com.duckblade.osrs.fortis.features.waves.WaveOverlayMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

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

	@ConfigSection(
		name = "Splits",
		description = "Time tracking and splits",
		position = 200
	)
	String SECTION_SPLITS = "splits";

	@ConfigItem(
		keyName = "splitsOverlayMode",
		name = "Overlay Panel",
		description = "Show splits as an overlay panel.",
		position = 201,
		section = SECTION_SPLITS
	)
	default SplitsOverlayMode splitsOverlayMode()
	{
		return SplitsOverlayMode.OFF;
	}

	@ConfigItem(
		keyName = "splitsFileCondition",
		name = "Save to File",
		description = "Save splits to files in .runelite/fortis-colosseum/splits/",
		position = 202,
		section = SECTION_SPLITS
	)
	default SplitsFileWriter.WriteCondition splitsFileCondition()
	{
		return SplitsFileWriter.WriteCondition.NEVER;
	}

	String KEY_LIVESPLIT_PORT = "splitsLivesplitPort";
	@ConfigItem(
		keyName = KEY_LIVESPLIT_PORT,
		name = "LiveSplit Port",
		description = "Send splits events to LiveSplit. Set to 0 to disable.<br>Requires LiveSplit Server. See the plugin README for more details.",
		position = 203,
		section = SECTION_SPLITS
	)
	@Range(min = 0, max = 65535)
	default int splitsLivesplitPort()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "splitsLivesplitAutoReset",
		name = "LiveSplit Auto-Reset",
		description = "Automatically restart the timer at Wave 1 when a new run is started.",
		position = 204,
		section = SECTION_SPLITS
	)
	default boolean splitsLivesplitAutoReset()
	{
		return false;
	}

}
