package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.storage.mysql.ByteArray;

import java.security.NoSuchAlgorithmException;

@DatabaseTable
public class PlayerAppealData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass =
          ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  @DatabaseField
  @Getter
  private PunishmentType type;

  @DatabaseField(index = true, canBeNull = false, uniqueIndex = false)
  @Getter
  @Setter
  private int type_id;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false)
  @Getter
  @Setter
  private ReportState state;

  @DatabaseField(index = true, canBeNull = true, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  @Setter
  private PlayerData assignee;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;

  PlayerAppealData() {

  }

}
