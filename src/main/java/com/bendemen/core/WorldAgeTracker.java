package com.bendemen.core;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public final class WorldAgeTracker implements Listener {
    private final JavaPlugin plugin;
    private final File stateFile;
    private final YamlConfiguration state;
    private World world;
    private Objective ageObjective;
    private long worldAgeDays;
    private long lastGameTime;
    private long lastDayTime;
    private long sleepPendingUntil = -1L;
    private boolean initialized;

    public WorldAgeTracker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "world-age.yml");
        this.state = YamlConfiguration.loadConfiguration(stateFile);
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        saveState();
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled()) return;
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        if (world == null || !event.getBed().getWorld().equals(world)) return;
        // A successful bed entry may cause Minecraft to skip the remaining night.
        sleepPendingUntil = Math.max(sleepPendingUntil, world.getGameTime() + 200L);
    }

    private void tick() {
        if (world == null) {
            String worldName = plugin.getConfig().getString("worlds.overworld", "world");
            world = Bukkit.getWorld(worldName);
            if (world == null) return;
        }

        ensureObjective();
        if (ageObjective == null) return;

        long gameTime = world.getGameTime();
        long dayTime = world.getTime();

        if (!initialized) {
            int scoreboardAge = ageObjective.getScore("#world_age").getScore();
            worldAgeDays = Math.max(0L, state.getLong("days",
                    scoreboardAge > 0 ? scoreboardAge : world.getFullTime() / 24000L));
            lastGameTime = gameTime;
            lastDayTime = dayTime;
            initialized = true;
            publishAge();
            saveState();
            return;
        }

        long elapsedGameTicks = gameTime - lastGameTime;
        boolean naturalDayRollover = lastDayTime >= 23990L && dayTime <= 10L;
        boolean sleptThroughNight = sleepPendingUntil >= gameTime
                && lastDayTime >= 12542L && dayTime <= 100L && dayTime < lastDayTime;

        // Game Time confirms that the server actually ticked. Day Time identifies
        // a completed day-cycle; a successful bed entry identifies the sleep skip.
        if (elapsedGameTicks > 0L && (naturalDayRollover || sleptThroughNight)) {
            worldAgeDays++;
            publishAge();
            saveState();
            if (sleptThroughNight) sleepPendingUntil = -1L;
        }

        if (gameTime > sleepPendingUntil) sleepPendingUntil = -1L;
        lastGameTime = gameTime;
        lastDayTime = dayTime;
    }

    private void ensureObjective() {
        if (ageObjective != null) return;
        Scoreboard scoreboard = Bukkit.getScoreboardManager() == null
                ? null : Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard == null) return;
        ageObjective = scoreboard.getObjective("bc_world_age");
        if (ageObjective == null) {
            ageObjective = scoreboard.registerNewObjective("bc_world_age", "dummy", "World Age");
        }
    }

    private void publishAge() {
        if (ageObjective != null) {
            ageObjective.getScore("#world_age").setScore((int) Math.min(Integer.MAX_VALUE, worldAgeDays));
        }
    }

    private void saveState() {
        state.set("days", worldAgeDays);
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            state.save(stateFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Kon world-age.yml niet opslaan: " + exception.getMessage());
        }
    }
}
