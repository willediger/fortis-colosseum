package com.duckblade.osrs.fortis.features.waves;

import com.duckblade.osrs.fortis.util.spawns.Enemy;

public enum EnemyNameMode
{

	OFFICIAL,
	COLLOQUIAL,
	;

	public String nameOf(Enemy enemy)
	{
		return this == OFFICIAL ? enemy.getNpcName() : enemy.getColloquialName();
	}

}
