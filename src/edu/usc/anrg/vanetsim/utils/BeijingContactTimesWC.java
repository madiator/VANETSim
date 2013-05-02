package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;
import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;
/**
 * Generate contac times for the Well Connected (WC) dataset.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class BeijingContactTimesWC {

  public static void main(String args[]) throws UnknownHostException, MongoException {

    /* If useIndexes is true, store only the indexes,
     * otherwise store the nid.
     * For example, instead of storing 1271, 1290 etc. store
     * 0, 1, .. as the nodes
     *
     */
    boolean useIndexes = false;

    Configuration config = new Configuration("mongo");
    config.setDbName("beijingfull");
    config.setCollName("locationsNormalized"); //

    Mongo m = new Mongo();
    DB db;
    if(useIndexes)
      db = m.getDB( "beijingWC");
    else
      db = m.getDB( "beijingWC_NID");

    DBCollection collNodes = db.getCollection("nodes");
    DBCollection collContactTimes = db.getCollection("contactTime");

    HashMap<Integer, Integer> wcNodes = new HashMap<Integer, Integer>();
    int[] wcNodesArray = WCNodes.wcNodes;
    for(int i = 0; i < wcNodesArray.length; i++) {
      wcNodes.put(wcNodesArray[i], i);
    }

    Application.init(config);


    NodeManager nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    int numNodes = nodeManager.getNumNodes();

    //Store the nodes in the "nodes" collection/table.

    for(int i = 0; i < wcNodesArray.length; i++) {
      BasicDBObject doc = new BasicDBObject();
      if(useIndexes)
        doc.put("nid", i);
      else
        doc.put("nid", wcNodesArray[i]);

      collNodes.insert(doc);
    }

    LocationManager locationManager = new LocationManagerDB(config, true);
    TimeManager timer = Application.getTimeManager();
    HashMap<Integer, NodeEntry> nodesInThisIntervalMap = null;
    if(timer.hasNext()) {
    nodesInThisIntervalMap =
      locationManager.getNodeEntriesByTimeMap(timer.getSeconds());
    }

    double contactDurationsSum = 0;
    while(timer.hasNext()) {

      HashMap<Integer, NodeEntry> nodesInNextIntervalMap =
        locationManager.getNodeEntriesByTimeMap(timer.getNextSeconds());

      //  processing the values from the current interval
      for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
        int nid = nodeManager.getNodeID(nodeIndex);
        if(!wcNodes.containsKey(nid))
          continue;

        NodeEntry sinkNodeCurrent = nodesInThisIntervalMap.get(nid);
        NodeEntry sinkNodeNext = nodesInNextIntervalMap.get(nid);
        if(sinkNodeCurrent==null)
          continue;
        if(sinkNodeNext==null) {
          sinkNodeNext = new NodeEntry(sinkNodeCurrent.getNodeID(),
              sinkNodeCurrent.getLonLat(),
              timer.getNextSeconds());
        }
        for(int destNodeIndex = 0; destNodeIndex < numNodes; destNodeIndex++) {
          int destNodeID = nodeManager.getNodeID(destNodeIndex);
          if((!wcNodes.containsKey(destNodeID))||(nid==destNodeID))
            continue;
          NodeEntry destNodeCurrent = nodesInThisIntervalMap.get(destNodeID);
          NodeEntry destNodeNext = nodesInNextIntervalMap.get(destNodeID);

          if(destNodeCurrent==null)
            continue;

          if(destNodeNext==null) {
            destNodeNext = new NodeEntry(destNodeCurrent.getNodeID(),
                destNodeCurrent.getLonLat(),
                timer.getNextSeconds());
          }

          ContactEpisode contactEpisode =
              locationManager.getContactEpisode(sinkNodeCurrent,
                  sinkNodeNext,
                  destNodeCurrent,
                  destNodeNext,
                  timer.getSeconds(), timer.getNextSeconds());

          /*
           * Enable the below if you want
           *
          comprehensiveDisplay(sinkNodeCurrent,
              sinkNodeNext,
              destNodeCurrent,
              destNodeNext,
              contactEpisode,
              timer.getSeconds());
           */

          int v1 = wcNodes.get(nid);
          int v2 = wcNodes.get(destNodeID);

          if(v1<v2) {
            if(useIndexes) {
              storeWCContacts(v1, timer.getTimeSlot(), v2,
                  contactEpisode.getContactDuration(),
                  collContactTimes);
            } else {
              storeWCContacts(nid, timer.getTimeSlot(), destNodeID,
                  contactEpisode.getContactDuration(),
                  collContactTimes);
            }
            contactDurationsSum += contactEpisode.getContactDuration();
          }
        } // done for_loop of dest node
      } //done for_loop of sink

      nodesInThisIntervalMap = nodesInNextIntervalMap;
      timer.inc();
    }
    System.out.println("sum = "+contactDurationsSum);
  }

  public static void storeWCContacts(int sink,
      int timer,
      int dest,
      double contact,
      DBCollection coll
      ) {
    System.out.println(sink+","+timer+","+dest+","+contact);
    BasicDBObject  doc = new BasicDBObject();
    doc.put("sink", sink);
    doc.put("timer", timer);
    doc.put("dest", dest);
    doc.put("contact", contact);
    coll.insert(doc);
  }
  public static void displayContact(int sink,
      int timer,
      int dest,
      double contact
      ) {
    System.out.println(sink+","+timer+","+dest+","+contact);
  }
  public static void comprehensiveDisplay(
      NodeEntry sinkNodeCurrent,
      NodeEntry sinkNodeNext,
      NodeEntry destNodeCurrent,
      NodeEntry destNodeNext,
      ContactEpisode contactEpisode,
      int seconds) {

    if(contactEpisode.getContactDuration()>0) {
      System.out.println(seconds+"\n"
          +sinkNodeCurrent.getNodeID()+": ("
          +sinkNodeCurrent.getLon()+", "
          +sinkNodeCurrent.getLat()+")\n"
          +sinkNodeNext.getLon()+", "
          +sinkNodeNext.getLat()+")\n"
          +destNodeCurrent.getNodeID()+": ("
          +destNodeCurrent.getLon()+", "
          +destNodeCurrent.getLat()+")\n"
          +destNodeNext.getLon()+", "
          +destNodeNext.getLat()+")\nContact = "
          +contactEpisode.getContactDuration());
    }
  }
}
