package com.duckblade.osrs.fortis.util;

import com.duckblade.osrs.fortis.util.spawns.WaveSpawns;
import java.util.Map;
import lombok.Getter;
import lombok.Value;

@Value
public class ColosseumState
{

	boolean inLobby;
	boolean inColosseum;
	int waveNumber;
	boolean waveStarted;
	Map<Handicap, Integer> handicaps;

	@Getter(lazy = true)
	WaveSpawns waveSpawns = WaveSpawns.forWave(this, false);

	@Getter(lazy = true)
	WaveSpawns nextWaveSpawns = WaveSpawns.forWave(this, true);

}
