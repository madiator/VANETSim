/**
 *
 */
package edu.usc.anrg.vanetsim;

import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.MongoManager;
import edu.usc.anrg.vanetsim.manager.SQLManager;
import edu.usc.anrg.vanetsim.manager.StorageManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;

/**
 * A global class that initiates the system and can be useful
 * in passing around some frequently required objects
 * @author Maheswaran Sathiamoorthy
 */
public class Application {
  private static TimeManager timeManager;
  private static Configuration config;
  private static DBManager dbManager;
  private static StorageManager storageManager;

  public static void init(Configuration config2) {
    config = config2;
    dbManager = createDBManager(config);
    timeManager = new TimeManager();
  }

  /**
   * Create the Database manager based on the configuration.
   * @param config
   * @return the created database manager.
   */
  private static DBManager createDBManager(Configuration config) {
    String dbType = config.dbType;
    DBManager dbManager = null;
    if(dbType.equals("mysql")) {
      dbManager = new SQLManager(config);
    }else if(dbType.equals("mongo")) {
      dbManager = new MongoManager(config);
    }
    return dbManager;
  }

  public static TimeManager getTimeManager() {
    return timeManager;
  }

  public static Configuration getConfiguration() {
    return config;
  }

  public static DBManager getDBManager() {
    return dbManager;
  }

  public static StorageManager getStorageManager() {
    return storageManager;
  }

  public static void setStorageManager(StorageManager storageManager) {
    Application.storageManager = storageManager;
  }
}
