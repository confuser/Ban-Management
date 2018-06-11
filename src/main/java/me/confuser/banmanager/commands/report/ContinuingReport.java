package me.confuser.banmanager.commands.report;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerData;

import java.util.ArrayList;

public class ContinuingReport {

  @Getter
  private PlayerData actor;

  @Getter
  private PlayerData player;

  @Getter
  private ArrayList<String> reason = new ArrayList<>();

  @Getter
  private boolean silent;

  public ContinuingReport(PlayerData actor, PlayerData player, boolean silent) {
    this.player = player;
    this.actor = actor;
    this.silent = silent;
  }
}
