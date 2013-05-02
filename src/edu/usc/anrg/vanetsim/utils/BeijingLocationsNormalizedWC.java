/**
 *
 */
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
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class BeijingLocationsNormalizedWC {
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
    DBCollection collLocationsNormalized =
        db.getCollection("locationsNormalized");

    HashMap<Integer, Integer> wcNodes = new HashMap<Integer, Integer>();
    int[] wcNodesArray = WCNodes.wcNodes;
    for(int i = 0; i < wcNodesArray.length; i++) {
      wcNodes.put(wcNodesArray[i], i);
    }

    Application.init(config);


    NodeManager nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);

    LocationManager locationManager = new LocationManagerDB(config, true);
    // I could have as well used the dbManager instead of the location manager.

    /* For each node get all entries and store them
     * in the table.
     */
    for(int nodeID:wcNodesArray) {
      List<NodeEntry> nodeEntries = locationManager.getNodeEntriesByNodeID(nodeID);
      for(NodeEntry nodeEntry:nodeEntries) {
        // Store the node entry
        storeNodeEntry(nodeEntry, collLocationsNormalized);
      }
    }
  }

  public static void storeNodeEntry(NodeEntry nodeEntry, DBCollection coll) {
    BasicDBObject doc = new BasicDBObject();
    BasicDBObject loc = new BasicDBObject();
    doc.put("nid", nodeEntry.getNodeID());
    doc.put("t", nodeEntry.getTime());
    loc.put("lon", nodeEntry.getLon());
    loc.put("lat", nodeEntry.getLat());
    doc.put("loc", loc);
    coll.insert(doc);
  }
}
