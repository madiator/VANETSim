package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;
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
 * Store the contact times of pairwise nodes.
 * Run once, if needed.
 *
 * @author mahesh
 *
 */
public class BeijingContactTimes {
  DBCollection coll;
  public BeijingContactTimes() {
    Mongo m;
    try {
      m = new Mongo();
      DB db = m.getDB( "beijingfull" );
      coll = db.getCollection("contactTimes");
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
    config.setDbName("beijingfull");
    config.setCollName("locationsNormalized");
    config.setHostName("localhost");
    config.setPort(27017);

    //get some required stuff
    Application.init(config);
    DBManager dbManager = Application.getDBManager();
    NodeManager nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    LocationManager locationManager = new LocationManagerDB(config, true);
    int numNodes = nodeManager.getNumNodes();
    TimeManager timer = Application.getTimeManager();


    /* for each node, for each time slot, find its neighbors
     * and store the beginning and ending contact time.
     */

    for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
      int nodeID = nodeManager.getNodeID(nodeIndex);
      NodeEntry node = dbManager.getNodeEntry(nodeID, timer.getSeconds());

      System.out.println("Processing "+nodeIndex+": "+nodeID);
      timer.reset();

      while(timer.hasNext()) {
        NodeEntry nodeNext = dbManager.getNodeEntry(nodeID, timer.getNextSeconds());
        if(node!=null) {
          if(nodeNext==null) {
            nodeNext = new NodeEntry(node.getNodeID(), node.getLonLat(), timer.getNextSeconds());
          }
          List<ContactEpisode> contactEpisodeList = locationManager.getContactEpisodes(node, nodeNext, timer);
          for(ContactEpisode contactEpisode:contactEpisodeList) {
            // store it!
            storeContactEpisode(nodeID, timer.getSeconds(), contactEpisode);
            System.out.println(nodeID + "," + contactEpisode.getNeighbor().getNodeID() + "-" +
                contactEpisode.getStartTime() + ", " + contactEpisode.getEndTime());
          }
        }
        node = nodeNext;
        timer.inc();
      }
    }

  }
  public void storeContactEpisode(int nodeID, int t, ContactEpisode c) {
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
    BeijingContactTimes bt = new BeijingContactTimes();
    bt.start();
  }
}
