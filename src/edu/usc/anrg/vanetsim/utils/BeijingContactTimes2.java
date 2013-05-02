package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * Run once, if needed.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class BeijingContactTimes2 {
  DBCollection coll;
  String dbName;
  public BeijingContactTimes2() {
    Mongo m;
    dbName = "beijingfull";
    try {
      m = new Mongo();
      DB db = m.getDB( dbName );
      coll = db.getCollection("contactTimes3");
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MongoException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

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
    List<NodeEntry> neighborEntryList;

    int startSlot = 1000;
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
      int entriesStored = 0;
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
        NodeEntry sinkNodeNext = nodesInNextIntervalMap.get(nid);
        if(sinkNodeCurrent==null)
          continue;

        // Find its neighbors
        neighborEntryList = locationManager.getNeighbors(sinkNodeCurrent, bigRange, timer);
        if(neighborEntryList.size() > 0) {
          for(NodeEntry destNodeCurrent:neighborEntryList) {
            // find the contact time
            NodeEntry destNodeNext = nodesInNextIntervalMap.get(destNodeCurrent.getNodeID());
            ContactEpisode contactEpisode =
              locationManager.getContactEpisode(sinkNodeCurrent,
                  sinkNodeNext,
                  destNodeCurrent,
                  destNodeNext,
                  timer.getSeconds(), timer.getNextSeconds());
            if(contactEpisode.getContactDuration()>0) {
              storeContactEpisode(nid, timer.getSeconds(), contactEpisode);
              entriesStored++;
            }
          }
        }
      }
      nodesInThisIntervalMap = nodesInNextIntervalMap;
      System.out.println(entriesStored+" entries stored." +
      		" Time for finding neighbors and storing = "+ (timer.getCurrentMillis() - dur1));
      System.out.println("Total time = "+(timer.getCurrentMillis() - startTimeMilli));
      timer.inc();
    }
  }

  public void storeContactEpisode(int nodeID, int t, ContactEpisode c) {
    //store only if nid < neighbor id
    if(nodeID >= c.getNeighbor().getNodeID())
      return;

    BasicDBObject doc = new BasicDBObject();
    doc.put("nid", nodeID);
    doc.put("t", t);
    doc.put("neighbor", c.getNeighbor().getNodeID());
    doc.put("starttime", c.getStartTime());
    doc.put("endtime", c.getEndTime());
    doc.put("startdistance", c.getStartDistance());
    if(Math.abs(c.getStartDistance()-c.getEndDistance())>=0.01)
      doc.put("enddistance", c.getEndDistance());

    coll.insert(doc);
  }


  public static void main(String args[]) throws UnknownHostException, MongoException {
    BeijingContactTimes2 bt = new BeijingContactTimes2();
    bt.start();
  }
}
