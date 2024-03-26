package com.duckblade.osrs.fortis.features.timetracking.livesplit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class LiveSplitWriter
{

	// es being a singleThreadExecutor is implicitly required for some things here,
	// otherwise we'd need a synchronization lock as well
	private ScheduledExecutorService es;

	private int targetPort = 0; // 0 denotes not connected
	private Socket activeSocket;
	private PrintWriter writer;
	private BufferedReader reader;

	public void startUp()
	{
		es = Executors.newSingleThreadScheduledExecutor(r ->
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("FortisColosseumLiveSplitWriter");
			return t;
		});
		es.scheduleWithFixedDelay(this::open, 0, 5, TimeUnit.SECONDS);
	}

	public void shutDown()
	{
		es.submit(this::close);
		es.shutdown();
	}

	public void setTargetPort(int port)
	{
		if (es == null || es.isShutdown())
		{
			targetPort = port;
			return;
		}

		// reconnect if the port changed
		es.submit(() ->
		{
			if (targetPort != port)
			{
				close();
				targetPort = port;
				open();
			}
		});
	}

	public void sendCommand(String command)
	{
		log.debug(">> {}", command);
		sendCommand(command, null);
	}

	public void sendCommand(String command, Consumer<String> callback)
	{
		es.submit(() ->
		{
			try
			{
				writer.print(command);
				writer.print("\r\n"); // livesplit always uses \r\n regardless of system line endings
				writer.flush();
				if (writer.checkError())
				{
					close();
				}
				else if (callback != null)
				{
					String response = reader.readLine();
					log.debug("<< {}", response);
					callback.accept(response);
				}
			}
			catch (IOException e)
			{
				log.warn("Unexpected error in sendCommand. Closing socket and reconnecting at next scheduled open.", e);
				close();
			}
		});
	}

	private void open()
	{
		// cancel early if no connection is desired, or there already is a connection
		if (targetPort == 0 || activeSocket != null)
		{
			log.trace("Not opening a connection to LiveSplit port={} socketNull={}", targetPort, activeSocket == null);
			return;
		}

		try
		{
			log.debug("Connecting to LiveSplit on port {}", targetPort);
			activeSocket = new Socket(InetAddress.getLoopbackAddress(), targetPort);
			writer = new PrintWriter(activeSocket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));

			log.info("Connected to LiveSplit on port {}", targetPort);
		}
		catch (IOException e)
		{
			log.debug("Failed to connect to LiveSplit on port {}, will retry later", this.targetPort);
			activeSocket = null;
			writer = null;
			reader = null;
		}
	}

	private void close()
	{
		if (activeSocket != null && !activeSocket.isClosed())
		{
			try
			{
				log.debug("Closing LiveSplit connection");
				activeSocket.close();
				writer.close();
				reader.close();
			}
			catch (IOException e)
			{
				// this shouldn't happen, but if it does there's nothing more we can do
				log.warn("encountered an unexpected error closing the socket, socket may be dangling!", e);
			}
			activeSocket = null;
			writer = null;
			reader = null;
		}
	}

}
