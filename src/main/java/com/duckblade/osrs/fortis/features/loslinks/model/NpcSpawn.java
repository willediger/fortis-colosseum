package com.duckblade.osrs.fortis.features.loslinks.model;

import com.duckblade.osrs.fortis.util.spawns.Enemy;
import lombok.Data;
import net.runelite.api.Point;

@Data
public class NpcSpawn
{
	private final int npcIndex;
	private final Point swTile; // in colosim coords
	private final Enemy enemyType;
	private final ManticoreOrbOrder orbOrder;
}
