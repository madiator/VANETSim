/**
 *
 */
package edu.usc.anrg.vanetsim.visualize;

import java.util.ArrayList;
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

/**
 * @author Maheswaran Sathiamoorthy
 * Will have to run it in rocky, because rocky does
 * not allow remote connections and the macbook pro
 * does not have chicago database!
 */
public class PlotDensityChicago {
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
    String collName = "locationsNormalized";
    config.setCollName(collName);
    config.setNodeCollName("nodes");
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
   // System.out.println("* " +
   //     "* * Initializing the Storage Manager..");
    //storageManager.initializeStorage();
    //System.out.println("* * * Storage Manager done storing files.\n" +
    //    "* * Done initializing the storage manager");
    System.out.println("Using "+collName);
    int[] numMoving = new int[timer.getMaxSlots()];
    long isMobileSum = 0;
    int numPrevEntriesReqd = 5; /* how many time slots do you need for
                        a node to have same GPS so as to declare
                        that it is stationary.*/
    int interval = timer.getInterval();
    for(int nodeIndex = 0; nodeIndex < nodeManager.getNumNodes();
        nodeIndex++) {
      List<NodeEntry> nodeEntries = locationManager.getNodeEntriesByNodeID(nodeManager.getNodeID(nodeIndex));
      List<NodeEntry> cleanedEntries = new ArrayList<NodeEntry>();
      System.out.println(nodeIndex
                          +": "+nodeManager.getNodeID(nodeIndex)
                          +" size = "+nodeEntries.size());
      // first clean the node entries
      NodeEntry prevEntry = null;
      int prevSlot = -1;
      for(NodeEntry node:nodeEntries) {
        if(prevEntry==null) {
          prevEntry = node;
          prevSlot = prevEntry.getTime()/interval;
          cleanedEntries.add(node);
          continue;
        }
        if(node.getTime()>=timer.getMaxSlots()*interval) {
          //done so break;
          break;
        }

        if(node.getTime()<prevEntry.getTime()) {
          System.err.println("Time is decreasing"+prevEntry.getTime()+
              ", "+node.getTime());
          System.exit(-1);
        }
        while(node.getTime()>prevEntry.getTime()+interval) {
          NodeEntry cleanedNode = new NodeEntry(
              prevEntry.getNodeID(),
              prevEntry.getLonLat(),
              prevEntry.getTime()+interval
              );
          cleanedEntries.add(cleanedNode);
          prevEntry = cleanedNode;
        }
        if(node.getTime()!=prevEntry.getTime()+interval) {
          System.err.println("Time not same"+prevEntry.getTime()+
              ", "+node.getTime());
          System.exit(-1);
        } else {
          cleanedEntries.add(node);
          prevEntry = node;
        }
      }
      // Let us process the nodeEntries
      nodeEntries = cleanedEntries;
      System.out.println("\tSize after cleaning = "+nodeEntries.size());
      List<NodeEntry> prevEntries = new ArrayList<NodeEntry>();
      prevEntry = null;

      for(NodeEntry nodeEntry:nodeEntries) {
        if(prevEntries.size()<numPrevEntriesReqd) { //to handle the first iteration.
          prevEntries.add(nodeEntry);
          continue;
        } else {
          //System.out.println("skipping");
        }
        prevEntry = prevEntries.remove(0);
        if(!prevEntry.getLonLat().equals(nodeEntry.getLonLat())) {
          int slot = nodeEntry.getTime()/interval;
          //if(numMoving[slot]==0) { numMoving[slot] = 1;
          if(slot>=numMoving.length) continue;
          numMoving[slot]++;
          isMobileSum++;
          //}
        }
        prevEntries.add(nodeEntry);
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

