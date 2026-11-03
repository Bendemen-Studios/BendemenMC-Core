scoreboard players set #initialized bc_dummy 1

# Overworld and Nether start at 50 blocks.
worldborder center 0 0
worldborder set 50
execute in minecraft:the_nether run worldborder center 0 0
execute in minecraft:the_nether run worldborder set 50

# Keep the central End island clear for the dragon fight.
execute in minecraft:the_end run worldborder center 0 0
execute in minecraft:the_end run worldborder set 250

# Initialize World Age from total Game Time (24,000 ticks per Minecraft day).
scoreboard players set #ticks_per_day bc_daytime 24000
execute store result score #current_game_time bc_daytime run time query gametime
scoreboard players operation #current_game_day bc_daytime = #current_game_time bc_daytime
scoreboard players operation #current_game_day bc_daytime /= #ticks_per_day bc_daytime
scoreboard players operation #world_age bc_world_age = #current_game_day bc_daytime
scoreboard players operation #last_game_day bc_daytime = #current_game_day bc_daytime

tellraw @a [{"text":"BendemenMC ","color":"white","bold":true},{"text":"| ","color":"dark_gray","bold":false},{"text":"World Age en bordergroei zijn gesynchroniseerd met Game Time. De dag-/nachtcyclus loopt altijd door, ook zonder spelers.","color":"gold","bold":false}]
