# Guild registration gates RPG level and Cyber XP

## Why
Before this update, creating a CyberRaces character unlocked progression.
The player could gain RPG XP from hostile mob kills or advancement completion
before ever selecting a class at the Adventurers' Guild. CyberQuest also
calls the same XP manager for quest rewards.

## Rule
With CyberClasses installed, a **ServerPlayer** may gain Cyber XP only after
their persistent `CyberClasses` record has both `ClassChosen=true`
and a nonblank `Class` id. Guild class registration writes this record.

The shared `ProgressionManager.addExperience` enforces this for combat,
advancements, quest rewards, and other callers; no source can bypass it.
Blocking XP does not block ordinary Minecraft vanilla XP or advancements.

- Before class registration: **zero Cyber XP**, no stored overflow, no Cyber Level gain.
- After class registration: Cyber XP awards work normally.
- Existing saves: do not destroy or reset already-earned Cyber Levels or XP.
- Wild NPC and other non-player progression: unchanged.
- Without CyberClasses installed: CyberRaces standalone levelling remains unchanged.
- Admin-set levels: unchanged; the XP gate does not block intentional admin commands.

## Local verification
1. In a new or reset player with a race but **no class**, earn vanilla
   advancements, defeat hostile mobs and complete any available quest.
   Verify Cyber Level stays 1 and TotalXp stays 0, but vanilla experience
   and advancements can still work.
2. Register a class through the Guild Receptionist and repeat. Cyber XP should
   accumulate and level-up sounds/messages should return.
3. Confirm CyberQuest XP rewards now respect the same gate.
4. Rejoin/restart and verify class and levels persist; existing developed
   characters must not be reset.
5. Test a Wild NPC kill and CyberRaces without CyberClasses to confirm no
   unintended gating.

Not compiled or in-game tested in this environment.
