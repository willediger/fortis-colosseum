package com.duckblade.osrs.fortis.features.modifiers;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import com.duckblade.osrs.fortis.util.Modifier;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class ModifierSelectNamesManager implements PluginLifecycleComponent
{

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;
	private static final int SCRIPT_MODIFIER_SELECT_OUTLINE = 4938;

	private static final int WIDGET_OPTION_1 = WidgetUtil.packComponentId(865, 15);
	private static final int WIDGET_OPTION_2 = WidgetUtil.packComponentId(865, 16);
	private static final int WIDGET_OPTION_3 = WidgetUtil.packComponentId(865, 17);

	private final EventBus eventBus;
	private final Client client;
	private final ColosseumStateTracker stateTracker;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.modifiersNamesOnSelectWidget() && colosseumState.isInColosseum();
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
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT && e.getScriptId() != SCRIPT_MODIFIER_SELECT_OUTLINE)
		{
			return;
		}

		List<Modifier> options = stateTracker.getModifierOptions();
		if (options.isEmpty())
		{
			log.debug("Can't add names, no options detected by state tracker");
			return;
		}

		installName(client.getWidget(WIDGET_OPTION_1), options.get(0));
		installName(client.getWidget(WIDGET_OPTION_2), options.get(1));
		installName(client.getWidget(WIDGET_OPTION_3), options.get(2));
	}

	private void installName(Widget container, Modifier option)
	{
		if (container == null)
		{
			log.debug("Can't install name for modifier {}, widget was not found", option);
			return;
		}

		int targetLevel = option.getLevelVarb() == -1 ? 1 : option.getLevel(client) + 1;
		String name = option.getName(targetLevel);
		container.createChild(WidgetType.TEXT)
			.setText(name)
			.setTextShadowed(true)
			.setTextColor(0xFFFFFF)
			.setFontId(FontID.PLAIN_11)
			.setSize(0, 0, WidgetSizeMode.MINUS, WidgetSizeMode.MINUS)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.BOTTOM)
			.setPos(0, 0, WidgetPositionMode.ABSOLUTE_CENTER, WidgetPositionMode.ABSOLUTE_BOTTOM)
			.revalidate();
	}
}
