# World Age is synchronized to total Minecraft Game Time.
# One Minecraft day equals 24,000 game ticks.
execute store result score #current_game_time bc_daytime run time query gametime
scoreboard players operation #current_game_day bc_daytime = #current_game_time bc_daytime
scoreboard players operation #current_game_day bc_daytime /= #ticks_per_day bc_daytime

# Keep the displayed World Age exactly synchronized with Game Time.
scoreboard players operation #world_age bc_world_age = #current_game_day bc_daytime

# Grow borders only if a new Game-Time day starts while at least one player is online.
execute if entity @a if score #current_game_day bc_daytime > #last_game_day bc_daytime run function borderchallenge:grow

# Always record the new day, even when nobody is online, so missed days do not cause catch-up growth.
scoreboard players operation #last_game_day bc_daytime = #current_game_day bc_daytime

# Initialize a player's stored death count if they have no score yet.
execute as @a unless score @s bc_deaths_old matches -2147483648..2147483647 run scoreboard players operation @s bc_deaths_old = @s bc_deaths

# Shrink once for each newly recorded death.
execute as @a if score @s bc_deaths > @s bc_deaths_old run function borderchallenge:death
execute as @a run scoreboard players operation @s bc_deaths_old = @s bc_deaths
