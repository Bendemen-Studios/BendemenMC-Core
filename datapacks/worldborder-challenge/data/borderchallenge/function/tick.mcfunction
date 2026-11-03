# Read the current position within the repeating Minecraft day (0-23999 ticks).
execute store result score #current_day_repetition bc_daytime run time query minecraft:day repetition

# A new day starts when the repeating day timeline wraps back to a lower tick value.
# Count one day only if at least one player is online.
execute if entity @a if score #current_day_repetition bc_daytime < #last_day_repetition bc_daytime run scoreboard players add #world_age bc_world_age 1

# Grow all borders on that same online day transition.
execute if entity @a if score #current_day_repetition bc_daytime < #last_day_repetition bc_daytime run function borderchallenge:grow

# Always update the previous timeline position. With daylight cycle paused while empty,
# this remains unchanged until players return and a new day actually begins.
scoreboard players operation #last_day_repetition bc_daytime = #current_day_repetition bc_daytime

# Initialize a player's stored death count if they have no score yet.
execute as @a unless score @s bc_deaths_old matches -2147483648..2147483647 run scoreboard players operation @s bc_deaths_old = @s bc_deaths

# Shrink once for each newly recorded death.
execute as @a if score @s bc_deaths > @s bc_deaths_old run function borderchallenge:death
execute as @a run scoreboard players operation @s bc_deaths_old = @s bc_deaths
