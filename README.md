# CyberRaces

CyberRaces is the race and character-creation system for the Season 2 Forge 1.20.1 modpack.

Initial race roster:

- Human
- Elf
- Dwarf
- Halfling
- Orc
- Goblin
- Tiefling
- Dragonborn
- Fairy

## Integrations

- **Pehkui** — physical race scale.
- **Iron's Spells 'n Spellbooks** — optional race-specific mana, regeneration and spell resistance.
- **Curios + Icarus** — Fairy's permanent Zanza's Wings and flight.
- **CyberServer** — soft first-join handshake so character creation finishes before the one-time church arrival sequence.

CyberRaces deliberately keeps race separate from class. An Orc Mage, Fairy Berserker, Dwarf Cleric, etc. remain valid combinations.

## Character creation

New players are placed into a temporary hidden/frozen creation hold instead of visibly entering the world.

The creator currently stores:

- Race
- Eye style
- Eye colour
- Race-feature variant

Once confirmed, CyberRaces marks the character as ready. CyberServer can then perform the player's actual one-time arrival/summoning sequence.

Existing players from older CyberRaces builds who already have a race are migrated as completed characters automatically.

## Asset policy

CyberRaces uses Minecraft 1.20.1's own built-in resources where possible instead of copying them into the mod. The reference repository used while locating vanilla resources is:

https://github.com/InventivetalentDev/minecraft-assets/tree/1.20.1/assets

Race-specific models/textures belong to CyberRaces itself.

## Admin/testing commands

- `/cyberraces race get <player>`
- `/cyberraces race set <player> <race>`
- `/cyberraces race clear <player>`

See `docs/RACE_BALANCE.md` and `docs/RACE_COSMETICS.md`.
