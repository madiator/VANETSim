/**
 *
 */
package edu.usc.anrg.vanetsim;

import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.StorageManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * A Simple Simulator, which relies on using precomputed
 * contact times. <p>
 * There is a single file which is stored into the nodes.<p>
 * If erasure coding is not to be used, then alpha nodes
 * will be randomly picked to store the file.
 * If erasure coding is used, a (alpha*k, k) code is used.
 * Here (n, k) code means a file is split into k blocks
 * and then encoded to n blocks. The actual encoding and
 * decoding is not done.<p>
 * In this case, k is determined depending on the file size
 * and the block size.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class SimpleSimulator {
  Configuration config;
  NodeManager nodeManager;
  DBManager dbManager;
  TimeManager timer;
  StorageManager storageManager;
  LocationManager locationManager;
  HashMap<Integer, NodeEntry> locations;

  private final int[] nodeArray;
  private final int kCoding, blockSize;

  public SimpleSimulator() {
    config = new Configuration("mongo");
    config.setDbName("beijingWC_NID");
    config.setCollName("locationsNormalized");
    int fileSize = 500;
    config.setFileSize(fileSize);
    blockSize = 1;
    kCoding = fileSize/blockSize;
    int alpha = 30; //redundancy of the storage

    boolean storageType = StorageManager.UNCODED;
    config.setBlockSize(blockSize);
    config.setkCoding(kCoding);
    config.setCapacity(500);
    config.setNumFiles(1);
    config.setStorageRedundancy(alpha);
    config.setCodedStorage(storageType);
    config.setUncodedDistributed(false);
    if(storageType==StorageManager.UNCODED) {
      config.setUncodedDistributed(false);
    }

    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    dbManager = Application.getDBManager();
    timer = Application.getTimeManager();
    locationManager = new LocationManagerDB(config, true);

    nodeArray = nodeManager.getNodeArray();
    System.out.println(
        "nodeArray size = "+nodeArray.length+"," +
        		" first element = "+nodeArray[0]);

    storageManager = new StorageManager(config);
    Application.setStorageManager(storageManager);
    System.out.println("* " +
        "* * Initializing the Storage Manager..");
    storageManager.initializeStorage();
    System.out.println("* * * Storage Manager done storing files.\n" +
        "* * Done initializing the storage manager");
  }

  public void simulateContacts() {
    int bandwidthLimitation = 0;
    List<ContactEpisode> contactEpisodes =
      dbManager.getEasyContactEpisodes(timer.getTimeSlot());

    for(ContactEpisode contactEpisode:contactEpisodes) {
      NodeEntry sinkNode = contactEpisode.getSink();
      NodeEntry destNode = contactEpisode.getNeighbor();

      bandwidthLimitation =
          (int) (contactEpisode.getContactDuration()/blockSize);
      int fileToDownload = 0;

      storageManager.copyFile(sinkNode,
              destNode,
              fileToDownload,
              bandwidthLimitation/2);

     storageManager.copyFile(destNode,
           sinkNode,
           fileToDownload,
           bandwidthLimitation/2);
    }
  }

  public double getStorageStatus(
      HashMap<Integer, NodeEntry> locationsTemp, int whichFile) {
    double totalStorage = 0;
    double numSatistiedNodes = 0;
    NodeEntry nodeEntry;
    for(int nodeID:nodeArray) {
      nodeEntry = locationsTemp.get(nodeID);
      if(nodeEntry==null)
        continue;
      double storageStatus =
          storageManager.getStorageStatus(nodeEntry, whichFile);
      if(storageStatus>=kCoding)
        numSatistiedNodes++;
      nodeEntry.setStorageStatus(storageStatus);
      totalStorage+= storageStatus;
    }
    System.out.println(totalStorage+", "+numSatistiedNodes);
    return totalStorage;
  }

  public void startSim() {
    timer.reset();
    while(timer.hasNext()) {
      locations =
          locationManager.getNodeEntriesByTimeMap(timer.getSeconds());
      System.out.print(timer.getTimeSlot()+", ");
      double totalStored = getStorageStatus(locations, 0);

      simulateContacts();
      timer.inc();
    }

  }
  public static void main(String args[]) {
    SimpleSimulator sim = new SimpleSimulator();
    sim.startSim();
  }

}
