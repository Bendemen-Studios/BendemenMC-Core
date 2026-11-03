# World Age is maintained by the Core plugin using both Game Time and Day Time.
# A day is recorded at a natural day-cycle rollover or after a successful sleep skip.
# The plugin persists the counter across restarts.

# Grow only when a new World Age day is recorded while at least one player is online.
execute if entity @a if score #world_age bc_world_age > #last_game_day bc_daytime run function borderchallenge:grow

# Always consume the new day, including days that began while nobody was online.
scoreboard players operation #last_game_day bc_daytime = #world_age bc_world_age

# Initialize a player's stored death count if they have no score yet.
execute as @a unless score @s bc_deaths_old matches -2147483648..2147483647 run scoreboard players operation @s bc_deaths_old = @s bc_deaths

# Shrink once for each newly recorded death.
execute as @a if score @s bc_deaths > @s bc_deaths_old run function borderchallenge:death
execute as @a run scoreboard players operation @s bc_deaths_old = @s bc_deaths
