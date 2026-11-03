scoreboard objectives add bc_dummy dummy
scoreboard objectives add bc_deaths deathCount
scoreboard objectives add bc_deaths_old dummy
scoreboard objectives add bc_active_time dummy
scoreboard objectives add bc_daytime dummy
scoreboard objectives add bc_world_age dummy

# Game Time is the source of truth: 24,000 ticks per Minecraft day.
scoreboard players set #ticks_per_day bc_daytime 24000
execute store result score #current_game_time bc_daytime run time query gametime
scoreboard players operation #current_game_day bc_daytime = #current_game_time bc_daytime
scoreboard players operation #current_game_day bc_daytime /= #ticks_per_day bc_daytime

execute unless score #initialized bc_dummy matches 1 run function borderchallenge:initialize

# Migrate an existing installation without resetting its worldborder or death data.
# The World Age now follows total Game Time, and the baseline prevents a fake border
# expansion immediately after a datapack reload.
scoreboard players operation #world_age bc_world_age = #current_game_day bc_daytime
scoreboard players operation #last_game_day bc_daytime = #current_game_day bc_daytime
