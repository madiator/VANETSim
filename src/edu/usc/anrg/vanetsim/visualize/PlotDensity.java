package edu.usc.anrg.vanetsim.visualize;

import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.NodeEntry;

public class PlotDensity {
  public static void main(String args[]) {
    Configuration config;
    NodeManager nodeManager;
    DBManager dbManager;
    TimeManager timer;

    LocationManager locationManager;
    HashMap<Integer, NodeEntry> locations;
    int[] nodeArray;
    config = new Configuration("mongo");
    config.setDbName("beijingfull");
    config.setCollName("locationsNormalized");
    config.setNodeCollName("nodes1000");
    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    //dbManager = Application.getDBManager();
    timer = Application.getTimeManager();
    locationManager = new LocationManagerDB(config, true);
    nodeArray = nodeManager.getNodeArray();
    System.out.println("nodeArray size = "+nodeArray.length+", first element = "+nodeArray[0]);
    //storageManager = new StorageManager(config);
    //Application.setStorageManager(storageManager);
    System.out.println("* " +
        "* * Initializing the Storage Manager..");
    //storageManager.initializeStorage();
    //System.out.println("* * * Storage Manager done storing files.\n" +
    //    "* * Done initializing the storage manager");

    int[] numMoving = new int[timer.getMaxSlots()];
    long isMobileSum = 0;
    int numPrevEntriesReqd = 5; /* how many time slots do you need for
                        a node to have same GPS so as to declare
                        that it is stationary.*/
    for(int nodeIndex = 0; nodeIndex < nodeManager.getNumNodes();
        nodeIndex++) {
      List<NodeEntry> nodeEntries = locationManager.getNodeEntriesByNodeID(nodeManager.getNodeID(nodeIndex));
      System.out.println(nodeEntries.size());
      // Let us process the nodeEntries
      //List<NodeEntry> prevEntries = new ArrayList<NodeEntry>();
      NodeEntry prevEntry = null;

      for(NodeEntry nodeEntry:nodeEntries) {
        if(prevEntry==null) { //to handle the first iteration.
          prevEntry = nodeEntry;
          continue;
        }
        if(!prevEntry.getLonLat().equals(nodeEntry.getLonLat())) {
          numMoving[nodeEntry.getTime()/timer.getInterval()]++;
          isMobileSum++;
        }
        prevEntry = nodeEntry;
      }
    }
    for(int num:numMoving) {
      System.out.print(num+", ");
    }
    System.out.println();
    // Now let us also calculate the average duration that nodes are moving.
    System.out.println((double)timer.getInterval()*isMobileSum/nodeManager.getNumNodes());
  }
}
