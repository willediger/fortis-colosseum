package com.duckblade.osrs.fortis.util;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DebugColosseumStateTrackerInterface
{

	private final ColosseumStateTracker stateTracker;

	public void setModifiers(Collection<Modifier> modifiers)
	{
		stateTracker.getModifiers().clear();
		stateTracker.getModifiers().addAll(modifiers);
	}

}
