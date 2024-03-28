package com.duckblade.osrs.fortis.util;

import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class ColosseumStateTracker implements PluginLifecycleComponent
{

	private static final int REGION_LOBBY = 7316;
	private static final int REGION_COLOSSEUM = 7216;

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;
	private static final int VARBIT_HANDICAP_SELECTED = 9788;

	private static final ColosseumState DEFAULT_STATE = new ColosseumState(false, false, 1, false, Collections.emptyMap());

	private final Client client;
	private final EventBus eventBus;

	@Getter
	private ColosseumState currentState = DEFAULT_STATE;

	private int waveNumber = 1;
	private boolean waveStarted = false;

	private final List<Handicap> handicapOptions = new ArrayList<>(3);
	private final Map<Handicap, Integer> handicaps = new EnumMap<>(Handicap.class);

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
			handicapOptions.clear();
			handicaps.clear();
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
			trackSelectedHandicap();
		}
		else if (msg.startsWith("Wave ") && msg.contains("completed!"))
		{
			// it's either a two-char number or a number and a space
			waveNumber = Integer.parseInt(msg.substring(5, 7).trim()) + 1;
			waveStarted = false;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT)
		{
			return;
		}

		try
		{
			// pull the options available for next wave from the script args
			handicapOptions.clear();
			Object[] args = e.getScriptEvent().getArguments();
			handicapOptions.add(Handicap.forId((Integer) args[2]));
			handicapOptions.add(Handicap.forId((Integer) args[3]));
			handicapOptions.add(Handicap.forId((Integer) args[4]));
			log.debug("Handicap options = {}", handicapOptions);

			// also refresh the previously selected handicaps from the same
			for (Handicap h : Handicap.forBitmask((Integer) args[8]))
			{
				handicaps.put(h, h.getLevel(client));
			}
		}
		catch (Exception ex)
		{
			// very much so don't want to throw uncaught into script eval
			log.warn("failed to extract handicaps from arguments", ex);
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

	private void trackSelectedHandicap()
	{
		if (handicapOptions.isEmpty())
		{
			log.warn("Wave started but handicap options were not tracked");
			return;
		}

		int selectedIx = client.getVarbitValue(VARBIT_HANDICAP_SELECTED);
		if (selectedIx == 0)
		{
			log.debug("varb {} = 0, no handicap selected?", VARBIT_HANDICAP_SELECTED);
			return;
		}

		Handicap selected = handicapOptions.get(selectedIx - 1);
		handicapOptions.clear();
		if (selected == null)
		{
			log.warn("Failed to select handicap with index {}, options = {}", selectedIx, handicapOptions);
			return;
		}

		handicaps.put(selected, selected.getLevel(client));
		log.debug("Tracking handicap selection {} (ix {}), handicaps = {}", selected, selectedIx, handicaps);
	}
}
