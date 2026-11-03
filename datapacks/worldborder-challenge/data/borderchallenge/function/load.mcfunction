scoreboard objectives add bc_dummy dummy
scoreboard objectives add bc_deaths deathCount
scoreboard objectives add bc_deaths_old dummy
scoreboard objectives add bc_active_time dummy
scoreboard objectives add bc_daytime dummy
scoreboard objectives add bc_world_age dummy

# First-time initialization creates the starting borders and seeds World Age.
execute unless score #initialized bc_dummy matches 1 run function borderchallenge:initialize

# Use the current persisted World Age as the baseline after every datapack reload.
# This prevents a reload from causing a false border expansion.
scoreboard players operation #last_game_day bc_daytime = #world_age bc_world_age
