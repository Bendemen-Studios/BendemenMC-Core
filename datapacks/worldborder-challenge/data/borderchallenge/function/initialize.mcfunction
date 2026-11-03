scoreboard players set #initialized bc_dummy 1

# Overworld and Nether start at 50 blocks.
worldborder center 0 0
worldborder set 50
execute in minecraft:the_nether run worldborder center 0 0
execute in minecraft:the_nether run worldborder set 50

# Keep the central End island clear for the dragon fight.
execute in minecraft:the_end run worldborder center 0 0
execute in minecraft:the_end run worldborder set 250

# Store the current Minecraft day as the initial world age.
execute store result score #last_day_repetition bc_daytime run time query minecraft:day repetition
scoreboard players operation #world_age bc_world_age = #last_day_repetition bc_daytime

tellraw @a [{"text":"BendemenMC ","color":"white","bold":true},{"text":"| ","color":"dark_gray","bold":false},{"text":"Overworld/Nether starten op 50 blokken; de End op 250. World Age en bordergroei tellen alleen wanneer minstens 1 speler online is. Slapen telt mee.","color":"gold","bold":false}]
