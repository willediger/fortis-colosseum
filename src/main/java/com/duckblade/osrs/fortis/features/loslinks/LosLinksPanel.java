/*
 * Copyright (c) 2025, Will Ediger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.duckblade.osrs.fortis.features.loslinks;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.features.loslinks.model.WaveSpawnRecord;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateChanged;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

@Singleton
public class LosLinksPanel extends PluginPanel implements PluginLifecycleComponent
{
	private static final int COMPONENT_HEIGHT = 30;
	private static final int GAP = 5;
	private static final int WAVE_NUMBER_WIDTH = 42;
	private static final int SPAWN_BUTTON_WIDTH = 62;
	private static final int REINFORCEMENTS_BUTTON_WIDTH = 118;
	private static final Dimension FULL_WIDTH = new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT);
	private static final Color BG_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BTN_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color HOVER_COLOR = new Color(52, 52, 52);

	private final ClientToolbar clientToolbar;
	private final EventBus eventBus;

	private final JPanel wavesContainer = new JPanel();
	private final Map<Integer, WavePanel> wavePanels = new HashMap<>();

	private NavigationButton navButton;

	@Inject
	public LosLinksPanel(
		EventBus eventBus,
		ClientToolbar clientToolbar,
		ClientThread clientThread,
		ColosseumStateTracker stateTracker,
		LosLinks loSLinks
	)
	{
		super(false);
		this.clientToolbar = clientToolbar;
		this.eventBus = eventBus;

		setBackground(BG_COLOR);
		setLayout(new BorderLayout());

		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));

		JButton currentLoS = createButton("Current LoS", FULL_WIDTH);
		currentLoS.addActionListener(e -> clientThread.invokeLater(() ->
		{
			if (stateTracker.getCurrentState().isInColosseum())
			{
				String url = loSLinks.constructWaveRecord().toLoSUrl();
				SwingUtilities.invokeLater(() -> LinkBrowser.browse(url));
			}
		}));

		JLabel wavesLabel = new JLabel("Waves", SwingConstants.CENTER);
		setFixedSize(wavesLabel, FULL_WIDTH);

		header.add(currentLoS);
		header.add(Box.createRigidArea(new Dimension(0, GAP)));
		header.add(wavesLabel);

		add(header, BorderLayout.NORTH);

		wavesContainer.setLayout(new BoxLayout(wavesContainer, BoxLayout.Y_AXIS));
		wavesContainer.setBackground(BG_COLOR);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(BG_COLOR);
		wrapper.add(wavesContainer, BorderLayout.NORTH);
		add(wrapper, BorderLayout.CENTER);
	}

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.losLinksEnabled();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		navButton = NavigationButton.builder()
			.tooltip("Fortis Colosseum")
			.icon(ImageUtil.loadImageResource(getClass(), "colosseum_icon.png"))
			.priority(10)
			.panel(this)
			.build();
		clientToolbar.addNavigation(navButton);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onWaveSpawnRecord(WaveSpawnRecord record)
	{
		SwingUtilities.invokeLater(() ->
		{
			WavePanel wavePanel = wavePanels.computeIfAbsent(
				record.getWave(), (k) ->
				{
					WavePanel panel = new WavePanel(record);
					wavesContainer.add(panel);
					wavesContainer.add(Box.createRigidArea(new Dimension(0, GAP)));
					wavesContainer.revalidate();
					return panel;
				}
			);

			if (!record.isWaveSpawn())
			{
				wavePanel.setReinforcementRecord(record);
			}
		});
	}

	@Subscribe
	public void onColosseumStateChanged(ColosseumStateChanged e)
	{
		if (e.getNewState().isInColosseum() && !e.getPreviousState().isInColosseum())
		{
			SwingUtilities.invokeLater(() ->
			{
				wavePanels.clear();
				wavesContainer.removeAll();
				wavesContainer.revalidate();
			});
		}
	}

	private static final class WavePanel extends JPanel
	{
		private final JButton reinfButton;
		private WaveSpawnRecord reinforcementRecord;

		WavePanel(WaveSpawnRecord spawnRecord)
		{
			setOpaque(false);
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(0, GAP, 0, GAP));

			JPanel row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
			row.setOpaque(false);
			setFixedSize(row, FULL_WIDTH);

			JLabel numberLabel = new JLabel(String.valueOf(spawnRecord.getWave()), SwingConstants.CENTER);
			setFixedSize(numberLabel, new Dimension(LosLinksPanel.WAVE_NUMBER_WIDTH, LosLinksPanel.COMPONENT_HEIGHT));

			JButton spawnButton = createButton("Spawn", new Dimension(SPAWN_BUTTON_WIDTH, COMPONENT_HEIGHT));
			spawnButton.addActionListener(e -> LinkBrowser.browse(spawnRecord.toLoSUrl()));

			reinfButton = createButton("Reinforcements", new Dimension(REINFORCEMENTS_BUTTON_WIDTH, COMPONENT_HEIGHT));
			reinfButton.setEnabled(false);
			reinfButton.setVisible(false);
			reinfButton.addActionListener(e -> LinkBrowser.browse(reinforcementRecord.toLoSUrl()));

			row.add(numberLabel);
			row.add(Box.createRigidArea(new Dimension(GAP, 0)));
			row.add(spawnButton);
			row.add(Box.createRigidArea(new Dimension(GAP, 0)));
			row.add(reinfButton);

			add(row, BorderLayout.CENTER);
		}

		void setReinforcementRecord(WaveSpawnRecord reinforcementRecord)
		{
			this.reinforcementRecord = reinforcementRecord;
			reinfButton.setEnabled(true);
			reinfButton.setVisible(true);
		}
	}

	private static JButton createButton(String text, Dimension size)
	{
		JButton button = new JButton(text);
		button.setFocusPainted(false);
		button.setBackground(BTN_COLOR);
		button.setForeground(Color.WHITE);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setFixedSize(button, size);
		button.setBorder(BorderFactory.createEmptyBorder());

		button.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				button.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				button.setBackground(BTN_COLOR);
			}
		});

		return button;
	}

	private static void setFixedSize(JComponent component, Dimension size)
	{
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		component.setMinimumSize(size);
	}
}