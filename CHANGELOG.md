# Changelog

## 0.2.19-alpha.23

### Wild CyberNpc race integration
- Added optional CyberNpc compatibility without making CyberNpc a required dependency.
- Only `cybernpc:cyber_npc` entities whose NPC type is **Wild** receive races.
- Main, Quest, Banker, Courier, Guard, Pope and other non-Wild CyberNpc roles remain unchanged.
- Wild NPCs receive one persistent random race from the full CyberRaces roster: Human, Elf, Dwarf, Halfling, Orc, Goblin, Tiefling, Dragonborn, Fairy, Catfolk, Dogfolk and Foxfolk.
- Race choice and cosmetic variation are saved on the NPC and survive world reloads.
- Race health, movement speed, knockback resistance, armour, Pehkui scale and optional Iron's Spells attributes now support non-player living entities.
- CyberRaces synchronizes Wild NPC race visuals only to clients tracking that NPC.
- CyberRaces race cosmetic layers now support player-shaped living-entity renderers and automatically attach to CyberNpc's renderer when present.
- Fairy Wild NPCs render CyberRaces/Icarus Zanza wings while retaining normal Wild NPC ground AI for now.
- Beastfolk Wild NPCs use the current Catfolk, Dogfolk and Foxfolk variant ears/tails from CyberRaces.
- Fire-damage racial modifiers now apply to raced Wild NPCs as well as players.
- Integration remains event-driven; no new all-NPC continuous scan was added.

## 0.1.0-alpha.1

Initial CyberRaces foundation:

- Forge 1.20.1 project.
- Nine launch races: Human, Elf, Dwarf, Halfling, Orc, Goblin, Tiefling, Dragonborn and Fairy.
- Persistent race choice.
- Pehkui-based physical scaling.
- Vanilla racial health, movement, knockback resistance and natural armour.
- Optional Iron's Spells max mana, mana regeneration and spell resistance integration.
- Fire-damage racial modifiers.
- Icarus detection and flight-policy groundwork.
- Player one-time race choice and admin testing commands.
