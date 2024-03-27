package com.duckblade.osrs.fortis.util;

import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import java.util.Collections;
import java.util.EnumSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
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

	private static final ColosseumState DEFAULT_STATE = new ColosseumState(false, false, 1, false, Collections.emptySet());

	private final Client client;
	private final EventBus eventBus;

	@Getter
	private ColosseumState currentState = DEFAULT_STATE;

	private int waveNumber = 1;
	private boolean waveStarted = false;
	private final EnumSet<Handicap> handicaps = EnumSet.noneOf(Handicap.class); // todo track these

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

		boolean inLobby = region == REGION_LOBBY;
		boolean inColosseum = client.isInInstancedRegion() && region == REGION_COLOSSEUM;

		if (!inColosseum)
		{
			waveNumber = 1;
			waveStarted = false;
		}

		setState(
			new ColosseumState(inLobby, inColosseum, waveNumber, waveStarted, handicaps),
			false
		);
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

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE || !getCurrentState().isInColosseum())
		{
			return;
		}

		String msg = e.getMessage();
		if (msg.contains("Wave: ")) // only wave start messages contain a :
		{
			waveNumber = Integer.parseInt(msg.substring(18, msg.length() - 6));
			waveStarted = true;
		}
		else if (msg.startsWith("Wave ") && msg.contains("completed!"))
		{
			// it's either a two-char number or a number and a space
			waveNumber = Integer.parseInt(msg.substring(5, 7).trim()) + 1;
			waveStarted = false;
		}
	}

	private void setState(ColosseumState newValue, boolean forceEvent)
	{
		if (!forceEvent && currentState.equals(newValue))
		{
			return;
		}

		ColosseumState previous = currentState;
		currentState = newValue;
		eventBus.post(new ColosseumStateChanged(previous, currentState));
	}
}
