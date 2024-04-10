package com.duckblade.osrs.fortis.debugplugins;

import com.duckblade.osrs.fortis.util.DebugColosseumStateTrackerInterface;
import com.duckblade.osrs.fortis.util.Modifier;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.devtools.DevToolsFrame;

@Singleton
@Slf4j
public class FortisColosseumDebugPanel extends DevToolsFrame
{

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;

	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;
	private final DebugColosseumStateTrackerInterface stateTrackerInterface;

	private final Map<Modifier, Integer> modifiers = new HashMap<>();
	private final List<Modifier> options = new ArrayList<>(Arrays.asList(Modifier.BEES, Modifier.BEES, Modifier.BEES));

	private boolean mockingModifiers = false;
	private boolean mockingOptions = false;

	@Inject
	public FortisColosseumDebugPanel(
		Client client,
		ClientThread clientThread,
		SpriteManager spriteManager,
		EventBus eventBus,
		DebugColosseumStateTrackerInterface stateTrackerInterface
	)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.stateTrackerInterface = stateTrackerInterface;
		eventBus.register(this);

		setTitle("Fortis Colosseum");
		setAlwaysOnTop(true);
		setPreferredSize(new Dimension(800, 320));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(Box.createVerticalStrut(5));

		JPanel mockModifiers = new JPanel();
		mockModifiers.setLayout(new BoxLayout(mockModifiers, BoxLayout.Y_AXIS));

		JCheckBox mockModifiersActive = new JCheckBox("Override modifiers");
		mockModifiersActive.addActionListener((_e) ->
		{
			mockingModifiers = !mockingModifiers;
			mockModifiers();
		});
		mockModifiers.add(mockModifiersActive);

		JPanel modifierToggles = new JPanel(new GridLayout(2, 7));
		for (Modifier m : Modifier.values())
		{
			modifierToggles.add(new ModifierToggle(spriteManager, m, this::modifierToggleCallback));
		}
		mockModifiers.add(modifierToggles);
		add(mockModifiers);
		add(Box.createVerticalStrut(10));

		JPanel mockOptions = new JPanel();
		mockOptions.setLayout(new BoxLayout(mockOptions, BoxLayout.Y_AXIS));
		JCheckBox mockOptionsActive = new JCheckBox("Override options");
		mockOptionsActive.addActionListener((_e) -> mockingOptions = !mockingOptions);
		mockOptions.add(mockOptionsActive);

		JPanel optionCombos = new JPanel(new GridLayout(1, 3));
		optionCombos.setMaximumSize(new Dimension(1000, 50));
		for (int i = 0; i < 3; i++)
		{
			final int optionIx = i;
			JComboBox<Modifier> option = new JComboBox<>(Modifier.values());
			option.addActionListener((_e) -> options.set(optionIx, (Modifier) option.getSelectedItem()));
			optionCombos.add(option);
		}
		mockOptions.add(optionCombos);
		add(mockOptions);

		pack();
	}

	private void modifierToggleCallback(Modifier m, int level)
	{
		if (level == 0)
		{
			modifiers.remove(m);
		}
		else
		{
			modifiers.put(m, level);
		}
		mockModifiers();
	}

	private void mockModifiers()
	{
		if (mockingModifiers)
		{
			for (Map.Entry<Modifier, Integer> e : modifiers.entrySet())
			{
				Modifier m = e.getKey();
				int level = e.getValue();
				if (m.getLevelVarb() != -1)
				{
					clientThread.invokeLater(() -> client.setVarbit(m.getLevelVarb(), level));
				}
			}
			stateTrackerInterface.setModifiers(modifiers.keySet());
		}
	}

	@Override
	public void open()
	{
		eventBus.register(this);
		super.open();
	}

	@Override
	public void close()
	{
		eventBus.unregister(this);
		super.close();
	}

	@Subscribe(priority = 100)
	public void onScriptPreFired(ScriptPreFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT)
		{
			return;
		}

		Object[] args = e.getScriptEvent().getArguments();

		if (mockingModifiers)
		{
			stateTrackerInterface.setModifiers(Collections.emptyList());
			int bits = 0;
			for (Modifier m : modifiers.keySet())
			{
				bits |= (1 << m.getId());
			}

			args[8] = bits;
		}

		if (mockingOptions)
		{
			args[2] = options.get(0).getId();
			args[3] = options.get(1).getId();
			args[4] = options.get(2).getId();
		}
	}

	@Subscribe(priority = 100)
	public void onVarbitChanged(VarbitChanged e)
	{
		if (!mockingModifiers)
		{
			return;
		}

		for (Modifier m : modifiers.keySet())
		{
			if (e.getVarbitId() == m.getLevelVarb())
			{
				e.setValue(modifiers.get(m));
				return;
			}
		}
	}
}
