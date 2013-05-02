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
 * A simple sink simulator. There is a single sink
 * which when meets nodes tries to download a particular file.<p>
 * Refer to {@link SimpleSimulator} for details on coding.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class SinkSimulator {
  public static void main(String args[]) {
    Configuration config;
    NodeManager nodeManager;
    DBManager dbManager;
    TimeManager timer;
    //StorageManager storageManager;

    LocationManager locationManager;
    HashMap<Integer, NodeEntry> locations;
    final int[] nodeArray;
    final int kCoding, blockSize;

    // Initialization
    config = new Configuration("mongo");
    config.setDbName("beijingWC_NID");
    config.setCollName("locationsNormalized");
    int fileSize = 1000;
    config.setFileSize(fileSize);
    blockSize = 1;
    kCoding = fileSize/blockSize;
    int alpha = 25;
    int numTrials = 20;

    boolean storageType = StorageManager.CODED;
    boolean distributionType = true;
    System.out.println("Is coding being used? - "+storageType);
    config.setBlockSize(blockSize);
    config.setkCoding(kCoding);
    config.setCapacity(10000);
    config.setNumFiles(1);
    config.setStorageRedundancy(alpha);
    config.setCodedStorage(storageType);
    config.setUncodedDistributed(false);
    if(storageType==StorageManager.UNCODED) {
      config.setUncodedDistributed(distributionType);
      System.out.println("Distributed everywhere in" +
      		" uncoded = "+distributionType);
    }

    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    dbManager = Application.getDBManager();
    timer = Application.getTimeManager();

    MetricCollector metricCollector = new MetricCollector(numTrials);

    //server = new Server(config, 1);
    locationManager = new LocationManagerDB(config, true);

    nodeArray = nodeManager.getNodeArray();
    System.out.println("nodeArray size = "+
          nodeArray.length+", first element = "+nodeArray[0]);

    for(int trialNum = 0; trialNum < numTrials; trialNum++) {
      System.out.println(trialNum);
      StorageManager storageManager = new StorageManager(config);
      Application.setStorageManager(storageManager);
      System.out.println("* " +
          "* * Initializing the Storage Manager..");

      storageManager.initializeStorage();

      System.out.println("* * * Storage Manager done storing files.\n" +
          "* * Done initializing the storage manager\n"+
          "* * Total storage used = "+storageManager.getTotalStorageUsed());

      int fileToDownload = 0;
      boolean doneDownloading = false;
      int numNull = 0;
      int sinkNodeIndex = trialNum+20;//nodeManager.getNewNodeIndex();
      int sinkID = nodeManager.getNodeID(sinkNodeIndex);
      System.out.println("Sink ID = "+sinkID);
      timer.reset();
      NodeEntry sinkNode;// = locationManager.getNodeEntry(sinkID, timer);
      while(timer.hasNext() && (!doneDownloading)) {
        double percentStored =
            100
              *storageManager.getStorageStatus(sinkNodeIndex, fileToDownload)
              /fileSize;
        metricCollector.recordStorageStatus(trialNum, percentStored);
        System.out.println(timer.getTimeSlot()+", "+percentStored);
        int bandwidthLimitation = 0;
        List<ContactEpisode> contactEpisodes =
          dbManager.getEasyContactEpisodes(timer.getTimeSlot());

        for(ContactEpisode contactEpisode:contactEpisodes) {
          int newSinkNodeID = contactEpisode.getSink().getNodeID();
          if(newSinkNodeID!=sinkID)
            continue;

          sinkNode = contactEpisode.getSink();
          NodeEntry destNode = contactEpisode.getNeighbor();

          bandwidthLimitation =
              (int) (contactEpisode.getContactDuration()/blockSize);

          storageManager.copyFile(sinkNode,
                  destNode,
                  fileToDownload,
                  2*bandwidthLimitation);

        }
        timer.inc();
      }
    }
    metricCollector.showStorageResults();
  }
}