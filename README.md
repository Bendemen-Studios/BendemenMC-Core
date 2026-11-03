# BendemenMC Core

Paper plugin and datapack utilities for BendemenMC.

## Features

- `/dailyreward` (aliases: `/daily`, `/dailybonus`) opens a server-wide daily reward chest GUI.
- `/days` opens a top-10 Minecraft play-days leaderboard, personal statistics, and World Age.
- The Worldborder Challenge datapack is included in `datapacks/worldborder-challenge/`.

## Minecraft days and online counting

- Player playtime comes from Bukkit's `PLAY_ONE_MINUTE` statistic, recorded in ticks. It is displayed as Minecraft-days using **24,000 played ticks = 1 Minecraft-day**. A player's personal counter advances only while that player is online.
- World Age is tracked by the datapack as completed Minecraft day repetitions. It advances only when at least one player is online. Days skipped by sleeping count as a Minecraft day; time changes made while nobody is online do not add to World Age.
- The datapack stores World Age in the main scoreboard objective `bc_world_age`, fake player `#world_age`. The plugin reads that value for the `/days` GUI.
- On first installation, World Age is initialized from the world's current Minecraft day repetition; thereafter it only advances while players are online.

## Worldborder Challenge datapack

Install the contents of `datapacks/worldborder-challenge/` into the world's `datapacks/worldborder-challenge/` folder, then run `/reload` or restart.

- Overworld and Nether start at 50 blocks; End starts at 250 blocks.
- A new Minecraft day while at least one player is online expands the borders according to `grow.mcfunction`.
- Death shrinks each border by 1 where possible, respecting the minimum sizes.
- Sleeping counts as a day transition.
- AFK handling is intentionally not included in this datapack.

## Daily reward

One claim per 24 hours for the entire server. Default weighted chances:

- Common: 70% — no Expansion
- Rare: 20% — +1 block Expansion
- Epic: 8% — +2 block Expansion
- Mythic: 2% — +3 block Expansion

Expansion is applied only to the configured Overworld and Nether. The End is not changed by this reward.

## Build

Requires Java 25 and Gradle:

```bash
gradle clean build
```

Output: `build/libs/BendemenMC-Core-1.0.0.jar`.

Configuration is stored in `plugins/BendemenMCCore/config.yml`; global cooldown data is stored in `plugins/BendemenMCCore/data.yml`.


## Continuous build

GitHub Actions builds the plugin on pushes to `feature/core-plugin` using Java 25. The compiled JAR is published as the `BendemenMC-Core` workflow artifact.
