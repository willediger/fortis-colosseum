package com.duckblade.osrs.fortis.features.modifiers;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import com.duckblade.osrs.fortis.util.Modifier;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

@Singleton
@Slf4j
public class ModifierOverlay extends OverlayPanel implements PluginLifecycleComponent
{

	private static final int SPRITE_PADDING = 4;

	private final OverlayManager overlayManager;
	private final EventBus eventBus;
	private final Client client;
	private final TooltipManager tooltipManager;
	private final FortisColosseumConfig config;
	private final ColosseumStateTracker stateTracker;
	private final ModifierSpriteCache spriteCache;

	@Inject
	public ModifierOverlay(
		OverlayManager overlayManager,
		EventBus eventBus,
		Client client,
		TooltipManager tooltipManager,
		FortisColosseumConfig config,
		ColosseumStateTracker stateTracker,
		ModifierSpriteCache spriteCache
	)
	{
		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		this.client = client;
		this.tooltipManager = tooltipManager;
		this.config = config;
		this.stateTracker = stateTracker;
		this.spriteCache = spriteCache;

		setPosition(OverlayPosition.BOTTOM_LEFT);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Flip", "Modifier Overlay", (me) ->
		{
			ComponentOrientation newVal = config.modifiersOverlayOrientation() == ComponentOrientation.HORIZONTAL
				? ComponentOrientation.VERTICAL
				: ComponentOrientation.HORIZONTAL;
			this.config.setModifiersOverlayOrientation(newVal);
		});
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Change Style", "Modifier Overlay", (me) ->
		{
			ModifierSpriteStyle newVal = config.modifiersOverlayStyle() == ModifierSpriteStyle.COMPACT
				? ModifierSpriteStyle.FANCY
				: ModifierSpriteStyle.COMPACT;
			this.config.setModifiersOverlayStyle(newVal);
		});
	}

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.modifiersOverlayEnabled() && colosseumState.isInColosseum();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long start = System.currentTimeMillis();
		setPreferredColor(new Color(0, 0, 0, 0)); // no background

		ComponentOrientation orientation = config.modifiersOverlayOrientation();
		getPanelComponent().setOrientation(orientation);
		getPanelComponent().setGap(new Point(SPRITE_PADDING, SPRITE_PADDING));

		int x = 4;
		int y = 4;
		List<Modifier> mods = stateTracker.getCurrentState().getModifiers();
		ModifierSpriteStyle style = config.modifiersOverlayStyle();
		for (Modifier modifier : mods)
		{
			if (orientation == ComponentOrientation.HORIZONTAL)
			{
				x += renderModifier(modifier, style, x, y) + SPRITE_PADDING;
			}
			else
			{
				y += renderModifier(modifier, style, x, y) + SPRITE_PADDING;
			}
		}

		long ms = System.currentTimeMillis() - start;
		if (ms > 1)
		{
			log.debug("modifier overlay rendered in {}ms", ms);
		}
		return super.render(graphics);
	}

	private int renderModifier(Modifier modifier, ModifierSpriteStyle style, int x, int y)
	{
		Rectangle bounds = getBounds();
		net.runelite.api.Point mouse = client.getMouseCanvasPosition();
		int mouseX = mouse.getX() - bounds.x;
		int mouseY = mouse.getY() - bounds.y;

		BufferedImage sprite = spriteCache.getSprite(modifier, style);
		if (sprite == null)
		{
			return 0;
		}

		getPanelComponent().getChildren().add(new ImageComponent(sprite));

		Rectangle hoverArea = new Rectangle(x, y, sprite.getWidth(), sprite.getHeight());
		if (hoverArea.contains(mouseX, mouseY))
		{
			tooltipManager.add(new Tooltip(modifier.getName(client)));
		}

		return hoverArea.width;
	}
}
