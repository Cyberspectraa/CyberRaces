# Race retirement update (CyberRaces 0.3.2-alpha.75)

This release targets the user's **test-world-only** development stage. There
are no real players to migrate. Do not add a migration subsystem prematurely.

## Player choice

- Not selectable: **Dragonborn, Fairy, Birdfolk, Goblin, Dwarf**.
- Server-side character creation, client race picker and admin /cyberraces
  race set validation all reject these.
- Keep enum IDs so mod data formats and older test worlds can still be read.
- Goblin and Dwarf **remain in the wild NPC race pool**.
- Retired flying races do not spawn as new wild NPC races.

## Removed powers and flight

- V-key Fairy hover, Birdfolk Wing Burst, Dragonborn Breath and player Goblin
  scavenger actions no longer run; a legacy packet cannot re-enable breath.
- No automatic Curios/Icarus racial wings are equipped by CyberRaces on race
  set, login/reapply or periodic checks.
- Existing CyberRaces-*tagged* Fairy/natural wings are cleaned up by the
  existing race reapply/login logic. **Player-earned/unmarked Icarus wings are
  not removed**, so equipment or magic can still unlock flight later.
- Skyborn, Elder Dwarf and Hobgoblin evolution IDs are retained but disabled
  for player progression. High Elf and Dark Elf remain usable.
- Kept harmless, now-unused class definitions where deleting their shared API
  would increase compatibility risk; these are not gameplay-accessible.

## Validation

This source was committed to `development-no-actions` and has **not** been
compiled or tested by GitHub Actions. The project owner builds locally using:

```powershell
cd "$HOME\CyberMods\CyberRaces"
git switch development-no-actions
git pull --ff-only
.\build-local.bat
```

Test a fresh character, verify the offered races, try the V-key, and check
that no innate wings arrive. Back up any test world before installing.
Future plans: audit and remove Ice and Fire integration, then implement the
admin-only Island Rift and main-seven escort/departure logic in CyberNpc.
