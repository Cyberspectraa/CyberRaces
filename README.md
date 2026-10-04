# CyberRaces

CyberRaces is the race/ancestry system for the Season 2 Forge 1.20.1 modpack.

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
- **Icarus** — optional racial-flight integration target, initially for Fairy.
- **Ears** — optional player-skin cosmetics; CyberRaces never relies on an Ears skin to determine gameplay race.

CyberRaces deliberately keeps race separate from class. An Orc Mage, Fairy Berserker, Dwarf Cleric, etc. remain valid combinations.

## Current alpha

The first alpha provides the backend and a command-driven testing path before the final character-creation GUI is added.

### Player commands

- `/cyberraces race list`
- `/cyberraces race info <race>`
- `/cyberraces race choose <race>`
- `/cyberraces race get`
- `/cyberraces race compat`

A player may use `choose` only once. Normal race swapping is intentionally disabled.

### Admin/testing commands

- `/cyberraces race get <player>`
- `/cyberraces race set <player> <race>`
- `/cyberraces race clear <player>`

These are intended for balancing and recovery while the system is in development.

## First test checklist

1. Join with Pehkui installed and confirm CyberRaces asks for a race.
2. Run `/cyberraces race choose dwarf` and verify the player visibly shrinks.
3. Confirm health, movement speed and knockback resistance reflect the race.
4. Rejoin the world and confirm the chosen race and scale persist.
5. With Iron's Spells installed, compare max mana/mana regen between Elf, Orc and Fairy using the admin set command.
6. Stand in fire as a Tiefling and confirm fire damage is reduced.
7. Run `/cyberraces race compat` to verify optional mods are detected.

## Not implemented yet

- First-join race selection GUI.
- Icarus-backed Fairy flight/wings.
- Ears race skin templates.
- Hunger-rate modifiers.
- School-specific Iron's affinities.
- Active racial abilities such as Dragonborn breath.
- The rest of each race's unique passive traits.

See `docs/RACE_BALANCE.md` for the current numbers and implementation status.
