package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SaveSubCommand extends SubCommand<BanManager> {

  public SaveSubCommand() {
    super("save");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    final ContinuingReport continuingReport = plugin.getReportManager()
                                    .remove(sender instanceof Player ? ((Player) sender).getUniqueId() : plugin.getConsoleConfig().getUuid());

    if (continuingReport == null) {
      Message.get("report.error.unknown").sendTo(sender);

      return true;
    }

    if (continuingReport.getReason().size() == 0) {
      Message.get("report.error.missingReason").sendTo(sender);

      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
      @Override
      public void run() {
        final String reason = StringUtils.join(continuingReport.getReason(), "\n"); // Stick to Unix separators for WebUI

        try {
          PlayerReportData report = new PlayerReportData(continuingReport.getPlayer(), continuingReport.getActor(),
                  reason, plugin.getReportStateStorage().queryForId(1));

          plugin.getPlayerReportStorage().report(report, continuingReport.isSilent());
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "save";
  }

  @Override
  public String getPermission() {
    return "command.report.save";
  }
}
