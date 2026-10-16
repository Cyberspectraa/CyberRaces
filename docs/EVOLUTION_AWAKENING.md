# Sleep evolution — in-world awakening

At **Cyber Level 30**, an unevolved Elf, Dwarf, Goblin or Dragonborn
may sleep in a bed to evolve. Other races have no evolution.

The player wakes as normal. After eight ticks the server starts a
**48-tick (~2.4-second) awakening** with spiralling race-coloured dust,
vanilla enchantment/end-rod particles, enchanting-table/amethyst sounds
and Minecraft title text. Movement and camera remain under the player's
control; there is no full-screen menu or forced camera animation.

- **Dwarf** automatically becomes **Elder Dwarf**.
- **Goblin** automatically becomes **Hobgoblin**.
- **Dragonborn** automatically becomes **Winged Dragonborn**.
- **Elf** sees a **two-button selector** only after the animation,
  choosing **High Elf** or **Dark Elf**. Esc postpones the choice until
  the next sleep. No third Later button or full-page GUI appears.

Once a single evolution applies (or an Elf picks one), the server sends
a short burst of End Rod particles, beacon activation sound and the
evolved race name as Minecraft title text.

No activity points or biome tracking are required. Only the server
sets evolution and checks the level, race and outstanding selection.
The sole event tick for this logic is CharacterCreationEvents, avoiding
the earlier duplicate tick in RacialPassiveEvents. An interrupted
animation, logout or world reload clears pending offers safely.

Appearance Mirror, the existing race cosmetics and the character save
format are unchanged by this update.
