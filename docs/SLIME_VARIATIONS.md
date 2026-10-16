# Slime appearances (Forge 1.20.1)

Slime varieties are **cosmetic character appearances**, not racial evolutions.
All choices use the same Slime health, bounce, fall immunity, hunger and fire-damage rules.

## Built-in character-creator choices

Always available: Slime, Magma Slime, Metal Slime, Acid Slime,
Crystal Slime, Frost Slime, Void Slime, Royal Slime.

When Tinkers' Construct (`tconstruct`) is installed, four more
appearance colours appear: Sky Slime, Earth Slime, Ender Slime and
Ichor Slime. These are inspired by their named slime types; they do
not copy Tinkers' model geometry or textures.

## Other modded slimes and resource packs

Additional variants can be registered in resource packs or mods by
providing JSON under:

`assets/<namespace>/cyberraces/slime_variants/<name>.json`

Example (`assets/examplemod/cyberraces/slime_variants/mystic.json`):

```json
{
  "slot": 12,
  "name": "Mystic Slime",
  "color": "#A57DDD",
  "requires_mod": "examplemod"
}
```

* `slot` must be an unused number from **12 to 63**. It is stored
  with the character and must remain stable across releases.
* `name` is the visible variant label.
* `color` is the RGB tint of the gelatinous skin.
* `requires_mod` is optional. If specified, the variant is hidden
  unless that Forge mod ID is present.

Only the appearance (tint and gelatin sheen) changes. A mod does
**not** automatically become a compatible variation just because it
registers a slime entity: its creator must provide this small JSON
descriptor (or a resource pack can provide it). No arbitrary third-
party entity models, textures or gameplay abilities are copied.

When a required mod or pack is missing, characters with its saved slot
are displayed using the default Slime appearance. Their saved slot is
kept, so the chosen colour returns when that pack is restored.

## Evolution choices

Evolution currently requires Cyber Level 30, sufficient activity, and
resting in a bed to receive an offer. Only Elf (High Elf/Dark Elf),
Dwarf (Elder Dwarf), Goblin (Hobgoblin), and Dragonborn (Winged
Dragonborn) receive new evolution offers. Other races have no evolution.
Existing characters using older saved evolution IDs remain supported.
