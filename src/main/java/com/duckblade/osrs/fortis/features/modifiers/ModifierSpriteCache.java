package com.duckblade.osrs.fortis.features.modifiers;

import com.duckblade.osrs.fortis.util.Modifier;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ModifierSpriteCache
{

	private static final int SPRITE_ID_BG = 5531;

	@Value
	private static class SpriteCacheKey
	{
		Modifier modifier;
		int level;
		ModifierSpriteStyle style;
	}

	private final SpriteManager spriteManager;
	private final Client client;

	private final Map<SpriteCacheKey, BufferedImage> modifierSpriteCache = new HashMap<>();

	public BufferedImage getSprite(Modifier modifier, ModifierSpriteStyle style)
	{
		int level = Math.max(1, modifier.getLevel(client));
		SpriteCacheKey key = new SpriteCacheKey(modifier, level, style);
		BufferedImage sprite;
		if ((sprite = modifierSpriteCache.get(key)) != null)
		{
			return sprite;
		}

		log.debug("sprite cache miss");
		sprite = getSpriteImpl(modifier, level, style);
		modifierSpriteCache.put(key, sprite);
		return sprite;
	}

	private BufferedImage getSpriteImpl(Modifier modifier, int level, ModifierSpriteStyle style)
	{
		BufferedImage modifierSprite = spriteManager.getSprite(modifier.getSpriteId(level), 0);
		if (modifierSprite == null)
		{
			return null;
		}

		if (style == ModifierSpriteStyle.COMPACT || style == ModifierSpriteStyle.ICON_ONLY)
		{
			BufferedImage ret = ImageUtil.resizeCanvas(modifierSprite, 38, 38);
			if (style == ModifierSpriteStyle.ICON_ONLY)
			{
				return ret;
			}

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
