package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.AbortSubCommand;
import me.confuser.banmanager.commands.report.SaveSubCommand;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.MultiCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class ReportCommand extends MultiCommandHandler<BanManager> {

  public ReportCommand() {
    super("report");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerSubCommand(new AbortSubCommand());
    registerSubCommand(new SaveSubCommand());
  }

  @Override
  public void commandNotFound(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    final UUID uuid = UUIDUtils.getUUID(sender);

    if (args.length == 0 && !plugin.getReportManager().has(uuid)) {
      sender.sendMessage(command.getUsage());

      return;
    }

    if (args.length > 0 && plugin.getReportManager().has(uuid)) {
      if (plugin.getReportManager().get(uuid).getPlayer().getName().equalsIgnoreCase(args[0])) {
        this.getCommands().get("abort").onCommand(sender, args);

        return;
      }

      Message.get("report.error.alreadyEnabled")
             .set("player", plugin.getReportManager().get(uuid).getPlayer().getName())
             .sendTo(sender);

      return;
    }

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;

    Player onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getServer().getPlayer(UUID.fromString(playerName));
    } else {
      onlinePlayer = plugin.getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.report.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return;
      }
    } else if (!sender.hasPermission("bm.exempt.override.report") && onlinePlayer.hasPermission("bm.exempt.report")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return;
    }

    final int argsLength = args.length;
    final String reason = CommandUtils.getReason(1, args).getMessage();

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player = CommandUtils.getPlayer(sender, playerName, false);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (plugin.getExemptionsConfig().isExempt(player, "ban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        try {
          if (plugin.getPlayerReportStorage().isRecentlyReported(player)) {
            Message.get("report.error.cooldown").sendTo(sender);
            return;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        if (argsLength == 1) {
          plugin.getReportManager().add(actor, player, isSilent);

          Message.get("report.mode.enabled")
                 .set("player", player.getName())
                 .sendTo(sender);
        } else {
          try {
            PlayerReportData report = new PlayerReportData(player, actor, reason, plugin.getReportStateStorage()
                                                                                        .queryForId(1));
            plugin.getPlayerReportStorage().report(report, isSilent);
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
          }
        }

      }

    });
  }
}
