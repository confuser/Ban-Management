package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class AbortSubCommand extends SubCommand<BanManager> {

  public AbortSubCommand() {
    super("abort");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {


    ContinuingReport report = plugin.getReportManager()
                                    .remove(UUIDUtils.getUUID(sender));

    if (report == null) {
      Message.get("report.error.unknown").sendTo(sender);
    } else {
      Message.get("report.mode.disabled").sendTo(sender);
    }

    return true;
  }

  @Override
  public String getHelp() {
    return "abort";
  }

  @Override
  public String getPermission() {
    return "command.report.abort";
  }
}
