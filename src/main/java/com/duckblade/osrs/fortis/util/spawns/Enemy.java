package com.duckblade.osrs.fortis.util.spawns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Enemy
{

	FREMENNIK("Fremennik", "Fremmy"),
	SERPENT_SHAMAN("Serpent Shaman", "Mage"),
	JAGUAR_WARRIOR("Jaguar Warrior", "Melee"),
	JAVELIN_COLOSSUS("Javelin Colossus", "Ranger"),
	MANTICORE("Manticore", "Lion"),
	SHOCKWAVE_COLOSSUS("Shockwave Colossus", "Shocker"),
	MINOTAUR("Minotaur", "Minotaur"),
	SOL_HEREDIT("Sol Heredit", "Sol Heredit"),

	DOOM_SCORPION("Doom Scorpion", "Doom Scorpion"),
	ANGRY_BEES("Angry Bees", "BEES!!"),
	;

	private final String npcName;
	private final String colloquialName;

}
