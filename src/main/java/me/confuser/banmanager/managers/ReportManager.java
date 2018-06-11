package me.confuser.banmanager.managers;

import me.confuser.banmanager.commands.report.ContinuingReport;
import me.confuser.banmanager.data.PlayerData;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReportManager {

  ConcurrentHashMap<UUID, ContinuingReport> continuingReports = new ConcurrentHashMap<>();

  public ContinuingReport add(PlayerData actor, PlayerData player, boolean silent) {
    return continuingReports.put(actor.getUUID(), new ContinuingReport(actor, player, silent));
  }

  public ContinuingReport get(UUID uuid) {
    return continuingReports.get(uuid);
  }

  public boolean has(UUID uuid) {
    return continuingReports.get(uuid) != null;
  }

  public ContinuingReport remove(UUID uuid) {
    return continuingReports.remove(uuid);
  }
}
