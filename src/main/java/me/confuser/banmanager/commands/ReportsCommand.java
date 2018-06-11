package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.*;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.MultiCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class ReportsCommand extends MultiCommandHandler<BanManager> {

  public ReportsCommand() {
    super("reports");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerSubCommand(new AssignSubCommand());
    registerSubCommand(new CloseSubCommand());
    registerSubCommand(new InfoSubCommand());
    registerSubCommand(new ListSubCommand());
    registerSubCommand(new TeleportSubCommand());
    registerSubCommand(new UnassignSubCommand());
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length == 0 && sender instanceof Player) return getCommands().get("list").onCommand(sender, args);

    return super.onCommand(sender, command, commandName, args);
  }
}
