package com.bendemen.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class BendemenMCCore extends JavaPlugin implements Listener {
    private static final long DEFAULT_COOLDOWN_SECONDS = 86400L;
    private static final long MINECRAFT_DAY_TICKS = 24000L;
    private static final String PREFIX = "&f&lBendemenMC &r&8|";
    private static final String DAYS_GUI_TITLE = PREFIX + " &6Days Leaderboard";

    private final Random random = new Random();
    private NamespacedKey actionKey;
    private File dataFile;
    private YamlConfiguration data;
    private boolean rolling = false;

    private record Reward(String key, String name, int weight, int expansion, Material material) {}
    private record PlayerDays(OfflinePlayer player, long playTicks) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
        actionKey = new NamespacedKey(this, "daily_action");
        dataFile = new File(getDataFolder(), "data.yml");
        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("dailyreward") == null || getCommand("days") == null) {
            getLogger().severe("Commands ontbreken in plugin.yml; plugin wordt uitgeschakeld.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("BendemenMC Core is ingeschakeld.");
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadData() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Kon data.yml niet aanmaken: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Kon data.yml niet opslaan: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(PREFIX + " &6Dit commando kan alleen door een speler worden gebruikt."));
            return true;
        }
        if (command.getName().equalsIgnoreCase("days")) {
            openDaysGui(player);
        } else if (command.getName().equalsIgnoreCase("dailyreward")) {
            openRewardGui(player);
        }
        return true;
    }

    private void openDaysGui(Player viewer) {
        String title = color(getConfig().getString("days-gui.title", DAYS_GUI_TITLE));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < 54; slot++) inv.setItem(slot, filler);

        List<PlayerDays> leaderboard = new ArrayList<>();
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() == null) continue;
            long ticks;
            try {
                ticks = offline.getStatistic(Statistic.PLAY_ONE_MINUTE);
            } catch (IllegalArgumentException ignored) {
                ticks = 0L;
            }
            if (ticks > 0L) leaderboard.add(new PlayerDays(offline, ticks));
        }
        leaderboard.sort(Comparator.comparingLong(PlayerDays::playTicks).reversed()
                .thenComparing(entry -> entry.player().getName() == null ? "" : entry.player().getName(),
                        String.CASE_INSENSITIVE_ORDER));

        int[] topSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            PlayerDays entry = leaderboard.get(i);
            String playerName = entry.player().getName();
            if (playerName == null || playerName.isBlank()) playerName = "Unknown";

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skull = (SkullMeta) head.getItemMeta();
            if (skull != null) {
                skull.setOwningPlayer(entry.player());
                skull.setDisplayName(color("&6#" + (i + 1) + " &6" + playerName));
                skull.setLore(List.of(
                        color("&6Minecraft-speeldagen: &6" + formatMinecraftDays(entry.playTicks())),
                        color("&6Speeltijd: &6" + formatTickDuration(entry.playTicks()))
                ));
                head.setItemMeta(skull);
            }
            inv.setItem(topSlots[i], head);
        }

        long personalTicks = viewer.getStatistic(Statistic.PLAY_ONE_MINUTE);
        inv.setItem(4, playerHead(viewer, "&6&lJOUW STATISTIEKEN", List.of(
                "&6Minecraft-speeldagen: &6" + formatMinecraftDays(personalTicks),
                "&6Totale speeltijd: &6" + formatTickDuration(personalTicks),
                "&6Eén Minecraft-dag = 24.000 speelticks"
        )));

        World overworld = Bukkit.getWorld(getConfig().getString("worlds.overworld", "world"));
        long worldAgeDays = getOnlineWorldAgeDays();
        String worldAgeDisplay = worldAgeDays < 0L
                ? "&6Datapack niet geladen"
                : "&6" + worldAgeDays + " Minecraft-dagen";
        inv.setItem(49, item(Material.CLOCK, "&6&lWORLD AGE", List.of(
                "&6Wereld: &6" + (overworld == null ? "Niet gevonden" : overworld.getName()),
                "&6Leeftijd: " + worldAgeDisplay,
                "&6Minecraft-dagen tellen alleen bij online spelers"
        )));
        inv.setItem(31, item(Material.GOLD_INGOT, "&6&lTOP 10 SPELERS", List.of(
                "&6Ranglijst op basis van Minecraft-speeltijd.",
                "&6Eén Minecraft-dag = 24.000 speelticks."
        )));
        viewer.openInventory(inv);
    }

    private long getOnlineWorldAgeDays() {
        if (Bukkit.getScoreboardManager() == null) return -1L;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("bc_world_age");
        if (objective == null) return -1L;
        return Math.max(0L, objective.getScore("#world_age").getScore());
    }

    private ItemStack playerHead(Player player, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(color(name));
            List<String> lines = new ArrayList<>();
            for (String line : lore) lines.add(color(line));
            meta.setLore(lines);
            head.setItemMeta(meta);
        }
        return head;
    }

    private void openRewardGui(Player player) {
        String title = color(getConfig().getString("gui.title", PREFIX + " &6Daily Reward"));
        Inventory inv = Bukkit.createInventory(null, 27, title);
        ItemStack glass = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < 27; slot++) inv.setItem(slot, glass);

        long remaining = remainingCooldownMillis();
        if (remaining <= 0L) {
            inv.setItem(13, taggedItem(Material.CHEST,
                    getConfig().getString("gui.claim-name", "&6&lCLAIM DAILY REWARD"),
                    getConfig().getStringList("gui.claim-lore"), "claim"));
        } else {
            long seconds = (remaining + 999L) / 1000L;
            inv.setItem(13, item(Material.CLOCK,
                    getConfig().getString("gui.cooldown-name", "&6&lREWARD OP COOLDOWN"),
                    List.of("&6Volgende serverclaim over:", "&6" + formatDuration(seconds))));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getView().getTitle().equals(color(getConfig().getString("days-gui.title", DAYS_GUI_TITLE)))) {
            event.setCancelled(true);
            return;
        }
        if (!event.getView().getTitle().equals(color(getConfig().getString("gui.title", PREFIX + " &6Daily Reward")))) {
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() != 13 || rolling) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String action = clicked.getItemMeta().getPersistentDataContainer()
                .get(actionKey, PersistentDataType.STRING);
        if (!"claim".equals(action)) return;

        long remaining = remainingCooldownMillis();
        if (remaining > 0L) {
            player.sendMessage(color(PREFIX + " &6De server daily reward is nog niet beschikbaar. &6("
                    + formatDuration((remaining + 999L) / 1000L) + ")"));
            player.closeInventory();
            return;
        }

        List<Reward> configuredRewards = loadRewards();
        if (configuredRewards.isEmpty()) {
            player.closeInventory();
            player.sendMessage(color(PREFIX + " &6Er zijn geen geldige rewards ingesteld in config.yml."));
            return;
        }

        data.set("last-claim", System.currentTimeMillis());
        saveData();
        rolling = true;
        startRoll(player, configuredRewards);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(color(getConfig().getString("gui.title", PREFIX + " &6Daily Reward")))
                || event.getView().getTitle().equals(color(getConfig().getString("days-gui.title", DAYS_GUI_TITLE)))) {
            event.setCancelled(true);
        }
    }

    private void startRoll(Player player, List<Reward> rewards) {
        Inventory inv = player.getOpenInventory().getTopInventory();

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    rolling = false;
                    cancel();
                    return;
                }
                if (ticks < 30) {
                    Reward preview = rewards.get(random.nextInt(rewards.size()));
                    inv.setItem(13, item(preview.material(), "&6" + preview.name(), List.of("&6Randomizer...")));
                    player.updateInventory();
                    ticks++;
                    return;
                }

                Reward won = weightedReward(rewards);
                if (won == null) won = rewards.get(0);
                applyExpansion(won.expansion());
                inv.setItem(13, item(won.material(), won.name(), List.of(
                        "&6Zeldzaamheid: &6" + rarityName(won.key()),
                        "&6Expansion: &6+" + won.expansion() + " blok(ken)"
                )));
                player.updateInventory();
                player.sendMessage(color(PREFIX + " &6Je daily reward: " + won.name()
                        + " &6(" + rarityName(won.key()) + ")"));

                if (won.expansion() > 0) {
                    Bukkit.broadcastMessage(color(PREFIX + " &6" + player.getName()
                            + " heeft " + won.name() + " gewonnen! &6Overworld en Nether zijn uitgebreid."));
                } else {
                    Bukkit.broadcastMessage(color(PREFIX + " &6" + player.getName()
                            + " heeft de daily reward geopend, maar kreeg geen Expansion. &6(Common)"));
                }

                rolling = false;
                cancel();
            }
        }.runTaskTimer(this, 0L, 2L);
    }

    private void applyExpansion(int amount) {
        if (amount <= 0) return;

        String overworldName = getConfig().getString("worlds.overworld", "world");
        String netherName = getConfig().getString("worlds.nether", "world_nether");
        World overworld = Bukkit.getWorld(overworldName);
        World nether = Bukkit.getWorld(netherName);

        if (overworld != null) {
            WorldBorder border = overworld.getWorldBorder();
            border.setSize(border.getSize() + amount);
        } else {
            getLogger().warning("Overworld '" + overworldName + "' niet gevonden; expansion niet toegepast.");
        }

        if (nether != null) {
            WorldBorder border = nether.getWorldBorder();
            border.setSize(border.getSize() + amount);
        } else {
            getLogger().warning("Nether '" + netherName + "' niet gevonden; expansion niet toegepast.");
        }
    }

    private List<Reward> loadRewards() {
        List<Reward> result = new ArrayList<>();
        ConfigurationSection section = getConfig().getConfigurationSection("rewards");
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            ConfigurationSection reward = section.getConfigurationSection(key);
            if (reward == null) continue;

            int weight = reward.getInt("weight", 0);
            int expansion = reward.getInt("expansion", 0);
            String displayName = reward.getString("display-name", key);
            Material material = Material.matchMaterial(reward.getString("material", "CHEST"));
            if (weight <= 0 || material == null) {
                getLogger().warning("Reward '" + key + "' heeft een ongeldige weight of material en wordt overgeslagen.");
                continue;
            }
            result.add(new Reward(key, color(displayName), weight, Math.max(0, expansion), material));
        }
        return result;
    }

    private Reward weightedReward(List<Reward> rewards) {
        int total = rewards.stream().mapToInt(Reward::weight).sum();
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        int cursor = 0;
        for (Reward reward : rewards) {
            cursor += reward.weight();
            if (roll < cursor) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    private long remainingCooldownMillis() {
        long last = data.getLong("last-claim", 0L);
        long cooldown = getConfig().getLong("cooldown-seconds", DEFAULT_COOLDOWN_SECONDS) * 1000L;
        return Math.max(0L, last + cooldown - System.currentTimeMillis());
    }

    private String rarityName(String key) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "common" -> "Common";
            case "rare" -> "Rare";
            case "epic" -> "Epic";
            case "mythic" -> "Mythic";
            default -> key;
        };
    }

    private String formatMinecraftDays(long ticks) {
        return String.format(Locale.ROOT, "%.2f", Math.max(0L, ticks) / (double) MINECRAFT_DAY_TICKS);
    }

    private String formatTickDuration(long ticks) {
        long totalSeconds = Math.max(0L, ticks) / 20L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        return hours + "u " + minutes + "m";
    }

    private String formatLongPlaytime(long ticks) {
        long totalSeconds = Math.max(0L, ticks) / 20L;
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        return days + "d " + hours + "u " + minutes + "m";
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs);
    }

    private ItemStack taggedItem(Material material, String name, List<String> lore, String action) {
        ItemStack stack = item(material, name, lore);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> colored = new ArrayList<>();
            for (String line : lore) colored.add(color(line));
            meta.setLore(colored);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
