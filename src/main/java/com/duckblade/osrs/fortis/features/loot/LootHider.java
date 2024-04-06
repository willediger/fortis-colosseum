package com.duckblade.osrs.fortis.features.loot;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.JavaScriptCallback;
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
public class LootHider implements PluginLifecycleComponent
{

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;

	private static final int WIDGET_LOOT_ITEMS = WidgetUtil.packComponentId(865, 39);
	private static final int WIDGET_LOOT_GP = WidgetUtil.packComponentId(865, 40);
	private static final int WIDGET_BUTTON_PARENT = WidgetUtil.packComponentId(865, 13);

	private static final int[] SPRITE_IDS_STANDARD = {
		913, 914, 915, 916, 917, 918, 919, 920,
	};
	private static final int[] SPRITE_IDS_HOVER = {
		921, 922, 923, 924, 925, 926, 927, 928,
	};

	private final EventBus eventBus;
	private final Client client;
	private final FortisColosseumConfig config;
	private final ColosseumStateTracker stateTracker;

	private Widget[] buttonGraphicWidgets = null;
	private Widget buttonTextWidget = null;

	private boolean showLootQueued = false;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState state)
	{
		return config.lootInterfaceHideNextWave() != LootHiderMode.OFF && state.isInColosseum();
	}

	@Override
	public void startUp()
	{
		buttonGraphicWidgets = null;
		buttonTextWidget = null;
		showLootQueued = false;
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		unhide();
		eventBus.unregister(this);
	}

	@Subscribe(priority = -10) // priority low just in case some other plugin reads the items out of here
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT)
		{
			return;
		}

		if (config.lootInterfaceHideNextWave() == LootHiderMode.WAVE_12 && stateTracker.getCurrentState().getWaveNumber() != 12)
		{
			return;
		}

		hide();
	}

	private void hide()
	{
		Widget itemPreview = client.getWidget(WIDGET_LOOT_ITEMS);
		Widget gpPreview = client.getWidget(WIDGET_LOOT_GP);
		Widget buttonParent = client.getWidget(WIDGET_BUTTON_PARENT);
		if (itemPreview == null || gpPreview == null || buttonParent == null)
		{
			return;
		}
		itemPreview.setHidden(true);
		gpPreview.setHidden(true);

		int w = 78;
		int h = 28;
		int x = buttonParent.getWidth() / 2 - w / 2; // of top left corner
		int y = buttonParent.getHeight() / 2 - h / 2;
		buttonGraphicWidgets = new Widget[]{
			buildGraphicWidget(buttonParent, x, y, 9, 9, SPRITE_IDS_STANDARD[0]), // corners
			buildGraphicWidget(buttonParent, x + w - 9, y, 9, 9, SPRITE_IDS_STANDARD[1]),
			buildGraphicWidget(buttonParent, x, y + h - 9, 9, 9, SPRITE_IDS_STANDARD[2]),
			buildGraphicWidget(buttonParent, x + w - 9, y + h - 9, 9, 9, SPRITE_IDS_STANDARD[3]),
			buildGraphicWidget(buttonParent, x, y + 9, 9, h - 18, SPRITE_IDS_STANDARD[4]), // edges
			buildGraphicWidget(buttonParent, x + 9, y, w - 18, 9, SPRITE_IDS_STANDARD[5]),
			buildGraphicWidget(buttonParent, x + w - 9, y + 9, 9, h - 18, SPRITE_IDS_STANDARD[6]),
			buildGraphicWidget(buttonParent, x + 9, y + h - 9, w - 18, 9, SPRITE_IDS_STANDARD[7]),
		};

		buttonTextWidget = buttonParent.createChild(-1, WidgetType.TEXT)
			.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP)
			.setSize(w, h, WidgetSizeMode.ABSOLUTE, WidgetSizeMode.ABSOLUTE)
			.setTextShadowed(true)
			.setFontId(FontID.PLAIN_12)
			.setTextColor(0xff981f)
			.setText("Show Loot")
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setHasListener(true);
		buttonTextWidget.revalidate();

		// on mouse hover and leave, update the sprite ids to simulate depth
		// also add a tooltip for the fake action
		buttonTextWidget.setOnMouseOverListener((JavaScriptCallback) (ignored) ->
		{
			for (int i = 0; buttonGraphicWidgets != null && i < buttonGraphicWidgets.length; i++)
			{
				buttonGraphicWidgets[i].setSpriteId(SPRITE_IDS_HOVER[i]);
			}
		});
		buttonTextWidget.setOnMouseLeaveListener((JavaScriptCallback) (ignored) ->
		{
			for (int i = 0; buttonGraphicWidgets != null && i < buttonGraphicWidgets.length; i++)
			{
				buttonGraphicWidgets[i].setSpriteId(SPRITE_IDS_STANDARD[i]);
			}
		});

		buttonTextWidget.setAction(0, "Show");
		buttonTextWidget.setOnOpListener((JavaScriptCallback) (ignored) ->
		{
			// queue the action for the next game tick just to simulate jagex widget delay ;)
			client.playSoundEffect(SoundEffectID.UI_BOOP);
			showLootQueued = true;
		});
	}

	private void unhide()
	{
		Widget items = client.getWidget(WIDGET_LOOT_ITEMS);
		if (items != null)
		{
			items.setHidden(false);
		}

		Widget gp = client.getWidget(WIDGET_LOOT_GP);
		if (gp != null)
		{
			gp.setHidden(false);
		}

		Widget buttonParent = client.getWidget(WIDGET_BUTTON_PARENT);
		if (buttonParent == null || buttonGraphicWidgets == null)
		{
			return;
		}

		// clear out all the children we added
		// I tried actually removing them from the array but setChildren failed on a classcast due to separate classloaders?
		for (Widget graphic : buttonGraphicWidgets)
		{
			graphic.setHidden(true);
		}
		buttonTextWidget.setHidden(true);
		buttonTextWidget.setHasListener(false);
		buttonGraphicWidgets = null;
		buttonTextWidget = null;
		showLootQueued = false;
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (showLootQueued)
		{
			unhide();
		}
	}

	private Widget buildGraphicWidget(Widget parent, int x, int y, int w, int h, int spriteId)
	{
		Widget graphic = parent.createChild(WidgetType.GRAPHIC)
			.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP)
			.setSize(w, h, WidgetSizeMode.ABSOLUTE, WidgetSizeMode.ABSOLUTE)
			.setSpriteId(spriteId);
		graphic.revalidate();
		return graphic;
	}
}
