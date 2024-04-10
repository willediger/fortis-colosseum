package com.duckblade.osrs.fortis.debugplugins;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.LoggerFactory;

@Singleton
@PluginDescriptor(
	name = "Fortis Colosseum Debug"
)
public class FortisColosseumDebugPlugin extends Plugin
{

	private FortisColosseumDebugPanel debugPanel;

	@Override
	protected void startUp()
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
		((Logger) LoggerFactory.getLogger("com.duckblade.osrs.fortis")).setLevel(Level.DEBUG);

		SwingUtilities.invokeLater(() -> debugPanel = injector.getInstance(FortisColosseumDebugPanel.class));
	}

	@Override
	protected void shutDown() throws Exception
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted e)
	{
		if (e.getCommand().equals("fortis"))
		{
			if (debugPanel.isVisible())
			{
				debugPanel.close();
			}
			else
			{
				debugPanel.open();
			}
		}
	}
}
