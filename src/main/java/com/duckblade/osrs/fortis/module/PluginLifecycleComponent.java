package com.duckblade.osrs.fortis.module;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.util.ColosseumState;

public interface PluginLifecycleComponent
{

	default boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return true;
	}

	void startUp();

	void shutDown();

}
