package com.duckblade.osrs.fortis.module;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.features.LeftClickBankAll;
import com.duckblade.osrs.fortis.features.timetracking.SplitsFileWriter;
import com.duckblade.osrs.fortis.features.timetracking.SplitsOverlay;
import com.duckblade.osrs.fortis.features.timetracking.SplitsTracker;
import com.duckblade.osrs.fortis.features.timetracking.livesplit.LiveSplitManager;
import com.duckblade.osrs.fortis.features.waves.WavesOverlay;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class FortisColosseumModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ComponentManager.class);
	}

	@Provides
	@Singleton
	Set<PluginLifecycleComponent> lifecycleComponents(
		ColosseumStateTracker colosseumStateTracker,
		LeftClickBankAll leftClickBankAll,
		LiveSplitManager liveSplitManager,
		SplitsFileWriter splitsFileWriter,
		SplitsOverlay splitsOverlay,
		SplitsTracker splitsTracker,
		WavesOverlay wavesOverlay
	)
	{
		return ImmutableSet.of(
			colosseumStateTracker,
			leftClickBankAll,
			liveSplitManager,
			splitsFileWriter,
			splitsOverlay,
			splitsTracker,
			wavesOverlay
		);
	}

	@Provides
	@Singleton
	FortisColosseumConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FortisColosseumConfig.class);
	}

}
