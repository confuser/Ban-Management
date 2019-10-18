package me.confuser.banmanager.common;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class BasePluginTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  protected BanManagerPlugin plugin;

  @Before
  public void setup() throws Exception {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(logger, temporaryFolder.getRoot(), new TestScheduler(), new TestServer());

    plugin.enable();
  }
}