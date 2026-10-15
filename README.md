# CyberRaces

**Current version: 0.3.2-alpha.61** · Minecraft **1.20.1** · Forge **47.4.10** · Java **17**

CyberRaces supplies character creation, race-specific attributes/cosmetics, racial abilities, race evolution and the shared **Cyber Level** progression system for the CyberSpectra Season 2 ecosystem. **Race and class are independent:** CyberClasses owns the class selection and equipment/magic rules.

## Crash fix: Forge keymapping initialization (0.3.2-alpha.48)

Fixed a client mod-loading `ExceptionInInitializerError` whose root cause was
`ConcurrentModificationException` inside Forge's `KeyMappingLookup.put`.
The racial ability V keybinding is no longer constructed as a static field
while Forge discovers event subscribers. It is created and registered during
`RegisterKeyMappingsEvent`, and the tick handler skips key input until the
mapping has been registered. The V key, Controls menu category, racial
abilities, network packet and save data remain unchanged.

Only **CyberRaces** changes; replace CyberRaces.jar on client and server with
0.3.2-alpha.48. This fixes the identified unsafe startup path but still
requires an in-game launch test with the full modpack.

## Dwarf mining Fortune and Figura Catfolk models (0.3.2-alpha.61)

**Dwarf:** Removed the unsuccessful crafting enchantment feature.
Dwarves keep their established underground mining-speed bonus and now
receive an innate **Fortune II-style bonus on resource drops** via a
registered Forge 1.20.1 Global Loot Modifier. Applies when a Dwarf
mines Forge-tagged ores (including supported modded ores), glowstone,
gravel or Nether wart and the resulting item is a resource rather than
the original block. Ordinary blocks like cobblestone and dirt are not
duplicated; Silk Touch and tools already enchanted with Fortune II+
are excluded from the additional bonus. This is a balanced chance
of extra resources, not a guarantee on every mined ore.

**Catfolk:** Replaced both ears and tail with an adaptation of the
*Cat_ears.bbmodel* included in the user's Figura download, made by
**Ice_Game** (Planet Minecraft, 2024):
https://www.planetminecraft.com/blog/custom-cat-ears-free-download-figura-log-1/
The actual model uses two ears built from a base, 3x3x1 ear body and
1x1x1 tip, and **four 1x1x3 tail segments**. The Forge render layer
retains ear-adjust controls and animates the four joined tail bones
with a gentle sway. Texture sheets are directly adapted/repacked from
the four supplied Figura colour variants: **Black, Blue-ish, Purple,
and White**. Those replace the previous eleven Minecraft-cat coat
variants to keep the texture selection consistent with the supplied
model; old saved feature IDs outside 0-3 fall back to White.

**Fairy** hovering and particles remain untouched after user confirmation.
The confirmed mechanics for Slime, Arachnid, Goblin, Dogfolk, Tiefling
and Birdfolk are unchanged. No new network packets or save fields.
Update only CyberRaces.jar on both client and server. Both changes
need confirmation in the actual game before CyberRaces is beta-ready.

## Dwarf, Fairy, Catfolk beta finishing pass (0.3.2-alpha.60)

- **Dwarf crafting:** Actually registered an `ItemCraftedEvent`
  handler; earlier builds only had the mining-speed passive.
  Every successfully crafted **single new, enchantable unenchanted
  equipment item** has a 15% chance of gaining a random modest
  vanilla-valid enchantment (approximately enchanting-table levels
  7-14). Compatible modded gear is eligible if it supports normal
  enchantment. This runs on the server and modifies the *crafted*
  stack, not the ingredients. A success message, gentle sound and
  enchant particles make it clear when it triggered.
  Bulk-stack recipes and already enchanted items are excluded.
- **Fairy hovering:** Reduced unwanted vertical oscillation by
  holding a stable vertical velocity inside a 3.2-3.8 block
  hovering range. Gently approaches this range from above/below,
  preserves horizontal movement and normal upward jump momentum,
  and adds a small server-synced END_ROD sparkle trail while hovering.
  Toggling hover on while grounded now automatically starts a
  gentle takeoff, with matching client and server prediction.
  Shift still provides controlled descent, and Icarus flight
  remains separate.
- **Catfolk ears:** Changed the Catfolk ear model to lower,
  outward-angled stepped triangular wedges instead of tall chunky
  rectangular ears. Their coat textures, ear adjustment settings,
  tail animations and racial stats remain unchanged.
- **Birdfolk:** Confirmed working in-game by the player, unchanged.
- Existing tested Slime, Arachnid, Goblin, Dogfolk, Tiefling
  and other races are unchanged. Existing saves and network
  protocol remain compatible. Replace only CyberRaces.jar on
  client and server.

In-game tests are still needed for Dwarf crafting, Fairy hovering
and the new Catfolk ear silhouette.

## Arachnid ladder-like wall hold (0.3.2-alpha.59)

Arachnid wall climbing now follows ladder-style controls:
- Move into a wall without sneaking to climb upward.
- Hold **Shift** to stay at your current height on the wall, without
  moving up or slipping down, even when you release the forward key.
- Release Shift to climb again; move away from the wall to fall normally.

Both the server and the client temporarily suspend gravity and zero
vertical velocity while the crouched wall hold is active. A short-range
collision check keeps contact when movement stops. Gravity is restored
when the hold ends, the race changes, or the player logs out. The
no-gravity flag is only removed when CyberRaces originally enabled it.

Slime is confirmed correct and unchanged. No changes to web projectiles,
other races, saved world data, or networking. Replace only CyberRaces.jar
on both client and server, then verify Shift holds your exact height.

## Slime picker and Arachnid crouch-climb polish (0.3.2-alpha.58)

- Slime Body Colour picker is hidden in character creation. Slime
  skin continues to use automatic gel recolouring, unchanged.
- Arachnids can now crouch while climbing a wall, as on a ladder.
  Sneaking no longer prevents upward climbing or forces a downward
  drift; local and server-side movement use the same velocity.
- No changes to the confirmed Goblin, Dogfolk, Tiefling, Dragonborn
  visuals, other race skins, Slime movement, web-shot behaviour, or
  saved data. Networking protocol remains 13.

Replace only CyberRaces.jar on client and server. Verify crouched
wall-climbing and that Slime no longer has a Body Colour button.

## Base race visual and movement corrections (0.3.2-alpha.57)

This fixes problems identified in game testing:

- **Orc, Dragonborn and Nymph:** Their player skin tones no longer
  recolour automatically. Like Goblin, players can open **Body Colour**
  during race creation, sample a pixel from their original skin and
  manually choose the replacement colour. Existing saved character
  appearance values are preserved. Acquired Undead automatic
  zombification skin tint is unchanged. Dragonborn horn/tail colours
  continue using their separate feature-colour selection.
- **Dragonborn:** Removed disconnected tail ridge/spikes and overlapped
  all six tapering chain pieces. Reduced the angle offsets between
  joints so the tail stays visually attached as it bends.
- **Slime:** Server-side, stronger jumps, landing droplets, movement
  slime particles and a natural short upward rebound after falls of
  at least 3 blocks. Sneak to prevent rebound. No fall damage.
  These particles do not depend on vanilla sprint-dust callbacks.
  No V ability is added.
- **Arachnid:** Climb automatically by pushing against a wall;
  sneaking stops upward movement and permits controlled descent.
  Server and local player apply matching climb velocity. V Web Shot
  now places an actual Cobweb in an empty block at the projectile's
  impact, with an 8-second cleanup, rather than only Slowness.
  The projectile still slows creatures hit directly. Solid blocks and
  existing builds are never overwritten by a web.

Wild NPC race IDs, progression, evolution suspension, other abilities,
the Goblin/Dogfolk/Tiefling tested mechanics, and network protocol
remain unchanged. Replace only CyberRaces on client and server.
The game needs a fresh visual and movement test.

## Complete through-wall treasure outlines (0.3.2-alpha.56)

Follow-up to the 0.3.2-alpha.55 through-wall fix: the gold/cyan
highlights showed through blocks but lost several of the twelve edges
on some graphics setups. This revision draws each edge as a small
camera-facing quad with Minecraft's ordinary position/color shader,
rather than feeding immediate line geometry to Minecraft's special
line-expansion shader. All twelve edges of each scanned block are
emitted explicitly, with depth testing disabled during draw and all
render state restored afterward.

The scan, duration, range, max block count, cooldown, coloured
ore/stocked chest distinctions, and saved player data are unchanged.
Dogfolk and Tiefling abilities are unaffected. This update only
requires CyberRaces.jar, on both client and server. Please verify the
complete cube outline from several viewing angles with solid stone
between the player and the ore.

## Treasure Instinct renderer correction (0.3.2-alpha.55)

The Goblin Treasure Instinct highlight now uses an immediate draw with
Minecraft's standard line shader. Unlike the previous batched line render,
the highlight is drawn while depth testing is disabled, allowing ore and
stocked-container boxes to remain visible behind terrain. Gold and cyan
colors, scan radius, limits, ability duration, and cooldown are unchanged.
Render state is restored after drawing. No new world entities or packets.

Only CyberRaces.jar requires replacement on both sides. In-game
confirmation of behind-block visibility is still needed.

## Vanilla Glowing effect for Hunter's Nose (0.3.2-alpha.54)

Dogfolk **Hunter's Nose (V)** now uses Minecraft's actual
`MobEffects.GLOWING` on up to 16 nearby living creatures for **7 seconds**
within 22 blocks. Creatures display the vanilla bright silhouette through
walls, not client-only setGlowingTag manipulation. The effect is applied
on the authoritative server, syncs normally and can be seen by **all nearby
players**, just like a regular Glowing potion effect. Potion particles and
status icons are suppressed; the distinctive entity outline remains.
An existing longer Glowing effect is preserved.

Goblin Treasure Instinct still highlights ore and stocked containers
using its gold/teal block-outline renderer, because Minecraft's vanilla
Glowing **status effect cannot be applied to blocks**. Giving blocks a
true vanilla entity-style outline would require a separate block-outline
rendering implementation; this release does not create fake entities.

The 28-second Dogfolk cooldown, saved characters, 7-second reveal duration,
other racial abilities, network protocol 13 and CyberQuest compatibility
are unchanged. Replace **CyberRaces.jar** on client and server.

## Final base-race V abilities (0.3.2-alpha.53)

The remaining three base-race abilities now have focused mechanics:

- **Goblin / Treasure Instinct (V):** One server-side, bounded scan
  of nearby ore and non-empty loot containers (14-block range), with
  client-only golden/teal block outlines lasting 8 seconds; no block
  mutation, exposed coordinates in chat, or repeated particle trails.
  The 30-second cooldown is saved on the player's data. Nearest 16
  ore blocks and 8 containers are shown to limit rendering.
- **Dogfolk / Hunter's Nose (V):** Reveals up to 16 nearest living
  creatures within 22 blocks as a private client-only glowing outline
  through walls for 7 seconds, then clears them. Does not permanently
  change their state or add a globally visible potion effect. 28-second
  cooldown, saved with the character.
- **Tiefling / Infernal Surge (V):** An eight-second melee window with
  +25% outgoing melee damage and three seconds of fire on susceptible
  victims; modest vanilla flame/sound on activation. 32-second cooldown.
  No mana, Iron's Spells or extra item requirements.

A compact per-player sensing packet enables the two private visual
effects. Networking protocol **13** requires both client and server
to install CyberRaces 0.3.2-alpha.53; CyberQuest's optional race guide
is updated separately. Old race IDs, appearance, evolution suspension,
Zombification and cure, Wild NPC compatibility and saved data remain
unchanged. The old Goblin and Dogfolk ability cooldown keys are reused.

## Race overhaul, first gameplay and visuals pass (0.3.2-alpha.52)

- **15 ordinary starting races**. Halfling and Aasimar are retired from
  new character creation and new natural Wild NPC assignments. Their
  registry IDs and existing player/NPC saves remain readable; no saved
  data are deleted. Undead is still acquired by zombification.
- Display names are **Catfolk, Dogfolk, Foxfolk, Birdfolk, Slime,
  Arachnid** (no borrowed species/subtitle suffixes). Race IDs do not change.
- Orc exposed skin gains a moss-green complexion. Dragonborn skin
  matches the chosen horn/tail colour (default muted teal), and Nymph
  skin has a subtle nature-green tint. This composes selectively onto
  source player skin pixels and preserves clothing, like Undead skin.
- Dragonborn's long disjointed tail has been replaced with a shorter,
  connected six-piece taper with restrained swaying. Catfolk ears are
  wider, pointed and more feline. These geometry changes need in-game
  visual verification at different GUI scales/player poses.
- **Dwarves:** 7% chance to add a modest random low-level valid
  enchantment to a newly crafted, enchantable piece of equipment.
- **Nymphs:** slowly regain one health while in water/rain every five
  seconds, in addition to their existing nature ability.
- **Slimes:** no fall damage, 8% stronger jumps, a soft automatic
  rebound after longer falls (sneak prevents it), and vanilla slime
  droplets when running, jumping and landing. The sprint-particle
  client mixin replaces ordinary block dust for player and Wild NPC
  Slimes. Elastic Leap V ability is removed for players.
- **Fairies:** small End Rod sparkles while actively hovering.
- **Arachnids:** automatic spider-like wall climbing (sneak to brake),
  and V now fires a visible cobweb-item projectile. On impact, it
  gives a struck living target five seconds of strong Slowness and
  produces a short web/cloud burst; it does not place permanent webs.
- Goblin Treasure Instinct, Dogfolk Hunter's Nose and Tiefling Infernal
  Surge are implemented in 0.3.2-alpha.53. The race journal is in
  CyberQuest 0.3.0-alpha.12.
- All previous evolution suspensions, Undead zombification and cure
  data, and race IDs remain intact.

Only CyberRaces needs replacing for this version, client and server.
Minecraft 1.20.1, Forge 47.4.10.

## Complete acquired Undead presentation and behaviour (0.3.2-alpha.51)

This update extends 0.3.2-alpha.50's infection and golden-apple cure
without changing how players contract or remove the condition:

- **Undead skin tone:** Player skins gain a muted moss-green complexion.
  The client composites it from the original 64x64 skin, sampling the
  lower face to identify likely exposed skin and retaining hair, eyes,
  clothing, transparent layers and original texture layout. Custom
  skinned body recolours are applied first. The exact result can vary
  with third-party player skins that have unusual facial patterns.
- **Original racial models stay visible:** The character sync packet
  carries both the underlying race and an explicit Undead flag. Elf
  ears, horns, tails, wing models and other base-race cosmetics can
  remain visible, with exposed racial ears tinted green where applicable.
  Underlying race, appearance and evolution save data are unchanged.
- **Zombie traits:** Uncovered, non-creative Undead players burn in
  direct daylight (rain, water and equipped helmets protect them).
  Existing Undead fire weakness, poison/Hunger effect resistance,
  base-race override and Grave Resilience ability continue to work.
- **Undead neutrality:** Vanilla and modded mobs reporting
  `MobType.UNDEAD` (including CyberNpc zombie replacements) cannot
  acquire an Undead player as their target; any stale target is cleared,
  and their attacks are defensively cancelled. This includes zombies,
  husks, drowned, skeletons and other vanilla undead. Mods that do
  not mark their undead mobs as `MobType.UNDEAD` may require a
  dedicated compatibility adapter.
- **Reversion:** The existing Weakness + normal Golden Apple cure
  restores the original skin, racial features and normal mob targeting.
  No alternate save data or new world data are required.

CyberRaces' character sync packet changes; networking protocol `12`
requires the updated JAR on **both client and server**. Install only
`CyberRaces.jar` 0.3.2-alpha.51. CyberNpc 0.44.61 remains unchanged.
Evolution mechanics are still suspended.

## Acquired Undead and zombie-villager-style cure (0.3.2-alpha.50)

**Player Zombification:** When CyberNpc is installed, every successful
hit from a vanilla Zombie or CyberNpc Zombie has a 1% chance to apply
CyberNpc's existing `cybernpc:zombification` effect for 120 seconds.
Administrators may also apply that same effect to test the mechanic.
If the effect runs out, the player becomes **Undead**. Its inherited race
remains saved in the original `CyberRaces.Race` key; an acquired flag,
`AcquiredUndead`, temporarily changes the effective race for attributes,
V-key ability, cosmetics and sync. Example: Elf -> Undead Elf -> Elf.
Original appearance, Cyber Level, class, inventory and existing stored
evolution data are kept; no player or Zombie entity is replaced.

**Curing:** Exactly like a Zombie Villager's ordinary cure inputs, apply
**Weakness** and then use a **regular Golden Apple** on the affected player
(right-click them in multiplayer); for a solo player, **eat the apple**
while Weakness is active. A transformed Undead begins a randomized
**3-5 minute** curing countdown. The time remaining persists through
logout/death/restarts, pausing while the character is offline. Brief
smoke particles appear during curing, followed by vanilla Happy Villager
particles and cure sounds; the original ancestry and its stats/abilities
are restored at completion. A player treated while *still infected* is
cured immediately before turning Undead, preventing transformation.
Creative players giving an apple do not expend one; surviving players
consume the apple normally. Only the basic Golden Apple is accepted.

Older players whose saved base race was directly set to Undead lack a
recorded original ancestry; they return to Human if successfully cured.
Admins can use the existing race-set command to give them another origin.
The existing CyberNpc Wild NPC zombification/conversion behavior and
spawn policies are unchanged. CyberRaces still works without CyberNpc;
player infection simply will not occur until CyberNpc is present.

No evolution mechanics have been re-enabled; races and save IDs remain
backwards compatible. Only **CyberRaces.jar** needs replacement (both
client and server) for this update.

## Race overhaul groundwork (0.3.2-alpha.49)

**Normal character creation** now offers 17 starting races. Undead is no
longer in the creator carousel and cannot be selected via the ordinary
`/cyberraces race choose undead` command. Server-side character creation
also checks eligibility: spoofed `SubmitCharacterPacket` messages cannot
bypass the restriction. Existing Undead characters are preserved, and
admin `/cyberraces race set` remains available for migration/testing.

**Evolutions are temporarily suspended, not deleted.** Previously stored
evolution IDs/affinity data remain in saved player NBT. Effective evolution
bonuses, names, appearances and extra passive abilities no longer apply;
sleep evolution offers and new evolution selection/setting are blocked. The
`/cyberraces evolution` command simply reports that the system is paused.
Cyber Level, CyberClasses progression, base-race stats and biome attunement
remain active.

**Undead conversion is the next separate milestone.** The existing
`cybernpc:zombification` effect currently infects **Wild NPCs**, not
players. This release does not yet change players into Undead on infection.
A later revision must add player exposure, progress, preservation of
original ancestry/appearance, and curing rules without breaking older
saves. No existing race IDs or save keys were renamed.

## Playable races

There are **18 base races** in the current `Race` registry:

| Race | Race | Race |
| --- | --- | --- |
| Human | Elf | Dwarf |
| Halfling | Orc | Goblin |
| Tiefling | Dragonborn | Fairy |
| Nymph | Catfolk | Dogfolk |
| Foxfolk | Aasimar | Birdfolk |
| Slime | Arachnid | Undead |

Each has its own data-defined mix of physical size, base health, movement and hunger modifiers, mana and regeneration, spell resistance, knockback resistance, armour bonuses and fire-damage sensitivity. Not every race receives an activated ability: many benefits are passive.

**Pehkui** supplies physical player/NPC scaling where compatible. Various feature models/skins provide race-specific cosmetics, including beastfolk features and optional wings.

## Character creation

New players enter a temporary hidden/frozen hold while they choose a race and customise supported features. The creator contains a player preview and controls for eye appearance, eye/feature colour, relevant variant styles, ears, wing choices and body/skin colour handling where supported.

After the player completes race creation, **CyberClasses** can open its mandatory class selector. With **CyberNpc + CyberServer** and configured arrival locations, the complete character setup flows into the one-time cinematic arrival and Pope welcome. Returning characters with saved race data are migrated/recognised rather than forcibly replaying creation.

Use the normal in-game creator for player selection; admin resets are intended for testing.

## Additional races in 0.3.2-alpha.43

- **Slime:** a humanoid player whose **entire 64×64 player skin** receives a procedurally generated glossy, translucent-looking gelatin treatment. Your original skin's eyes, facial details, clothing and UV layout remain visible under the tint; armour, movement and humanoid animations remain intact. The full-body slime skin follows the chosen evolution's colour: molten orange for Magma, silver for Metal, yellow-green for Acid, turquoise for Crystal, ice blue for Frost, violet for Void and gold for Royal. This is a pixel-processed skin composite, **not** physically translucent glass geometry. With CyberNpc 0.44.44+, Slime NPCs use the same composited skin approach instead of rendering an additional translucent humanoid: female slim arms and bow poses remain aligned. Earlier CyberNpc releases retain the previous semi-transparent overlay as a compatibility fallback. Slimes take 80% less fall damage and use **V** for Elastic Leap. They are slightly more vulnerable to fire before a resistant evolution.
- **Arachnid:** a quick humanoid spider-kin. Sneaking into a wall allows climbing (no forced hovering); **V** activates a web snare against nearby enemies in front of the player. Evolution choices emphasize underground stealth, poisonous webs, pursuit, protective silk or binding strength.
- **Undead:** a slower, sturdy revenant-like humanoid with lower hunger consumption, resistance to magical effects and increased fire vulnerability. Poison and Hunger status effects are cleared, and **V** activates Grave Resilience for healing and temporary damage resistance. Evolution choices include spectral speed, magical lich affinity, heavy knight defence and plague power.

The three new race IDs are `slime`, `arachnid` and `undead`. All are available during first-join character creation and work with the persistent race/evolution data format. **Undead is player-only**: Slime and Arachnid, but not Undead, can be assigned to Wild NPCs. CyberNpc racial combat AI for eligible races is implemented in CyberRaces with independent cooldowns; CyberNpc itself does not need rebuilding.

## Biome-weighted natural Wild NPC races (0.3.2-alpha.46)

With CyberNpc **0.44.40+**, natural Wild NPC expeditions select one **base race** appropriate to the surrounding biome and dimension, using their shared pack ID to keep the same-biome group consistent. Examples:

| Natural environment | Favored races |
| --- | --- |
| Plains | Human, Halfling, Aasimar |
| Flower forests/meadows | Fairy, Nymph, Halfling |
| Forests | Elf, Fairy, Nymph, Foxfolk |
| Taiga | Dogfolk, Foxfolk |
| Cold/snowy biomes | Dogfolk, Foxfolk, Dwarf |
| Mountains/peaks | Dwarf, Dragonborn, Birdfolk |
| Savanna/badlands | Orc, Goblin, Dragonborn |
| Jungle | Catfolk, Arachnid |
| Swamp | Slime, Nymph, Goblin |
| Caves and deep underground | Goblin, Arachnid, Dwarf |
| Rivers/coasts | Nymph, Human, Slime |
| Nether (rare expedition spawns) | Tiefling, Dragonborn, Goblin (some Slime/Dwarf variants) |
| End | **Never natural spawns** |

The choice uses weighted habitat pools rather than a hard one-race-per-biome rule, and recognizes plausible modded biome registry names. **No Wild NPC receives Undead or any evolution**. Existing Wild NPC races remain unchanged; this affects only previously unassigned natural spawns. Spawn eggs and manual NPC creation keep the prior race selection rules. CyberNpc controls natural pack size, party cohesion and campfire placement; CyberRaces owns the base race/appearance and racial combat AI.

## Biome attunement (v0.3.2-alpha.45)

Players and Wild NPCs gain a **small, temporary racial homeland bonus** after spending **60 continuous seconds in a single favored biome**. The server tracks the actual biome ID and dimension: crossing into any other biome immediately clears the buff and restarts the timer, even when the next biome belongs to the same type. **No buff is permanent**, and a character cannot gain multiple biome bonuses at once.

- Base races receive weaker buffs appropriate to their origin: for example, Elves in woodlands, Dwarves in mountains, Slimes in swamps, Arachnids in caves and player-only Undead in soul/deep-dark biomes.
- Accepted evolutions replace that starting homeland with a more distinctive environment, e.g. Magma Slime in the Nether, Winterfang in frozen biomes, Tideglimmer along rivers and Skyclaw in mountains. All 79 evolutions have a biome affinity, with their named lore reflected in the matching habitat.
- Depending on the race and evolution, the active bonus grants **+5.5%/+10% movement speed**, **+1/+2 armour**, **+0.75/+1.5 attack damage**, or **+0.5/+1 Luck with modest periodic recovery** (base/evolved respectively).
- Bonuses use separate temporary attribute modifiers and are removed when leaving the biome, changing race, logging out or unloading an NPC. They do not overwrite normal potion effects or persist on saved characters.
- Existing evolution-specific passives (such as Nether fire protection, river water-breathing, or underground mining speed) only activate **after the corresponding biome attunement**; short effects applied by this system are tracked and removed when the attunement ends.
- The `/cyberraces biome status` command shows your favored biome, active bonus or time remaining. Compatible modded biomes can match by their registry name (forests, taigas, rivers, jungles, caves, etc.).

The biome system rewards **staying in a place**, while the pre-evolution playstyle-history system still rewards **travel and discovery**. It doesn't change the Level 30 requirement or the choice offered after sleeping.

## Racial abilities

**V** is the default key for the racial ability. Abilities are handled server-side and may have cooldowns, eligibility conditions or a toggle.

| Base race | Implemented active ability |
| --- | --- |
| Dragonborn | Dragon Breath (requires **Iron's Spells 'n Spellbooks** integration; available from level 10) |
| Fairy | Toggleable hover |
| Dogfolk | Scent / nearby tracking |
| Foxfolk | Quickstep |
| Orc | War Cry |
| Goblin | Scavenger Sense |
| Nymph | Nature Grace |
| Aasimar | Radiance (requires **Iron's Spells 'n Spellbooks**) |
| Birdfolk | Wing Burst |
| Slime | Elastic Leap (bounce forward, reduce landing damage; evolution-specific bonuses) |
| Arachnid | Web Snare (briefly slows targets ahead; Venomfang poisons) |
| Undead | Grave Resilience (healing and temporary defence; evolution-specific buffs) |

Other races have passive/statistical identities but **no V-key active ability yet**. The `RacialAbilityPacket` deliberately reports this, rather than silently creating an ability.

Racial effects for Wild CyberNpc entities are handled separately by CyberRaces' NPC AI bridge. Implemented NPC behaviour includes appropriate dragon breath, fairy evasion, fox quickstep, orc war cry, nymph grace, catfolk pounce, aasimar radiance, birdfolk wing bursts, and utility/scavenging behaviour as applicable. The NPC implementation need not exactly mirror every player's V-key control.

## Dynamic race evolution — Cyber Level 30

The current `RaceEvolution` registry defines **79 evolution choices** across the 18 base races. Each race has multiple branches with different attribute modifiers and ability profiles. Examples:

- Human: Paragon, Awakened, Survivor.
- Elf: High Elf, Wood Elf, Moon Elf.
- Dragonborn: Emberblood, Frostblood, Stormblood, Skyborn.
- Fairy: Sylph, Glimmerfae, Thornfae.
- Nymph: Dryad, Naiad, Oread, Anthousa.
- Dogfolk: Houndkin, Wolfkin, Mastiffkin.
- Foxfolk: Swiftfox, Trickster, Spirit Fox.
- Aasimar: Seraphic, Fallen, Celestial Guardian.
- Birdfolk: Raptor, Nightwing, Stormwing.
- Slime: Magma Slime, Metal Slime, Acid Slime, Crystal Slime, Frost Slime, Void Slime, Royal Slime.
- Arachnid: Webweaver, Venomfang, Cave Stalker, Brood Sovereign, Silk Reaver.
- Undead: Revenant, Lichbound, Dread Knight, Wraith, Plagueborn.

### Additional original-race branches (v0.3.2-alpha.44)

All fifteen earlier playable races now have an additional class-independent lineage, for **79 total evolution choices**. These added paths are **player-only** and retain the normal Level 30, playstyle-affinity and voluntary post-sleep offer requirements:

| Base race | New evolution | Situational specialty |
| --- | --- | --- |
| Human | Horizonbound | Travel and sprinting in the open |
| Elf | Astralweaver | Starry skies, night vision and arcane potential |
| Dwarf | Cinderthane | Nether heat and fire resistance |
| Halfling | Nightwhisper | Stealthy night movement |
| Orc | Thundermaw | Storm-weather mobility |
| Goblin | Deepdelver | Faster underground mining |
| Tiefling | Netherstrider | Nether travel and heat resistance |
| Dragonborn | Obsidianhide | Greater armour and underground resilience |
| Fairy | Tideglimmer | Water affinity and breathing underwater |
| Nymph | Stormsinger | Fighting and moving through storms |
| Catfolk | Skyclaw | Air mobility and gentler descents |
| Dogfolk | Winterfang | Pursuit through cold climates |
| Foxfolk | Cinderfox | Fire-resistant travel |
| Aasimar | Sunward Archon | Restorative energy in daylight |
| Birdfolk | Tidecrest | Fast swimming and coastal exploration |

Each new evolution has **permanent stat modifiers and a lightweight, situational passive** that checks its environment on the server. These passives expire when leaving their relevant conditions. Previously saved evolution IDs and character appearance data remain valid.


**As of 0.3.2-alpha.42, race evolution is prompted after sleeping in a bed.** Your activity and environmental history still calculate a lineage recommendation, but reaching the level/affinity thresholds **does not evolve you automatically**. After a genuine sleep, you see your recommended lineage, its stats and ability profile, and can choose **Evolve** or **Not Yet**.

**Not Yet / ESC** leaves your race unchanged and all earned affinity history intact. To reconsider, sleep again after gaining more experience; your next offer uses your current affinities and may change if your playstyle has changed. You cannot cycle freely through every race evolution on that screen, and the server validates accepted offers. Earlier saved evolutions are preserved.

- Race evolution requires **Cyber Level 30**, at least **80 total activity points** and **28 points in a dominant affinity**. Activity can be accumulated **before** Level 30.
- **Battle:** defeat monsters and bosses.
- **Mystic:** cast Iron's spells when installed, use magical implements/potions, or defeat enemies with magic-related weapons.
- **Wanderer:** explore previously unvisited chunks in multiple locations and dimensions (sampled every five seconds).
- **Guardian:** survive incoming damage, recover health and overcome bosses.
- **Situational history:** relevant water, Nether, storms, high altitude, underground exploration and night conditions influence which branch wins when playstyles are close.
- The journey now distinguishes **forest, cold climates, open sky, daylight, darkness, underground, storms, water, the Nether and the End**. Unvisited chunks and active travel earn context history; a player standing idle cannot farm it. Earlier seven-context save data is upgraded without losing progress. Different evolution paths within each race have distinct playstyle/environment combinations rather than sharing identical scoring, avoiding permanently unreachable choices. The `/cyberraces evolution progress` command also shows the leading environment and, when ready, your current recommendation. You still must **sleep and explicitly accept** before changing race.

- Branches are **recommended server-side** from the player's race-specific evolution roster. Acceptance is required; packets cannot request an unearned branch. Existing selected evolutions are preserved through upgrades; changing/clearing a base race starts a new journey.
- Check progress using `/cyberraces evolution progress`. The system uses bounded scores and limited stored exploration history, avoiding per-tick scans.

The 47 evolution/stat paths exist, but not every branch has a unique independent active skill. The situational weighting is a **playtest balancing pass**, intended for further in-pack tuning.

## Cyber Level progression

The persistent Cyber Level system is distinct from vanilla XP:

- Starts at Level 1 and has a **maximum level of 1,000**.
- Tracks total Cyber XP with a scaling XP-per-level requirement.
- Grants experience through eligible gameplay activities; other mods can grant Cyber XP through their compatibility bridges.
- Gates eligibility for **advanced Class Books from CyberClasses** and **earned racial evolution starting at Level 30**.
- CyberServer can assign starting Cyber Levels and natural class policy for Wild NPCs. It cannot override the base-race-only or Undead-exclusion rules.

## Current commands

Player-facing commands:

```mcfunction
/cyberraces race list
/cyberraces race info <race>
/cyberraces race get <player>
/cyberraces evolution get
/cyberraces evolution progress
/cyberraces character status
/cyberraces level status
```

Admin commands (permission level 2, examples):

```mcfunction
/cyberraces race set <player> <race>
/cyberraces evolution set <player> <evolution>
/cyberraces evolution reset <player>
/cyberraces character reset <player>
/cyberraces level addxp <player> <amount>
/cyberraces level setlevel <player> <level>
/cyberresetall [player]
```

`/cyberresetall` is a **testing reset**, clearing CyberRaces character/progression data and invoking optional class/intro resets when those mods are present. Do not use it on a player whose progression should be kept. The older `/cyberlevel` command root and typo `/cyberresetal` alias are no longer current. Use `/cyberraces level ...` and `/cyberresetall`.

## Integrations and limits

- **CyberClasses:** separate player classes and Level 20 class progression.
- **CyberNpc:** Wild NPCs receive **17 permitted base races**; **Undead/Reborn is playable by players only**, never assigned to Wild NPCs. They keep racial appearance, baseline stats and base racial combat/utility AI but can **never randomly evolve at Level 30 or later**. Older evolved Wild NPCs are migrated back to their base race when loaded. Older Undead Wild NPCs are rerolled to an allowed base race, retaining their identity, class, level and inventory.
- **CyberQuest:** optional character/race/evolution prerequisite checks and Cyber XP awards.
- **CyberServer:** first-arrival sequencing and Wild NPC Cyber Level/class progression policy. Any old `evolvedraces` setting is overridden by CyberRaces' base-race-only NPC rule.
- **Pehkui:** entity scaling; **Iron's Spells 'n Spellbooks:** optional racial mana/spell calculations and selected abilities; **Icarus/Curios and other wing mods:** optional flight and wing rendering paths.

The source contains additional tuning and design notes under `docs/RACE_BALANCE.md` and `docs/RACE_COSMETICS.md`. Those and the current Java code are the source of truth for detailed balance; all races/abilities still need gameplay validation in the full modpack.
