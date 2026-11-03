# Overworld and Nether never shrink below 50 blocks.
execute in minecraft:overworld store result score #border_size bc_dummy run worldborder get
execute if score #border_size bc_dummy matches 51.. in minecraft:overworld run worldborder add -1

execute in minecraft:the_nether store result score #border_size bc_dummy run worldborder get
execute if score #border_size bc_dummy matches 51.. in minecraft:the_nether run worldborder add -1

# End never shrinks below 250 blocks, keeping the central island usable for the dragon fight.
execute in minecraft:the_end store result score #border_size bc_dummy run worldborder get
execute if score #border_size bc_dummy matches 251.. in minecraft:the_end run worldborder add -1

tellraw @a [{"text":"BendemenMC ","color":"white","bold":true},{"text":"| ","color":"dark_gray"},{"text":"Een speler is gestorven! Borders zijn kleiner geworden waar toegestaan. Minimum: Overworld/Nether 50, End 250.","color":"gold"}]
