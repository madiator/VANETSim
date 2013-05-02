/**
 *
 */
package edu.usc.anrg.vanetsim.visualize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class LocationPrinter {
  public static void main(String args[]) {
    Configuration config;
    NodeManager nodeManager;
    DBManager dbManager;
    TimeManager timer;

    LocationManager locationManager;
    HashMap<Integer, NodeEntry> locations;
    int[] nodeArray;
    config = new Configuration("mongo");
    config.setDbName("chicagobus");
    config.setCollName("locations");
    config.setHostName("yourhostname.com");
    config.setPort(27017);
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
    for(int nodeIndex = 58; nodeIndex < 60;//nodeManager.getNumNodes();
        nodeIndex++) {
      BufferedWriter out = null;
      try {
        FileWriter fstream = new FileWriter("chicagoLocations"+nodeIndex+".txt");
        out = new BufferedWriter(fstream);

      List<NodeEntry> nodeEntries = locationManager.getNodeEntriesByNodeID(nodeManager.getNodeID(nodeIndex));
      System.out.println(nodeEntries.size());
      // Let us process the nodeEntries
      //List<NodeEntry> prevEntries = new ArrayList<NodeEntry>();

      NodeEntry prevEntry = null;
      for(NodeEntry nodeEntry:nodeEntries) {
        if(prevEntry==null) { //to handle the first iteration.
          prevEntry = nodeEntry;
          LonLat lonlat = nodeEntry.getLonLat();
          //System.out.println((nodeEntry.getTime()/timer.getInterval())+", "+lonlat.getLat()+", "+lonlat.getLon());
          System.out.println(lonlat.getLat()+", "+lonlat.getLon());
          continue;
        }
        if(!prevEntry.getLonLat().equals(nodeEntry.getLonLat())) {
          LonLat lonlat = nodeEntry.getLonLat();
          //System.out.println(((double)nodeEntry.getTime()/timer.getInterval())+", "+lonlat.getLat()+", "+lonlat.getLon());
          System.out.println(lonlat.getLat()+", "+lonlat.getLon());
          out.write(lonlat.getLat()+", "+lonlat.getLon()+"\n");
        }
        prevEntry = nodeEntry;
      }
      System.out.println("--");
      }catch(IOException e) {
        e.printStackTrace();
      }
    }
  }
}

