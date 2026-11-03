# Detect day changes, including days skipped by sleeping.
execute store result score #current_day_repetition bc_daytime run time query minecraft:day repetition

# Count Minecraft days only while at least one player is online.
scoreboard players operation #day_delta bc_daytime = #current_day_repetition bc_daytime
scoreboard players operation #day_delta bc_daytime -= #last_day_repetition bc_daytime
execute if entity @a if score #day_delta bc_daytime matches 1.. run scoreboard players operation #world_age bc_world_age += #day_delta bc_daytime

# Grow borders once when a new day starts while a player is online.
execute if score #current_day_repetition bc_daytime > #last_day_repetition bc_daytime if entity @a run function borderchallenge:grow
scoreboard players operation #last_day_repetition bc_daytime = #current_day_repetition bc_daytime

# Initialize a player's stored death count if they have no score yet.
execute as @a unless score @s bc_deaths_old matches -2147483648..2147483647 run scoreboard players operation @s bc_deaths_old = @s bc_deaths

# Shrink once for each newly recorded death.
execute as @a if score @s bc_deaths > @s bc_deaths_old run function borderchallenge:death
execute as @a run scoreboard players operation @s bc_deaths_old = @s bc_deaths
