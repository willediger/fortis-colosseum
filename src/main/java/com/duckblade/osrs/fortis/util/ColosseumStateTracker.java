package com.duckblade.osrs.fortis.util;

import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ColosseumStateTracker implements PluginLifecycleComponent
{

	private static final int REGION_LOBBY = 7316;
	private static final int REGION_COLOSSEUM = 7216;

	private static final ColosseumState DEFAULT_STATE = new ColosseumState(false, false);

	private final Client client;
	private final EventBus eventBus;

	@Getter
	private ColosseumState currentState = DEFAULT_STATE;

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe(priority = 5)
	public void onGameTick(GameTick e)
	{
		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		int region = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();

		setState(new ColosseumState(region == REGION_LOBBY, region == REGION_COLOSSEUM), false);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		switch (e.getGameState())
		{
			case LOGGING_IN:
			case HOPPING:
				setState(DEFAULT_STATE, true);
		}
	}

	private void setState(ColosseumState newValue, boolean forceEvent)
	{
		ColosseumState previous = currentState;
		currentState = newValue;

		if (forceEvent || !currentState.equals(previous))
		{
			eventBus.post(new ColosseumStateChanged(previous, currentState));
		}
	}
}
