package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateChanged;
import com.duckblade.osrs.fortis.util.TimerMode;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class SplitsFileWriter implements PluginLifecycleComponent
{

	public enum WriteCondition
	{
		NEVER,
		EVERY_RUN,
		SUCESSFUL_RUNS,
	}

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	private final EventBus eventBus;

	private final FortisColosseumConfig config;
	private final SplitsTracker splitsTracker;
	private final ChatMessageManager chatMessageManager;

	@VisibleForTesting
	ExecutorService es;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.splitsFileCondition() != WriteCondition.NEVER;
	}

	@Override
	public void startUp()
	{
		es = Executors.newSingleThreadExecutor(r ->
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("FortisColosseumSplitsFileWriter");
			return t;
		});
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		es.shutdown();
	}

	@Subscribe
	public void onColosseumStateChanged(ColosseumStateChanged e)
	{
		if (!e.getNewState().isInColosseum() && e.getPreviousState().isInColosseum())
		{
			List<Split> splits = splitsTracker.getSplits();
			if (splits.isEmpty() || (splits.size() < 12 && config.splitsFileCondition() == WriteCondition.SUCESSFUL_RUNS))
			{
				return;
			}

			queueWrite(splits);
		}
	}

	@VisibleForTesting
	void queueWrite(List<Split> splitsIn)
	{
		log.debug("Queuing write of {} splits", splitsIn.size());
		final List<Split> splits = new ArrayList<>(splitsIn);
		final String fileName = DATE_FORMATTER.format(new Date()) + ".txt";
		es.submit(() ->
		{
			TimerMode timerMode = config.splitsFileTimerMode();

			File dir = new File(RuneLite.RUNELITE_DIR, "fortis-colosseum/splits");
			if (!dir.mkdirs() && !dir.exists())
			{
				chatMessageManager.queue(QueuedMessage.builder()
					.value("Could not write splits: failed to create directory")
					.build());
				return;
			}

			File destFile = new File(dir, fileName);
			try (PrintWriter out = new PrintWriter(new FileWriter(destFile)))
			{
				for (Split split : splits)
				{
					out.print("Wave ");
					out.print(split.getWave());
					out.print(": ");
					out.print(timerMode.format(split.getWaveDuration()));
					out.print(" / ");
					out.print(timerMode.format(split.getCumulativeWaveDuration()));
					out.print(" / ");
					out.println(timerMode.format(split.getCumulativeDuration()));
				}
			}
			catch (Exception e)
			{
				log.warn("Failed to write fortis colosseum splits to [{}]", destFile.getAbsoluteFile(), e);
				chatMessageManager.queue(QueuedMessage.builder()
					.value("Could not write splits: an unexpected error occurred")
					.build());
			}
		});
	}
}
