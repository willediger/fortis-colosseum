package com.duckblade.osrs.fortis.features.timetracking.livesplit;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.features.timetracking.Split;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateChanged;
import com.duckblade.osrs.fortis.util.TimerMode;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LiveSplitManager implements PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final FortisColosseumConfig config;
	private final LiveSplitWriter ls;

	private boolean active;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.splitsLivesplitPort() != 0;
	}

	@Override
	public void startUp()
	{
		ls.setTargetPort(config.splitsLivesplitPort());
		ls.startUp();
		eventBus.register(this);
		active = true;
	}

	@Override
	public void shutDown()
	{
		active = false;
		eventBus.unregister(this);
		ls.shutDown();
	}

	@Subscribe(priority = 1)
	public void onConfigChanged(ConfigChanged e)
	{
		if (Objects.equals(e.getGroup(), FortisColosseumConfig.CONFIG_GROUP) && Objects.equals(e.getKey(), FortisColosseumConfig.KEY_LIVESPLIT_PORT))
		{
			ls.setTargetPort(config.splitsLivesplitPort());
		}
	}

	@Subscribe(priority = 1)
	public void onColosseumStateChanged(ColosseumStateChanged e)
	{
		if (!e.getPreviousState().isInColosseum() && e.getNewState().isInColosseum() && config.splitsLivesplitAutoReset())
		{
			ls.sendCommand("reset");
		}
	}

	public void onRunStart()
	{
		if (active)
		{
			ls.sendCommand("starttimer");
		}
	}

	public void onSplit(Split e)
	{
		if (active)
		{
			ls.sendCommand("pausegametime");
			ls.sendCommand("setgametime " + TimerMode.PRECISE.format(e.getCumulativeDuration()));
			ls.sendCommand("split");
			ls.sendCommand("unpausegametime");
		}
	}
}
