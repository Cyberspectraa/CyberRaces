# CyberRaces

**Current version: 0.3.2-alpha.44** · Minecraft **1.20.1** · Forge **47.4.10** · Java **17**

CyberRaces supplies character creation, race-specific attributes/cosmetics, racial abilities, race evolution and the shared **Cyber Level** progression system for the CyberSpectra Season 2 ecosystem. **Race and class are independent:** CyberClasses owns the class selection and equipment/magic rules.

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

- **Slime:** a humanoid player whose **entire 64×64 player skin** receives a procedurally generated glossy, translucent-looking gelatin treatment. Your original skin's eyes, facial details, clothing and UV layout remain visible under the tint; armour, movement and humanoid animations remain intact. The full-body slime skin follows the chosen evolution's colour: molten orange for Magma, silver for Metal, yellow-green for Acid, turquoise for Crystal, ice blue for Frost, violet for Void and gold for Royal. This is a pixel-processed skin composite, **not** physically translucent glass geometry. Wild NPCs receive the matching semi-transparent slime overlay through the CyberNpc-compatible humanoid model layer. Slimes take 80% less fall damage and use **V** for Elastic Leap. They are slightly more vulnerable to fire before a resistant evolution.
- **Arachnid:** a quick humanoid spider-kin. Sneaking into a wall allows climbing (no forced hovering); **V** activates a web snare against nearby enemies in front of the player. Evolution choices emphasize underground stealth, poisonous webs, pursuit, protective silk or binding strength.
- **Undead:** a slower, sturdy revenant-like humanoid with lower hunger consumption, resistance to magical effects and increased fire vulnerability. Poison and Hunger status effects are cleared, and **V** activates Grave Resilience for healing and temporary damage resistance. Evolution choices include spectral speed, magical lich affinity, heavy knight defence and plague power.

The three new race IDs are `slime`, `arachnid` and `undead`. All are available during first-join character creation, work with the persistent race/evolution data format and are eligible for CyberNpc Wild NPC assignment. CyberNpc racial combat AI for these races is implemented in CyberRaces with independent cooldowns; CyberNpc itself does not need rebuilding.

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
- Arachnid: Webweaver, Venomfang, Cave Stalker, Broodmother, Huntsman.
- Undead: Revenant, Lichbound, Dread Knight, Wraith, Plagueborn.

### Additional original-race branches (v0.3.2-alpha.44)

All fifteen earlier playable races now have an additional class-independent lineage, for **79 total evolution choices**. These added paths are **player-only** and retain the normal Level 30, playstyle-affinity and voluntary post-sleep offer requirements:

| Base race | New evolution | Situational specialty |
| --- | --- | --- |
| Human | Wayfarer | Travel and sprinting in the open |
| Elf | Starseeker | Starry skies, night vision and arcane potential |
| Dwarf | Emberforged | Nether heat and fire resistance |
| Halfling | Whisperfoot | Stealthy night movement |
| Orc | Stormchaser | Storm-weather mobility |
| Goblin | Cavewright | Faster underground mining |
| Tiefling | Riftwalker | Nether travel and heat resistance |
| Dragonborn | Earthscale | Greater armour and underground resilience |
| Fairy | Dewdrop | Water affinity and breathing underwater |
| Nymph | Tempest Nymph | Fighting and moving through storms |
| Catfolk | Cloudleaper | Air mobility and gentler descents |
| Dogfolk | Frosthound | Pursuit through cold climates |
| Foxfolk | Emberfox | Fire-resistant travel |
| Aasimar | Dawnbringer | Restorative energy in daylight |
| Birdfolk | Tidewing | Fast swimming and coastal exploration |

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
- CyberServer can assign starting Cyber Levels to naturally generated Wild NPCs and adjust natural class/evolution policy.

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
- **CyberNpc:** Wild NPCs receive **base races only**, including the three new ones. They keep racial appearance, baseline stats and base racial combat/utility AI but can **never randomly evolve at Level 30 or later**. Older evolved Wild NPCs are migrated back to their base race when loaded, keeping their race, class, level, inventory and identity.
- **CyberQuest:** optional character/race/evolution prerequisite checks and Cyber XP awards.
- **CyberServer:** first-arrival sequencing and Wild NPC Cyber Level/class progression policy. Any old `evolvedraces` setting is overridden by CyberRaces' base-race-only NPC rule.
- **Pehkui:** entity scaling; **Iron's Spells 'n Spellbooks:** optional racial mana/spell calculations and selected abilities; **Icarus/Curios and other wing mods:** optional flight and wing rendering paths.

The source contains additional tuning and design notes under `docs/RACE_BALANCE.md` and `docs/RACE_COSMETICS.md`. Those and the current Java code are the source of truth for detailed balance; all races/abilities still need gameplay validation in the full modpack.
