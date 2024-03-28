package com.duckblade.osrs.fortis.util;

import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@RequiredArgsConstructor
public enum Handicap
{

	BEES("Bees!", 5544, 2, 9791),
	BLASPHEMY("Blasphemy", 5538, 4, 9790),
	DOOM("Doom", 5543, 8, -1),
	DOOM_SCORPION("The Doom Scorpion", 5539, 0, 9789),
	DYNAMIC_DUO("Dynamic Duo", 5545, 9, -1),
	FRAILTY("Frailty", 5541, 12, 9796),
	MYOPIA("Myopia", 5547, 11, 9795),
	REENTRY("Reentry", 5536, 1, 9792),
	RED_FLAG("Red Flag", 5540, 13, -1),
	RELENTLESS("Relentless", 5535, 5, 9798),
	SOLARFLARE("Solarflare", 5537, 10, 9797),
	QUARTET("Quartet", 5546, 6, -1),
	TOTEMIC("Totemic", 5542, 7, -1),
	VOLATILITY("Volatility", 5534, 3, 9799),
	;

	private final String name;
	private final int spriteId;
	private final int id;
	private final int levelVarb;

	public int getLevel(Client client)
	{
		if (levelVarb == -1)
		{
			return 1;
		}

		return client.getVarbitValue(levelVarb);
	}

	public String getName(int level)
	{
		if (levelVarb == -1)
		{
			return name;
		}

		return level == 3 ? name + " (III)"
			: level == 2 ? name + " (II)"
			: name;
	}

	public String getName(Client client)
	{
		return getName(getLevel(client));
	}

	public static Handicap forId(int id)
	{
		for (Handicap h : values())
		{
			if (h.id == id)
			{
				return h;
			}
		}

		return null;
	}

	public static Set<Handicap> forBitmask(int bits)
	{
		Set<Handicap> ret = EnumSet.noneOf(Handicap.class);
		for (Handicap h : values())
		{
			if ((bits & (1 << h.id)) != 0)
			{
				ret.add(h);
			}
		}

		return ret;
	}

}
