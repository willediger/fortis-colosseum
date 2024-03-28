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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import joptsimple.internal.Strings;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ImageUtil;

@Singleton
public class ModifierOverlay extends OverlayPanel implements PluginLifecycleComponent
{

	public enum Style
	{
		FANCY,
		COMPACT,
	}

	private static final int SPRITE_ID_BG = 5531;
	private static final int SPRITE_PADDING = 4;

	private final OverlayManager overlayManager;
	private final EventBus eventBus;
	private final Client client;
	private final SpriteManager spriteManager;
	private final TooltipManager tooltipManager;
	private final FortisColosseumConfig config;
	private final ColosseumStateTracker stateTracker;

	@Inject
	public ModifierOverlay(
		OverlayManager overlayManager,
		EventBus eventBus,
		Client client,
		SpriteManager spriteManager,
		TooltipManager tooltipManager,
		FortisColosseumConfig config,
		ColosseumStateTracker stateTracker
	)
	{

		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		this.client = client;
		this.spriteManager = spriteManager;
		this.tooltipManager = tooltipManager;
		this.config = config;
		this.stateTracker = stateTracker;

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
			Style newVal = config.modifiersOverlayStyle() == Style.COMPACT
				? Style.FANCY
				: Style.COMPACT;
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
		setPreferredColor(new Color(0, 0, 0, 0)); // no background

		ComponentOrientation orientation = config.modifiersOverlayOrientation();
		getPanelComponent().setOrientation(orientation);
		getPanelComponent().setGap(new Point(SPRITE_PADDING, SPRITE_PADDING));

		int x = 4;
		int y = 4;
		List<Modifier> mods = stateTracker.getCurrentState().getModifiers();
		for (Modifier modifier : mods)
		{
			if (orientation == ComponentOrientation.HORIZONTAL)
			{
				x += renderModifier(modifier, x, y) + SPRITE_PADDING;
			}
			else
			{
				y += renderModifier(modifier, x, y) + SPRITE_PADDING;
			}
		}

		return super.render(graphics);
	}

	private int renderModifier(Modifier modifier, int x, int y)
	{
		Rectangle bounds = getBounds();
		net.runelite.api.Point mouse = client.getMouseCanvasPosition();
		int mouseX = mouse.getX() - bounds.x;
		int mouseY = mouse.getY() - bounds.y;

		BufferedImage sprite = getSprite(modifier);
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

	private BufferedImage getSprite(Modifier modifier)
	{
		BufferedImage modifierSprite = spriteManager.getSprite(modifier.getSpriteId(client), 0);
		if (modifierSprite == null)
		{
			return null;
		}

		int level = Math.max(1, modifier.getLevel(client));
		if (config.modifiersOverlayStyle() == Style.COMPACT)
		{
			BufferedImage ret = ImageUtil.resizeCanvas(modifierSprite, 38, 38);
			if (modifier.getLevelVarb() != -1)
			{
				Graphics2D g = ret.createGraphics();
				g.setFont(FontManager.getRunescapeSmallFont());
				String label = Strings.repeat('I', level);
				Rectangle2D labelSize = g.getFontMetrics().getStringBounds(label, g);
				g.setColor(Color.black);
				g.drawString(label, ret.getWidth() - ((int) labelSize.getWidth()), ((int) labelSize.getHeight()));
				g.setColor(Color.white);
				g.drawString(label, ret.getWidth() - ((int) labelSize.getWidth()) - 1, (int) labelSize.getHeight() - 1);
			}
			return ret;
		}

		BufferedImage bgSprite = spriteManager.getSprite(SPRITE_ID_BG + level - 1, 0);
		if (bgSprite == null)
		{
			return null;
		}

		BufferedImage ret = new BufferedImage(bgSprite.getWidth(), bgSprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = ret.createGraphics();
		g.drawImage(bgSprite, 0, 0, null);
		g.drawImage(
			modifierSprite,
			bgSprite.getWidth() / 2 - modifierSprite.getWidth() / 2,
			bgSprite.getHeight() / 2 - modifierSprite.getHeight() / 2,
			null
		);
		return ret;
	}
}
