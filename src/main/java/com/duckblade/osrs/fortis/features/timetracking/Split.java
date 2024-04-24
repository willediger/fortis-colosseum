package com.duckblade.osrs.fortis.features.timetracking;

import lombok.Value;

@Value
public class Split
{

	int wave;
	int waveDuration;
	int cumulativeDuration;
	int cumulativeWaveDuration;

}
