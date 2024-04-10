package com.duckblade.osrs.fortis.debugplugins;

import com.duckblade.osrs.fortis.util.Modifier;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

class ModifierToggle extends JButton
{
	private static final BufferedImage EMPTY = new BufferedImage(38, 38, BufferedImage.TYPE_INT_ARGB);

	private final Modifier modifier;
	private final SpriteManager spriteManager;
	private final BiConsumer<Modifier, Integer> callback;

	private final JLabel imageLabel;
	private final JLabel textLabel;

	private int level = 0;

	public ModifierToggle(SpriteManager spriteManager, Modifier modifier, BiConsumer<Modifier, Integer> callback)
	{
		super();
		this.spriteManager = spriteManager;
		this.modifier = modifier;
		this.callback = callback;

		setLayout(new BorderLayout());
		add(imageLabel = new JLabel(new ImageIcon(EMPTY)), BorderLayout.CENTER);
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		add(textLabel = new JLabel(modifier.name()), BorderLayout.SOUTH);
		textLabel.setHorizontalAlignment(JLabel.CENTER);

		addActionListener((_e) -> toggle());
	}

	private void toggle()
	{
		textLabel.setHorizontalAlignment(JLabel.CENTER);
		level++;
		level %= (modifier.getLevelVarb() == -1) ? 2 : 4;
		if (level == 0)
		{
			imageLabel.setIcon(new ImageIcon(EMPTY));
		}
		else
		{
			ImageIcon icon = new ImageIcon();
			imageLabel.setIcon(icon);
			spriteManager.getSpriteAsync(modifier.getSpriteId(level), 0, i ->
				icon.setImage(ImageUtil.resizeCanvas(i, 38, 38)));
		}
		textLabel.setName(modifier.getName(level));
		callback.accept(modifier, level);
	}
}
