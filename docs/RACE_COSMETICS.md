# CyberRaces cosmetics

CyberRaces now owns race cosmetics directly. Ears is no longer part of the design.

## Core rules

1. A normal Minecraft skin remains the base character skin unless a race genuinely needs a stronger replacement layer.
2. CyberRaces renders race-defining geometry itself.
3. Cosmetic parts attach to normal player bones wherever possible so FA+Player can continue animating the player.
4. Appearance selections are stored server-side with the race.
5. Minecraft 1.20.1 built-in resources are reused by resource location where practical instead of copying vanilla assets into the mod.
6. Custom CyberRaces textures/models are used for features Minecraft does not provide.

## First character-creator data

- Race
- Eye style
- Eye colour
- Race-feature variant

The first UI is deliberately smaller than a full RPG face sculptor. It creates a stable data format first, then visible race parts can be added without changing player saves.

## Race silhouettes

### Human
- Normal player silhouette.
- Feature variants can later represent subtle markings/scars rather than fantasy anatomy.

### Elf
- Long pointed ears attached to the head.
- Three ear variants planned.

### Dwarf
- Short body from Pehkui.
- Broader/stout silhouette.
- Beard remains optional.

### Halfling
- Very short humanoid.
- Softer/rounder ear variants so it does not look like a second Dwarf.

### Orc
- Lower tusks.
- Mildly pointed ears.
- Broader silhouette.
- No forced green skin.

### Goblin
- Long outward ears.
- Small body.
- Optional nose extension later.

### Tiefling
- Horns.
- Animated tail attached to the body.
- Multiple horn variants.

### Dragonborn
- Strongest custom model treatment.
- Dragon-like head/snout shell.
- Horn/crest variants.
- Tail.
- Future scale-colour option.

### Fairy
- Very small body.
- Permanent Zanza's Wings.
- Small fey-ear variants later.

## Arrival integration

Character creation happens before the visible CyberServer arrival.

1. Player connects.
2. CyberRaces marks unfinished players as not ready and opens the full-screen creator.
3. Server keeps the player invisible, invulnerable, frozen and without gravity.
4. Player confirms race and appearance.
5. CyberRaces stores the character and sets `CharacterCreated=true`.
6. CyberServer sees the ready flag and runs the church arrival/beam.
7. The player is now visibly in the world.

This avoids consuming CyberServer's one-time arrival before the player has a finished character.
