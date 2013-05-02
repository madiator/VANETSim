package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * The coordinates are likely stored for all possible durations. Normalize
 * it to interval boundaries
 * @author Maheswaran Sathiamoorthy
 *
 */
public class DatasetNormalizer {
  public DatasetNormalizer() {

  }

  public void start() throws UnknownHostException, MongoException {
    Configuration config = new Configuration("mongo");
    //config.setDbName("beijingfull");
    config.setDbName("chicagobus");
    config.setCollName("locations");
    Application.init(config);
    NodeManager nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    DBManager dbManager =  Application.getDBManager();
    int numNodes = nodeManager.getNumNodes();

    Mongo m = new Mongo();
    DB db = m.getDB(config.getDbName());

    DBCollection coll2 = db.getCollection("locationsNormalized");
    TimeManager timer = Application.getTimeManager();

    for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
      int nodeID = nodeManager.getNodeID(nodeIndex);
      System.out.println("Processing "+nodeIndex+": "+nodeID);
      // Get its initial location
      timer.reset();
      NodeEntry nodeInitial = dbManager.getNodeEntry(nodeID, timer.getSeconds(), timer.getNextSeconds());
      if(nodeInitial!=null)
        store(coll2, nodeID, timer.getSeconds(), nodeInitial.getLonLat());

      timer.inc();
      while(timer.hasNext()) {
        NodeEntry nodeThis = dbManager.getNodeEntry(nodeID, timer.getSeconds(), timer.getNextSeconds());
        if(nodeThis!=null) {
          NodeEntry nodeInter = intermediate(nodeInitial, nodeThis, timer.getSeconds());
          store(coll2, nodeID, timer.getSeconds(), nodeInter.getLonLat());
        }

        nodeInitial = nodeThis;
        timer.inc();
      }

    }
  }

  public void store(DBCollection coll, int nodeID, int t, LonLat lonlat) {
    BasicDBObject doc = new BasicDBObject();
    BasicDBObject loc = new BasicDBObject();
    doc.put("nid", nodeID);
    doc.put("t", t);
    loc.put("lon", lonlat.getLon());
    loc.put("lat", lonlat.getLat());
    doc.put("loc", loc);
    coll.insert(doc);
  }

  public NodeEntry intermediate(NodeEntry node1, NodeEntry node2, int t) {

    if(node1==null) {
      return new NodeEntry(node2.getNodeID(), node2.getLonLat(), t);
    }


    LonLat interLonLat = LonLat.getIntermediateLonLat(
        node1.getLonLat(),
        node1.getTime(),
        node2.getLonLat(),
        node2.getTime(), t);
    return new NodeEntry(node1.getNodeID(), interLonLat, t);
  }

  public static void main(String args[]) throws UnknownHostException, MongoException {
    DatasetNormalizer b = new DatasetNormalizer();
    b.start();
  }



}
