package com.duckblade.osrs.fortis.features;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class LeftClickBankAll implements PluginLifecycleComponent
{

	private static final String MENU_ENTRY_OPTION = "Bank-all";

	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.leftClickBankAll() && colosseumState.isInColosseum();
	}

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

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (MENU_ENTRY_OPTION.equals(e.getOption()))
		{
			e.getMenuEntry().setForceLeftClick(true);
		}
	}
}
