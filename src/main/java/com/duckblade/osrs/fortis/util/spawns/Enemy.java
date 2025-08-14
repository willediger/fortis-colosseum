package com.duckblade.osrs.fortis.util.spawns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Enemy
{

	FREMENNIK("Fremennik", "Fremmy", -1),
	SERPENT_SHAMAN("Serpent Shaman", "Mage", 1),
	JAGUAR_WARRIOR("Jaguar Warrior", "Melee", 3),
	JAVELIN_COLOSSUS("Javelin Colossus", "Ranger", 2),
	MANTICORE("Manticore", "Lion", 4),
	SHOCKWAVE_COLOSSUS("Shockwave Colossus", "Shocker", 6),
	MINOTAUR("Minotaur", "Minotaur", 5),
	SOL_HEREDIT("Sol Heredit", "Sol Heredit", -1),

	ANGRY_BEES("Angry Bees", "BEES!!", -1),
	;

	private final String npcName;
	private final String colloquialName;
	private final int colosimLosId;

}
