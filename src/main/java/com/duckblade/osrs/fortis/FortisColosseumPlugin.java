package com.duckblade.osrs.fortis;

import com.duckblade.osrs.fortis.module.ComponentManager;
import com.duckblade.osrs.fortis.module.FortisColosseumModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Fortis Colosseum",
	description = "Utilities and information for the Fortis Colosseum.",
	tags = {"fortis", "colosseum", "varlamore", "dizana", "quiver"}
)
public class FortisColosseumPlugin extends Plugin
{

	@Inject
	private Injector injector;

	private ComponentManager componentManager = null;

	@Override
	public void configure(Binder binder)
	{
		binder.install(new FortisColosseumModule());
	}

	@Override
	protected void startUp() throws Exception
	{
		if (componentManager == null)
		{
			componentManager = injector.getInstance(ComponentManager.class);
		}
		componentManager.onPluginStart();
	}

	@Override
	protected void shutDown() throws Exception
	{
		componentManager.onPluginStop();
	}
}
