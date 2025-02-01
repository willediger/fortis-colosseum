package com.duckblade.osrs.fortis.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@Getter
@RequiredArgsConstructor
public enum TimerMode
{

	PRECISE("--:--.--"),
	LAX("--:--"),
	TICKS("--"),
	;

	private final String nullPattern;

	private static final int VARBIT_PRECISE_TIMING = 11866;

	public String format(int ticks)
	{
		if (ticks < 0)
		{
			return this.nullPattern;
		}

		int mins = (ticks / 100);
		float secondsReal = (ticks % 100) * 0.6f;
		int seconds = (int) secondsReal;
		int tenths = Math.round((secondsReal - seconds) * 10f);

		switch (this)
		{
			case PRECISE:
				return String.format("%d:%02d.%d0", mins, seconds, tenths);

			case LAX:
				return String.format("%d:%02d", mins, seconds);

			case TICKS:
				return String.valueOf(ticks);

			default:
				throw new IllegalStateException("Unimplemented timer mode format " + this.name());
		}
	}

	public static TimerMode fromClient(Client c)
	{
		assert c.isClientThread();
		return c.getVarbitValue(VARBIT_PRECISE_TIMING) == 1 ? PRECISE : LAX;
	}

}
