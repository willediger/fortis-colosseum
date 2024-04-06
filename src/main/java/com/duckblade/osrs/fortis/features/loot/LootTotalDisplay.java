package com.duckblade.osrs.fortis.features.loot;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
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
public class LootTotalDisplay implements PluginLifecycleComponent
{

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;

	private static final int WIDGET_PARENT_EARNED = WidgetUtil.packComponentId(865, 5);
	private static final int WIDGET_PARENT_NEXT = WidgetUtil.packComponentId(865, 11);
	private static final int WIDGET_GP_TOTAL = WidgetUtil.packComponentId(865, 37);
	private static final int WIDGET_GP_EARNED = WidgetUtil.packComponentId(865, 9);
	private static final int WIDGET_GP_NEXT = WidgetUtil.packComponentId(865, 40);

	private final EventBus eventBus;
	private final Client client;
	private final FortisColosseumConfig config;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState state)
	{
		return config.lootInterfaceShowTotal() != LootTotalMode.OFF && state.isInColosseum();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		uninstall();
		eventBus.unregister(this);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT)
		{
			return;
		}

		Widget total = client.getWidget(WIDGET_GP_TOTAL);
		Widget earnedParent = client.getWidget(WIDGET_PARENT_EARNED);
		Widget earnedGpLabel = client.getWidget(WIDGET_GP_EARNED);
		Widget nextParent = client.getWidget(WIDGET_PARENT_NEXT);
		Widget nextGpLabel = client.getWidget(WIDGET_GP_NEXT);
		if (total == null || earnedParent == null || earnedGpLabel == null || nextParent == null || nextGpLabel == null)
		{
			return;
		}

		// this widget contains all the items and the total label
		int totalEarned = 0;
		Widget[] children = total.getChildren();
		if (children != null && children.length != 0)
		{
			totalEarned = readGPLabel(children[children.length - 1]);
		}

		LootTotalMode mode = config.lootInterfaceShowTotal();
		if (mode == LootTotalMode.EARNED || mode == LootTotalMode.BOTH)
		{
			addTotalWidget(earnedParent, earnedGpLabel, "Total", totalEarned);
		}

		if (mode == LootTotalMode.POTENTIAL || mode == LootTotalMode.BOTH)
		{
			int potential = totalEarned + readGPLabel(nextGpLabel);
			addTotalWidget(nextParent, nextGpLabel, "Potential", potential);
		}
	}

	private void uninstall()
	{
		// everything vanilla is static children, we can deleteAll
		Widget earned = client.getWidget(WIDGET_PARENT_EARNED);
		if (earned != null)
		{
			earned.deleteAllChildren();
		}

		Widget next = client.getWidget(WIDGET_PARENT_NEXT);
		if (next != null)
		{
			next.deleteAllChildren();
		}
	}

	private int readGPLabel(Widget reference)
	{
		String totalLabel = reference.getText();
		if (totalLabel != null && totalLabel.endsWith(" GP"))
		{
			String withoutGP = totalLabel.substring(0, totalLabel.length() - 3);
			String sanitized = withoutGP.replace(",", "");
			return Integer.parseInt(sanitized);
		}
		return 0;
	}

	private void addTotalWidget(Widget parent, Widget yRef, String prefix, int value)
	{
		parent.createChild(WidgetType.TEXT)
			.setPos(0, yRef.getOriginalY() + 26, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP)
			.setSize(0, 26, WidgetSizeMode.MINUS, WidgetSizeMode.ABSOLUTE)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setFontId(FontID.PLAIN_11)
			.setText(String.format("%s:<br>%,d GP", prefix, value))
			.setTextColor(0xFFFFFF)
			.setTextShadowed(true)
			.setLineHeight(14)
			.revalidate();
	}
}
