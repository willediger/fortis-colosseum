package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.util.TimerMode;

public enum SplitsOverlayMode
{

	OFF,
	WAVE_TIME,
	CUMULATIVE,
	BOTH,
	;

	public String formatSplit(TimerMode timerMode, Split split)
	{
		switch (this)
		{
			case WAVE_TIME:
				return timerMode.format(split.getWaveDuration());

			case CUMULATIVE:
				return timerMode.format(split.getCumulativeDuration());

			case BOTH:
				return timerMode.format(split.getWaveDuration()) + " / " + timerMode.format(split.getCumulativeDuration());

			default:
				return "";
		}
	}

}
