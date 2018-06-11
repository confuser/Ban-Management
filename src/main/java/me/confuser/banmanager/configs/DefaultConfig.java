package me.confuser.banmanager.configs;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.configs.Config;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DefaultConfig extends Config<BanManager> {

  @Getter
  private DatabaseConfig localDb;
  @Getter
  private DatabaseConfig globalDb;
  @Getter
  private TimeLimitsConfig timeLimits;
  @Getter
  private HashSet<String> mutedBlacklistCommands;
  @Getter
  private HashSet<String> softMutedBlacklistCommands;
  @Getter
  private boolean duplicateIpCheckEnabled = true;
  @Getter
  private HashSet<Long> bypassPlayerIps;
  @Getter
  private boolean kickLoggingEnabled = false;
  @Getter
  private boolean debugEnabled = false;
  @Getter
  private long warningCooldown;
  @Getter
  private long reportCooldown;
  @Getter
  private WarningActionsConfig warningActions;
  @Getter
  private boolean displayNotificationsEnabled = true;
  @Getter
  private boolean onlineMode = true;
  @Getter
  private boolean checkForUpdates = false;
  @Getter
  private boolean offlineAutoComplete = true;
  @Getter
  private boolean punishAlts = false;
  @Getter
  private boolean denyAlts = false;
  @Getter
  private HashMap<String, CleanUp> cleanUps;
  @Getter
  private int maxOnlinePerIp = 0;
  @Getter
  private int maxMultiaccountsRecently = 0;
  @Getter
  private long multiaccountsTime = 300L;
  @Getter
  private HooksConfig hooksConfig;
  @Getter
  private boolean broadcastOnSync = false;
  @Getter
  private boolean checkOnJoin = false;
  @Getter
  private boolean createNoteReasons = false;
  @Getter
  private boolean warningMutesEnabled = false;
  @Getter
  private boolean logIpsEnabled = true;
  @Getter
  private EventPriority chatPriority;
  @Getter
  private int maxReportLines;

  public DefaultConfig() {
    super("config.yml");
  }

  @Override
  public void afterLoad() {
    localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));

    if (conf.getConfigurationSection("databases.external") != null) {
      convertToGlobal();
    }

    globalDb = new GlobalDatabaseConfig(conf.getConfigurationSection("databases.global"));
    timeLimits = new TimeLimitsConfig(conf.getConfigurationSection("timeLimits"));
    duplicateIpCheckEnabled = conf.getBoolean("duplicateIpCheck", true);
    onlineMode = conf.getBoolean("onlineMode", true);
    checkForUpdates = conf.getBoolean("checkForUpdates", false);
    offlineAutoComplete = conf.getBoolean("offlineAutoComplete", true);

    bypassPlayerIps = new HashSet<>();
    for (String ip : conf.getStringList("bypassDuplicateChecks")) {
      bypassPlayerIps.add(IPUtils.toLong(ip));
    }

    reportCooldown = conf.getLong("reportCooldown", 0);
    warningCooldown = conf.getLong("warningCooldown", 0);
    warningActions = new WarningActionsConfig(conf.getConfigurationSection("warningActions"));
    kickLoggingEnabled = conf.getBoolean("logKicks", false);
    debugEnabled = conf.getBoolean("debug", false);
    displayNotificationsEnabled = conf.getBoolean("displayNotifications", true);
    punishAlts = conf.getBoolean("punishAlts", false);
    denyAlts = conf.getBoolean("denyAlts", false);

    cleanUps = new HashMap<>(6);
    for (String type : conf.getConfigurationSection("cleanUp").getKeys(false)) {
      cleanUps.put(type, new CleanUp(conf.getInt("cleanUp." + type)));
    }

    maxOnlinePerIp = conf.getInt("maxOnlinePerIp", 0);
    maxReportLines = conf.getInt("reports.maxLines", 20);
    maxMultiaccountsRecently = conf.getInt("maxMultiaccountsRecently", 0);
    multiaccountsTime = conf.getLong("multiaccountsTime", 300L);

    hooksConfig = new HooksConfig(conf.getConfigurationSection("hooks"));

    broadcastOnSync = conf.getBoolean("broadcastOnSync", false);
    checkOnJoin = conf.getBoolean("checkOnJoin", false);
    createNoteReasons = conf.getBoolean("createNoteReasons", false);
    warningMutesEnabled = conf.getBoolean("warningMute", true);
    logIpsEnabled = conf.getBoolean("logIps", true);

    mutedBlacklistCommands = new HashSet<>();
    softMutedBlacklistCommands = new HashSet<>();

    try {
      chatPriority = EventPriority.valueOf(conf.getString("chatPriority", "NORMAL").toUpperCase());
    } catch (Exception e) {
      chatPriority = EventPriority.NORMAL;
      plugin.getLogger().warning("Invalid chatPriority option, using normal priority");
    }

    // Run this after startup to ensure all aliases are found
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

      @Override
      public void run() {
        plugin.getLogger().info("The following commands are blocked whilst muted:");
        handleBlockedCommands(conf.getStringList("mutedCommandBlacklist"), mutedBlacklistCommands);

        plugin.getLogger().info("The following commands are blocked whilst soft muted:");
        handleBlockedCommands(conf.getStringList("softMutedCommandBlacklist"), softMutedBlacklistCommands);
      }

    });
  }

  private void convertToGlobal() {
    plugin.getLogger().info("Converting external connection details to global");

    ConfigurationSection section = conf.getConfigurationSection("databases.external");

    conf.set("databases.global.enabled", section.getBoolean("enabled"));
    conf.set("databases.global.host", section.getString("host"));
    conf.set("databases.global.port", section.getInt("port"));
    conf.set("databases.global.name", section.getString("name"));
    conf.set("databases.global.user", section.getString("user"));
    conf.set("databases.global.password", section.getString("password"));
    conf.set("databases.global.maxConnections", section.getInt("maxConnections"));
    conf.set("databases.global.leakDetection", section.getInt("leakDetection"));

    // Tables
    conf.set("databases.global.tables.playerBans", section.getString("tables.playerBans"));
    conf.set("databases.global.tables.playerUnbans", section.getString("tables.playerUnbans"));
    conf.set("databases.global.tables.playerMutes", section.getString("tables.playerMutes"));
    conf.set("databases.global.tables.playerUnmutes", section.getString("tables.playerUnmutes"));
    conf.set("databases.global.tables.playerNotes", section.getString("tables.playerNotes"));
    conf.set("databases.global.tables.ipBans", section.getString("tables.ipBans"));
    conf.set("databases.global.tables.ipUnbans", section.getString("tables.ipUnbans"));

    conf.set("databases.external", null);
    save();

    plugin.getLogger().info("Converted external connection details to global");
  }

  private void handleBlockedCommands(List<String> blocked, HashSet<String> set) {
    for (String cmd : blocked) {
      set.add(cmd);
      StringBuilder infoBuilder = new StringBuilder(cmd);

      // Check for aliases
      PluginCommand command = plugin.getServer().getPluginCommand(cmd);
      if (command == null) {
        plugin.getLogger().info(cmd);
        continue;
      }

      infoBuilder.append(" - ");

      if (!set.contains(command.getName())) {
        set.add(command.getName());
        infoBuilder.append(command.getName()).append(' ');
      }

      for (String aliasCmd : command.getAliases()) {
        infoBuilder.append(aliasCmd).append(' ');
        set.add(aliasCmd);
        // Block the annoying /plugin:cmd too
        set.add(command.getPlugin().getDescription().getName().toLowerCase() + ":" + aliasCmd);
      }

      plugin.getLogger().info(infoBuilder.toString());
    }
  }

  @Override
  public void onSave() {

  }

  public boolean isBlockedCommand(String cmd) {
    return mutedBlacklistCommands.contains(cmd);
  }

  public boolean isSoftBlockedCommand(String cmd) {
    return softMutedBlacklistCommands.contains(cmd);
  }

}
