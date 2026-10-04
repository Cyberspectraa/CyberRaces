# CyberRaces cosmetic direction

Race identity should be visible without requiring a special player skin.

## Rules

1. Core race features are rendered by CyberRaces and must not depend on Ears skin metadata.
2. Ears remains supported as an optional extra for players who want additional skin-driven details.
3. Cosmetic parts should attach to existing player model bones wherever possible so animation packs such as FA+Player remain compatible.
4. Cosmetic choices are visual only unless a race explicitly defines the feature mechanically.
5. Avoid replacing the player's normal skin unnecessarily. Full head/body replacement is reserved for races such as Dragonborn where a human skin cannot represent the race well.
6. Use a small set of selectable cosmetic variants rather than dozens of bespoke models.

## Race silhouettes

### Human
- No mandatory geometry.
- Keeps the normal Minecraft player silhouette.
- Optional future details: scars, freckles, cosmetic backgrounds.

### Elf
- Mandatory long pointed ears attached to the head.
- Several future ear shapes/lengths.
- Keep the player's normal skin visible.

### Dwarf
- Shorter body from Pehkui.
- Broader proportions should be explored with Pehkui width scaling.
- Beards should be optional rather than forced.

### Halfling
- Very short humanoid silhouette.
- Slightly larger/rounded ears are optional.
- Do not turn Halfling into a second Dwarf.

### Orc
- Mandatory lower tusks.
- Mandatory mildly pointed ears.
- Broader body proportions should be explored.
- Do not force green skin; the player's own skin remains valid.

### Goblin
- Mandatory long outward-pointing ears.
- Optional small nose extension later.
- Small body is already a major silhouette feature.

### Tiefling
- Mandatory horns.
- Mandatory tail.
- Horn style should eventually be selectable.
- Tail should attach to the body bone so player animations move it naturally.

### Dragonborn
- Needs the strongest custom appearance.
- Custom dragon-like head shell/snout over the player's head.
- Horns.
- Tail.
- Optional scale colour choice.
- The custom head can intentionally cover most of the human face because otherwise Dragonborn reads as a normal human with horns.

### Fairy
- Very small body.
- Permanent Zanza-style racial wings.
- Optional small pointed/fey ears.
- Avoid excessive particles by default.

## Customisation screen direction

Race selection should eventually include a second cosmetic step.

Example:

1. Choose race.
2. Pick race cosmetic variants.
3. Pick permitted cosmetic colours.
4. Preview the full player model.
5. Confirm permanently with the race choice.

The first cosmetic version should remain small:

- Elf: 2-3 ear shapes.
- Orc: 2 tusk shapes.
- Goblin: 2-3 ear shapes.
- Tiefling: 3 horn shapes + 2 tail tips.
- Dragonborn: 3 horn/head variants + scale colour.
- Fairy: Zanza wings fixed initially.

This is enough variety without turning CyberRaces into a full character creator.
