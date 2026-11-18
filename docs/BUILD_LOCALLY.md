# CyberRaces development without GitHub Actions

Use branch `development-no-actions`. Do not merge it into `main` or open
a pull request targeting `main` while Actions minutes are unavailable.

**Requirements:** Minecraft Forge 1.20.1 project; JDK 17 and Gradle **8.1.1**
installed locally, available on `PATH`. First build downloads Forge/Gradle
dependencies and therefore needs internet access.

On Windows double-click `build-local.bat` from the repository root.
The launcher runs `gradle clean build --no-daemon --stacktrace` and, **only
if the build succeeds**, creates a convenient `CyberRaces.jar` at the root.
The original versioned JAR is also in `build/libs/`.

On other systems: `gradle clean build --no-daemon --stacktrace`.
Use the versioned JAR in `build/libs/`.

Clone directly to the no-Actions branch:

```bash
git clone --branch development-no-actions https://github.com/Cyberspectraa/CyberRaces.git
cd CyberRaces
```

## Current unverified development changes

- [x] Character creator and server validation no longer offer Dragonborn,
  Fairy, Birdfolk, Goblin or Dwarf to new players.
- [x] Wild NPC race pool keeps Goblin and Dwarf, while dropping Dragonborn,
  Fairy and Birdfolk.
- [x] Preserve all enum IDs, existing character NBT, appearance values and
  admin override behaviour; no forced change to already created characters.
- [ ] Handle existing players who previously chose retired races and disable
  removed race abilities; preserve progression through migration.
- [ ] Audit Ice and Fire references in affected Cyber mods.
- [ ] Run a successful local Forge build and Minecraft test.

Do **not** label the change as tested or deploy to your main pack until the
local build succeeds. No GitHub Actions were invoked for these branch commits.
