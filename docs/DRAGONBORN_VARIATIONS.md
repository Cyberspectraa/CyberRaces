# Dragonborn appearance and breath choices

The character creator offers five elements:

| Type | V-key spell from Iron's Spells 'n Spellbooks |
|---|---|
| Fire | Fire Breath |
| Frost | Cone of Cold |
| Lightning | A 16-tick electrical breath stream; Ice and Fire CE dragon-lightning sound and damage when installed (vanilla fallback) |
| Poison | Poison Breath |
| Ender | Dragon's Breath |

Fire, Frost, Poison and Ender use Iron's Spells 'n Spellbooks. Lightning instead fires a custom short sustained breath stream with Ice and Fire CE damage and sound when installed; it never destroys blocks. Without CE, Lightning uses vanilla damage and particles.
The standard independent CyberRaces breath cooldown still applies.
The breath element belongs to the **base Dragonborn**, and stays unchanged
after evolving into Winged Dragonborn. Evolution adds wings and stat bonuses,
rather than replacing the chosen breath.

The creator stores the dragon element and horn shape together in the
existing CharacterAppearance.FeatureStyle field: `element * 3 + hornShape`.
Existing Dragonborn characters with styles 0–2 remain Fire Dragonborn.
Elements: 0 Fire, 1 Frost, 2 Lightning, 3 Poison, 4 Ender.
Horn shapes: 0 Horned, 1 Crowned, 2 Swept.

The horn/tail colour control cycles the 16 real dye colours of Icarus
dragon wings. Winged Dragonborn automatically equip matching Icarus
dragon wings, when Icarus and Curios are installed and a Curios back
slot is available. Existing non-palette colours are snapped to the
closest matching wing colour.

New Wild NPC Dragonborn can spawn with any of these element/crest and
wing-dye colour combinations. Their Wild-NPC AI uses matching particle
and damage effects for its short-range racial attack. Wild NPCs do not
evolve into Winged Dragonborn.

Evolved appearance:
- High Elf: higher ears, pale-gold circlet and light-coloured ear accents.
- Dark Elf: darker skin tint, long violet ears and dark/violet circlet.
- Elder Dwarf: thick grey beard with braided side pieces.
- Hobgoblin: long green ears and prominent tusks.
- Winged Dragonborn: matching Icarus dragon wings; chosen horns, tail,
  colour and breath stay the same.

The awakening screen is a Minecraft-styled rune ritual with a wake-up
particle and sound cue. This is a cosmetic scene, not an activity
point system; evolution still only needs Cyber Level 30 and sleeping.
