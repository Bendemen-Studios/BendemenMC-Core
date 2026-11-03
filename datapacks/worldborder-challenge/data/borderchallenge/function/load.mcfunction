scoreboard objectives add bc_dummy dummy
scoreboard objectives add bc_deaths deathCount
scoreboard objectives add bc_deaths_old dummy
scoreboard objectives add bc_active_time dummy
scoreboard objectives add bc_daytime dummy
scoreboard objectives add bc_world_age dummy

execute unless score #initialized bc_dummy matches 1 run function borderchallenge:initialize

# Migrate worlds that already had the previous datapack installed.
execute unless score #world_age bc_world_age matches 0.. run scoreboard players operation #world_age bc_world_age = #last_day_repetition bc_daytime
