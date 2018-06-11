package me.confuser.banmanager.util;

import com.google.common.collect.ImmutableList;
import me.confuser.banmanager.BanManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * Based on UUIDFetcher by evilmidget38
 */
public class UUIDUtils implements Callable<Map<String, UUID>> {

  private static final double PROFILES_PER_REQUEST = 100;
  private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
  private final static JSONParser jsonParser = new JSONParser();
  private final List<String> names;
  private final boolean rateLimiting;

  public UUIDUtils(List<String> names, boolean rateLimiting) {
    this.names = ImmutableList.copyOf(names);
    this.rateLimiting = rateLimiting;
  }

  public UUIDUtils(Set<String> names, boolean rateLimiting) {
    this.names = ImmutableList.copyOf(names);
    this.rateLimiting = rateLimiting;
  }

  public UUIDUtils(List<String> names) {
    this(names, true);
  }

  private static void writeBody(HttpURLConnection connection, String body) throws Exception {
    OutputStream stream = connection.getOutputStream();
    stream.write(body.getBytes());
    stream.flush();
    stream.close();
  }

  private static HttpURLConnection createConnection(String urlStr, String method) throws Exception {
    URL url = new URL(urlStr);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setUseCaches(false);
    connection.setDoInput(true);

    if (method.equals("POST")) connection.setDoOutput(true);
    return connection;
  }

  private static UUID getUUID(String id) {
    return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id
            .substring(16, 20) + "-" + id.substring(20, 32));
  }

  public static byte[] toBytes(UUID uuid) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
    byteBuffer.putLong(uuid.getMostSignificantBits());
    byteBuffer.putLong(uuid.getLeastSignificantBits());
    return byteBuffer.array();
  }

  public static byte[] toBytes(Player player) {
    return toBytes(getUUID(player));
  }

  public static UUID fromBytes(byte[] array) {
    if (array.length != 16) {
      throw new IllegalArgumentException("Illegal byte array length: " + array.length);
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(array);
    long mostSignificant = byteBuffer.getLong();
    long leastSignificant = byteBuffer.getLong();
    return new UUID(mostSignificant, leastSignificant);
  }

  public static UUIDProfile getUUIDOf(String name) throws Exception {
    Map<String, UUID> players = new UUIDUtils(Collections.singletonList(name)).call();

    if (players.isEmpty()) {
      return null;
    }

    Entry<String, UUID> player = players.entrySet().iterator().next();

    return new UUIDProfile(player.getKey(), player.getValue());
  }

  public static String getCurrentName(UUID uuid) throws Exception {
    BanManager.plugin.getLogger().info("Requesting name for " + uuid.toString());
    String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names";

    HttpURLConnection connection = createConnection(url, "GET");

    int status = connection.getResponseCode();

    if (status != 200) throw new Exception("Error retrieving name from " + url);

    JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

    if (array.size() == 0) return null;

    JSONObject jsonProfile = (JSONObject) array.get(array.size() - 1);

    return (String) jsonProfile.get("name");
  }

  public static UUIDProfile getUUIDProfile(String name, long time) throws Exception {
    if (!BanManager.getPlugin().getConfiguration().isOnlineMode())
      return new UUIDProfile(name, createUUID(name));

    BanManager.plugin.getLogger().info("Requesting UUID for " + name + " at " + time);
    String url = "https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + time;

    HttpURLConnection connection = createConnection(url, "GET");

    int status = connection.getResponseCode();

    if (status != 200) throw new Exception("Error retrieving name from " + url);

    JSONObject obj = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

    if (obj.size() == 0) return null;

    return new UUIDProfile((String) obj.get("name"), getUUID((String) obj.get("id")));
  }

  public static UUID getUUID(Player player) {
    if (BanManager.getPlugin().getConfiguration().isOnlineMode()) return player.getUniqueId();

    return createUUID(player.getName());
  }

  public static UUID getUUID(AsyncPlayerPreLoginEvent event) {
    if (BanManager.getPlugin().getConfiguration().isOnlineMode()) return event.getUniqueId();

    return createUUID(event.getName());
  }

  private static UUID createUUID(String s) {
    try {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + s.toLowerCase()).getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  public static UUID getUUID(CommandSender sender) {
    if (sender instanceof Player) {
      return UUIDUtils.getUUID((Player) sender);
    }

    return BanManager.getPlugin().getConsoleConfig().getUuid();
  }

  public Map<String, UUID> call() throws Exception {

    Map<String, UUID> uuidMap = new HashMap<>();
    if (!BanManager.getPlugin().getConfiguration().isOnlineMode()) {
      BanManager.plugin.getLogger().info("Generating offline UUIDs for " + StringUtils.join(names, ','));

      for (String s : names) {
        uuidMap.put(s, createUUID(s));
      }

      return uuidMap;
    }

    BanManager.plugin.getLogger().info("Requesting UUIDs for " + StringUtils.join(names, ','));

    int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
    for (int i = 0; i < requests; i++) {
      HttpURLConnection connection = createConnection(PROFILE_URL, "POST");
      String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
      writeBody(connection, body);
      JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
      for (Object profile : array) {
        JSONObject jsonProfile = (JSONObject) profile;
        String id = (String) jsonProfile.get("id");
        String name = (String) jsonProfile.get("name");
        UUID uuid = UUIDUtils.getUUID(id);
        uuidMap.put(name, uuid);
      }
      if (rateLimiting && i != requests - 1) {
        // Try to avoid rate limit
        Thread.sleep(10000L);
      }
    }
    return uuidMap;
  }
}
