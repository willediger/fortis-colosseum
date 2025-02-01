package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.util.TimerMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.runelite.client.eventbus.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SplitsFileWriterTest
{

	@InjectMocks
	SplitsFileWriter fileWriter;

	@Mock
	private FortisColosseumConfig config;

	@Mock
	private EventBus eventBus;

	@BeforeEach
	void setUp()
	{
		fileWriter.startUp();
	}

	@AfterEach
	void shutDown()
	{
		fileWriter.shutDown();
	}

	@Test
	@Disabled("writes to userhome, no verification mechanism")
	void testPrecise() throws InterruptedException
	{
		when(config.splitsFileTimerMode()).thenReturn(TimerMode.PRECISE);
		fileWriter.queueWrite(List.of(
			new Split(1, 10, 10, 10),
			new Split(2, 15, 40, 25),
			new Split(3, 30, 100, 55)
		));
		fileWriter.es.awaitTermination(1, TimeUnit.SECONDS);
	}

	@Test
	@Disabled("writes to userhome, no verification mechanism")
	void testLax() throws InterruptedException
	{
		when(config.splitsFileTimerMode()).thenReturn(TimerMode.LAX);
		fileWriter.queueWrite(List.of(
			new Split(1, 10, 10, 10),
			new Split(2, 15, 40, 25),
			new Split(3, 30, 100, 55)
		));
		fileWriter.es.awaitTermination(1, TimeUnit.SECONDS);
	}

	@Test
	@Disabled("writes to userhome, no verification mechanism")
	void testTicks() throws InterruptedException
	{
		when(config.splitsFileTimerMode()).thenReturn(TimerMode.TICKS);
		fileWriter.queueWrite(List.of(
			new Split(1, 10, 10, 10),
			new Split(2, 15, 40, 25),
			new Split(3, 30, 100, 55)
		));
		fileWriter.es.awaitTermination(1, TimeUnit.SECONDS);
	}

}
