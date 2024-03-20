package com.duckblade.osrs.fortis.module;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateChanged;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.GameEventManager;

/**
 * Manages all the subcomponents of the plugin
 * so they can register themselves to RuneLite resources
 * e.g. EventBus/OverlayManager/init on startup/etc
 * instead of the FortisColosseumPlugin class handling everything.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ComponentManager
{

	private final EventBus eventBus;
	private final GameEventManager gameEventManager;
	private final FortisColosseumConfig config;
	private final ColosseumStateTracker colosseumStateTracker;
	private final Set<PluginLifecycleComponent> components;

	private final Map<PluginLifecycleComponent, Boolean> states = new HashMap<>();

	public void onPluginStart()
	{
		eventBus.register(this);
		components.forEach(c -> states.put(c, false));
		revalidateComponentStates();
	}

	public void onPluginStop()
	{
		eventBus.unregister(this);
		components.stream()
			.filter(states::get)
			.forEach(this::tryShutDown);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (!FortisColosseumConfig.CONFIG_GROUP.equals(e.getGroup()))
		{
			return;
		}

		revalidateComponentStates();
	}

	@Subscribe
	public void onColosseumStateChanged(ColosseumStateChanged e)
	{
		revalidateComponentStates();
	}

	private void revalidateComponentStates()
	{
		ColosseumState colosseumState = colosseumStateTracker.getCurrentState();
		components.forEach(c ->
		{
			boolean shouldBeEnabled = c.isEnabled(config, colosseumState);
			boolean isEnabled = states.get(c);
			if (shouldBeEnabled == isEnabled)
			{
				return;
			}

			if (shouldBeEnabled)
			{
				tryStartUp(c);
			}
			else
			{
				tryShutDown(c);
			}
		});
	}

	private void tryStartUp(PluginLifecycleComponent component)
	{
		if (states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Enabling FortisColosseum plugin component [{}]", component.getClass().getName());
		}

		try
		{
			component.startUp();
			gameEventManager.simulateGameEvents(component);
			states.put(component, true);
		}
		catch (Exception e)
		{
			log.error("Failed to start FortisColosseum plugin component [{}]", component.getClass().getName(), e);
		}
	}

	private void tryShutDown(PluginLifecycleComponent component)
	{
		if (!states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Disabling FortisColosseum plugin component [{}]", component.getClass().getName());
		}

		try
		{
			component.shutDown();
		}
		catch (Exception e)
		{
			log.error("Failed to cleanly shut down FortisColosseum plugin component [{}]", component.getClass().getName());
		}
		finally
		{
			states.put(component, false);
		}
	}

}
