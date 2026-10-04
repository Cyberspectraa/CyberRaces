# CyberRaces initial balance sheet

These values are intentionally conservative and are expected to change during playtesting.

| Race | Scale | HP | Move | Hunger | Max mana | Mana regen | Spell resist | KB resist | Natural armour | Fire damage taken | Flight |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Human | 1.00x | 20 | 100% | 100% | 100% | 100% | 100% | +0% | 0 | 100% | None |
| Elf | 1.03x | 18 | 105% | 100% | 115% | 110% | 100% | +0% | 0 | 100% | None |
| Dwarf | 0.82x | 22 | 95% | 100% | 90% | 100% | 110% | +20% | +1 | 100% | None |
| Halfling | 0.72x | 18 | 103% | 95% | 100% | 100% | 100% | +0% | 0 | 100% | None |
| Orc | 1.10x | 24 | 100% | 115% | 85% | 90% | 100% | +15% | 0 | 100% | None |
| Goblin | 0.75x | 18 | 108% | 100% | 100% | 105% | 100% | none | 0 | 100% | None |
| Tiefling | 1.00x | 20 | 100% | 100% | 115% | 105% | 100% | +0% | 0 | 50% | None |
| Dragonborn | 1.07x | 22 | 98% | 110% | 105% | 100% | 105% | +10% | +2 | 85% | None |
| Fairy | 0.60x | 14 | 110% | 90% | 130% | 120% | 95% | none | 0 | 100% | Icarus natural flight |

## Implemented in alpha.1

- Persistent race assignment.
- Pehkui base scaling.
- Max health, movement speed, knockback resistance and natural armour modifiers.
- Optional Iron's max mana, mana regeneration and spell resistance modifiers.
- Fire damage multipliers (currently used by Tiefling and the temporary Dragonborn baseline).
- One-time self-selection command plus admin set/clear commands.
- Icarus detection and a race flight-policy field.

## Designed but not implemented yet

- Character-creation GUI on first join.
- Icarus-backed Fairy wing/flight grant.
- Hunger-rate modifier.
- School-specific Iron's spell affinities.
- Dragonborn ancestry and breath ability.
- Halfling luck.
- Goblin darkvision/scavenging.
- Dwarf mining trait.
- Elf senses/fall trait.
- Orc low-health resilience.
- Human background choice.
- Ears templates/documentation.
