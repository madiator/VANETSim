package edu.usc.anrg.vanetsim.utils;

import java.util.HashMap;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.NodeEntry;

public class BeijingContactTimesMemory {
  String dbName;

  public BeijingContactTimesMemory() {
    dbName = "beijingfull";
  }
  public void start() {
    // Setup
    Configuration config = new Configuration("mongo");
    config.setDbName(dbName);
    config.setCollName("locationsNormalized");
    //config.setHostName();
    //config.setPort(27017);

    //get some required stuff
    Application.init(config);
    DBManager dbManager = Application.getDBManager();
    NodeManager nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    LocationManager locationManager = new LocationManagerDB(config, true);
    int numNodes = nodeManager.getNumNodes();
    TimeManager timer = Application.getTimeManager();
    double bigRange = locationManager.getBigRange();


    int startSlot = 211;
    while(timer.getTimeSlot()<startSlot)
      timer.inc();


    HashMap<Integer, NodeEntry> nodesInThisIntervalMap = null;
    if(timer.hasNext()) {
    nodesInThisIntervalMap =
      locationManager.getNodeEntriesByTimeMap(timer.getSeconds());
    }


    long startTimeMilli = timer.getCurrentMillis();
    long dur1;
    while(timer.hasNext()) {
      System.out.println("Time Slot = "+timer.getTimeSlot()+"\n------------------");
      startTimeMilli = timer.getCurrentMillis();
      HashMap<Integer, NodeEntry> nodesInNextIntervalMap =
        locationManager.getNodeEntriesByTimeMap(timer.getNextSeconds());
      dur1 = timer.getCurrentMillis();
      System.out.println("Time to load nodes = "+(dur1 - startTimeMilli)+
          ", length of the list(hashmap) = "+nodesInNextIntervalMap.size());

      //  processing the values from the current interval
      for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
        int nid = nodeManager.getNodeID(nodeIndex);
        NodeEntry sinkNodeCurrent = nodesInThisIntervalMap.get(nid);

        if(sinkNodeCurrent==null)
          continue;

        // Find its neighbors
        locationManager.getNeighbors(sinkNodeCurrent, bigRange, timer);

      }
      nodesInThisIntervalMap = nodesInNextIntervalMap;
      System.out.println("Total time = "+(timer.getCurrentMillis() - startTimeMilli));
      timer.inc();
    }
  }

  public static void main(String args[]){
    BeijingContactTimes2 bt = new BeijingContactTimes2();
    bt.start();
  }
}
