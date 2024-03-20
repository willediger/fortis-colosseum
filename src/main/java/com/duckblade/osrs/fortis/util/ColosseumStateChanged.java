package com.duckblade.osrs.fortis.util;

import lombok.Value;

@Value
public class ColosseumStateChanged
{

	private final ColosseumState previousState;
	private final ColosseumState newState;

}
