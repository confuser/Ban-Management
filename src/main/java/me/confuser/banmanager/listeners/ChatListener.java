package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.ContinuingReport;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.UUID;

public class ChatListener extends Listeners<BanManager> {

  public void onPlayerChat(AsyncPlayerChatEvent event) {
    UUID uuid = UUIDUtils.getUUID(event.getPlayer());

    ContinuingReport report = plugin.getReportManager().get(uuid);

    if (report != null) {
      event.setCancelled(true);

      // @TODO Change if causes CME
      for (Player player : event.getRecipients()) {
        if (plugin.getReportManager().has(uuid)) event.getRecipients().remove(player);
      }

      if (report.getReason().size() == plugin.getConfiguration().getMaxReportLines()) {
        Message.get("report.mode.maxLines")
               .sendTo(event.getPlayer());
      } else if (event.getMessage().startsWith("#delete")) {
        int lineNumber;

        try {
          lineNumber = Integer.parseInt(event.getMessage().replace("#delete", ""));
        } catch (NumberFormatException e) {
          return;
        }

        if (report.getReason().size() > lineNumber && lineNumber > 0) return;
        report.getReason().remove(lineNumber - 1);

        Message.get("report.mode.removeLine").set("line", lineNumber).sendTo(event.getPlayer());

        return;
      } else {
        report.getReason().add(event.getMessage());

        Message.get("report.mode.addLine")
               .set("message", event.getMessage())
               .sendTo(event.getPlayer());
      }

      return;
    }

    if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
      if (plugin.getPlayerWarnStorage().isMuted(uuid)) {
        PlayerWarnData warning = plugin.getPlayerWarnStorage().getMute(uuid);

        if (warning.getReason().toLowerCase().equals(event.getMessage().toLowerCase())) {
          plugin.getPlayerWarnStorage().removeMute(uuid);
          Message.get("warn.player.disallowed.removed").sendTo(event.getPlayer());
        } else {
          Message.get("warn.player.disallowed.header").sendTo(event.getPlayer());
          Message.get("warn.player.disallowed.reason").set("reason", warning.getReason()).sendTo(event.getPlayer());
        }

        event.setCancelled(true);
      }

      return;
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(uuid);

    if (mute.hasExpired()) {
      try {
        plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.getRecipients().clear();
      event.getRecipients().add(event.getPlayer());
      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("mute.player.broadcast")
                               .set("message", event.getMessage())
                               .set("displayName", event.getPlayer().getDisplayName())
                               .set("player", event.getPlayer().getName())
                               .set("playerId", uuid.toString())
                               .set("reason", mute.getReason())
                               .set("actor", mute.getActor().getName());

    CommandUtils.broadcast(broadcast.toString(), "bm.notify.muted");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("playerId", uuid.toString())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName());

    event.getPlayer().sendMessage(message.toString());
  }

  public void onIpChat(AsyncPlayerChatEvent event) {
    if (!plugin.getIpMuteStorage().isMuted(event.getPlayer().getAddress().getAddress())) {
      return;
    }

    IpMuteData mute = plugin.getIpMuteStorage().getMute(event.getPlayer().getAddress().getAddress());

    if (mute.hasExpired()) {
      try {
        plugin.getIpMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.getRecipients().clear();
      event.getRecipients().add(event.getPlayer());
      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("muteip.ip.broadcast")
                               .set("message", event.getMessage())
                               .set("displayName", event.getPlayer().getDisplayName())
                               .set("player", event.getPlayer().getName())
                               .set("playerId", UUIDUtils.getUUID(event.getPlayer()).toString())
                               .set("reason", mute.getReason())
                               .set("actor", mute.getActor().getName());

    CommandUtils.broadcast(broadcast.toString(), "bm.notify.mutedip");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("muteip.ip.disallowed");
    } else {
      message = Message.get("tempmuteip.ip.disallowed")
                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", event.getPlayer().getDisplayName())
           .set("player", event.getPlayer().getName())
           .set("playerId", UUIDUtils.getUUID(event.getPlayer()).toString())
           .set("reason", mute.getReason())
           .set("actor", mute.getActor().getName())
           .set("ip", IPUtils.toString(mute.getIp()));

    event.getPlayer().sendMessage(message.toString());
  }
}
