package com.duckblade.osrs.fortis.features.waves;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import com.duckblade.osrs.fortis.util.spawns.WaveSpawn;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class WavesOverlay extends OverlayPanel implements PluginLifecycleComponent
{

	private static final Color HEADER_COLOR = ColorScheme.BRAND_ORANGE;
	private static final Color SPAWN_COLOR = Color.white;
	private static final Color REINFORCEMENT_COLOR = ColorScheme.GRAND_EXCHANGE_ALCH;

	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final FortisColosseumConfig config;
	private final ColosseumStateTracker stateTracker;

	@Inject
	private WavesOverlay(
		EventBus eventBus,
		OverlayManager overlayManager,
		FortisColosseumConfig config,
		ColosseumStateTracker stateTracker
	)
	{
		this.eventBus = eventBus;
		this.overlayManager = overlayManager;
		this.config = config;
		this.stateTracker = stateTracker;

		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.wavesOverlayMode() != WaveOverlayMode.OFF
			&& colosseumState.isInColosseum();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WaveOverlayMode mode = config.wavesOverlayMode();
		EnemyNameMode nameMode = config.wavesOverlayNames();

		ColosseumState state = this.stateTracker.getCurrentState();
		if (mode.showCurrent())
		{
			addTitleLine(state.getWaveNumber());
			state.getWaveSpawns().getSpawns().forEach(s -> addSpawnLine(nameMode, s, false));
			state.getWaveSpawns().getReinforcements().forEach(s -> addSpawnLine(nameMode, s, true));
		}

		if (mode == WaveOverlayMode.BOTH && state.getWaveNumber() != 12)
		{
			// a lil spacer
			panelComponent.getChildren().add(TitleComponent.builder().text("").build());
		}

		if (mode.showNext() && state.getWaveNumber() != 12)
		{
			addTitleLine(state.getWaveNumber() + 1);
			state.getNextWaveSpawns().getSpawns().forEach(s -> addSpawnLine(nameMode, s, false));
			state.getNextWaveSpawns().getReinforcements().forEach(s -> addSpawnLine(nameMode, s, true));
		}

		return super.render(graphics);
	}

	private void addTitleLine(int wave)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Wave " + wave)
			.color(HEADER_COLOR)
			.build());
	}

	private void addSpawnLine(EnemyNameMode nameMode, WaveSpawn spawn, boolean isReinforcement)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(spawn.getCount() + "x " + nameMode.nameOf(spawn.getEnemy()))
			.leftColor(isReinforcement ? REINFORCEMENT_COLOR : SPAWN_COLOR)
			.build());
	}
}
