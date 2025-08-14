package com.duckblade.osrs.fortis.features.loslinks.model;

import com.duckblade.osrs.fortis.util.spawns.Enemy;
import java.util.List;
import lombok.Value;
import net.runelite.api.Point;

@Value
public class WaveSpawnRecord
{

	int wave;
	Point playerTile;
	List<NpcSpawn> spawns;
	boolean waveSpawn;
	boolean mm3;

	public String toLoSUrl()
	{
		StringBuilder urlBuilder = new StringBuilder("https://los.colosim.com/?");

		for (NpcSpawn spawn : spawns)
		{
			urlBuilder.append(String.format(
				"%02d%02d%d",
				spawn.getSwTile().getX(),
				spawn.getSwTile().getY(),
				spawn.getEnemyType().getColosimLosId()
			));

			if (spawn.getEnemyType() == Enemy.MANTICORE)
			{
				urlBuilder.append(spawn.getOrbOrder().toLoSCode());
			}
			urlBuilder.append(".");
		}

		int playerEncoded = playerTile.getX() + (256 * playerTile.getY());
		urlBuilder.append("#").append(playerEncoded);

		if (waveSpawn)
		{
			urlBuilder.append("_ws");
		}

		if (mm3)
		{
			urlBuilder.append("_mm3");
		}

		return urlBuilder.toString();
	}

}
