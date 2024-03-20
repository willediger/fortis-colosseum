package com.duckblade.osrs.fortis.features.waves;

public enum WaveOverlayMode
{

	OFF,
	CURRENT,
	NEXT,
	BOTH,
	;

	public boolean showCurrent()
	{
		return this == CURRENT || this == BOTH;
	}

	public boolean showNext()
	{
		return this == NEXT || this == BOTH;
	}

}
