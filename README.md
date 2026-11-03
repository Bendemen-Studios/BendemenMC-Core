# BendemenMC Core

Paper plugin for BendemenMC.

## Features

- `/dailyreward` (aliases: `/daily`, `/dailybonus`) opens a server-wide daily reward chest GUI.
- `/days` opens a top-10 Minecraft play-days leaderboard, personal statistics, and the Overworld age.

## Minecraft play-days

Player playtime comes from Bukkit's `PLAY_ONE_MINUTE` statistic, which is recorded in ticks. It is displayed as Minecraft-days using **24,000 played ticks = 1 Minecraft-day**. This is active Minecraft playtime, not 24 hours of wall-clock time.

The World Age is shown separately from the configured Overworld's `World#getFullTime()`, divided by 24,000.

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
