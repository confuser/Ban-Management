package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerBanData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;
  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;
  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;

  PlayerBanData() {

  }

  public PlayerBanData(PlayerData player, PlayerData actor, String reason) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
  }

  public PlayerBanData(PlayerData player, PlayerData actor, String reason, long expires) {
    this(player, actor, reason);

    this.expires = expires;
  }

  // Only use for imports!
  public PlayerBanData(PlayerData player, PlayerData actor, String reason, long expires, long created) {
    this(player, actor, reason, expires);

    this.created = created;
  }

  public PlayerBanData(int id, PlayerData player, PlayerData actor, String reason, long expires, long created, long updated) {
    this(player, actor, reason, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public PlayerBanData(PlayerBanRecord record) {
    this(record.getPlayer(), record.getPastActor(), record.getReason(), record.getExpired(), record.getPastCreated());
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean equalsBan(PlayerBanData ban) {
    return ban.getReason().equals(this.reason)
            && ban.getExpires() == expires
            && ban.getCreated() == this.created
            && ban.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && ban.getActor().getUUID().equals(this.actor.getUUID());
  }

}
