package edu.usc.anrg.vanetsim.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * Manage all aspects of the MongoDB database.
 * Uses {@link Configuration}
 * @author Maheswaran Sathiamoorthy
 *
 */
public class MongoManager extends DBManager {
  Mongo m;
  DB db;
  DBCollection coll, nodeColl, contactTimeColl;
  DBCursor cur;
  private final String DEFAULTCOLLNAME = "chicagotest"; //default collection name
  private final String COLLNAME;
  double earthRadius;

  public MongoManager(Configuration config) {
    this.COLLNAME = config.getCollName();
    this.earthRadius = config.getEarthRadius();
    try {
      if((config.getHostName()!=null)&&(config.getPort()!=0))
        m = new Mongo(config.getHostName(), config.getPort());
      else
        m = new Mongo();
      db = m.getDB(config.getDbName());
      coll = db.getCollection(config.getCollName());
      nodeColl = db.getCollection(config.getNodeCollName());
      contactTimeColl = db.getCollection("contactTime");
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  public MongoManager(String collname) {
    this.COLLNAME = collname;
    try {
      m = new Mongo();
      db = m.getDB( "db" );
      coll = db.getCollection(COLLNAME);
      nodeColl = db.getCollection("nodes");
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public NodeEntry getOneNodeEntry(int nid) {
    BasicDBObject query = new BasicDBObject();
    query.put("nid", nid);
    cur = coll.find(query);

    BasicDBObject x = (BasicDBObject) cur.next().get("loc");
    double lon = (Double) x.get("lon");
    double lat = (Double) x.get("lat");
    int t = (Integer) cur.curr().get("t");
    NodeEntry node = new NodeEntry(nid, new LonLat(lon, lat), t);
    return node;
  }

  @Override
  public DateTime getFirstDateTime() {
    DBCursor cur = coll.find(QueryBuilder.start().put("startingtime").exists(true).get());
    if(cur.hasNext()) {
      Date d = (Date) cur.next().get("startingtime");
      return new DateTime(d);
    }else
      return null;
  }

  @Override
  public DateTime getLastDateTime() {
    DBCursor cur = coll.find(QueryBuilder.start().put("endingtime").exists(true).get());
    if(cur.hasNext()) {
      Date d = (Date) cur.next().get("endingtime");
      return new DateTime(d);
    }else
      return null;
  }

  @Override
  public List<Integer> getAllNodesList() {
    DBCursor nodeCur = nodeColl.find();
    List<Integer> nodeList = new ArrayList<Integer>();
    while(nodeCur.hasNext()) {
      int nodeID = (Integer) nodeCur.next().get("nid");
      nodeList.add(new Integer(nodeID));
    }
    return nodeList;
  }

  public List<Integer> getAllNodesListFromOriginal() {
    BasicDBObject query = new BasicDBObject();
    query.put("distinct", COLLNAME);
    query.put("key", "nid");
    CommandResult cmdResult = db.command(query);
    List<Integer> nodeList = new ArrayList<Integer>();
    BasicDBList r = (BasicDBList) cmdResult.get("values");
    Iterator<Object> iterator = r.iterator();
    while(iterator.hasNext()) {
      int nodeID = (Integer) iterator.next();
      nodeList.add(new Integer(nodeID));
    }
    return nodeList;
  }

  @Override
  public NodeEntry getNodeEntry(int nid, int t) {
    BasicDBObject query = new BasicDBObject();
    query.put("nid", nid);
    query.put("t", t);
    //System.out.println(query.toString());
    cur = coll.find(query);
    if(cur.size()==0)
      return null;

    double lon = 0, lat = 0;
    while(cur.hasNext()) {
      BasicDBObject x = (BasicDBObject) cur.next().get("loc");
      lon = (Double) x.get("lon");
      lat = (Double) x.get("lat");

    }
    //so you get the last entry
    NodeEntry node = new NodeEntry(nid, new LonLat(lon, lat), t);

    return node;
  }

  @Override
  public List<NodeEntry> getNodeEntries(int nid) {
    BasicDBObject query = new BasicDBObject();
    query.put("nid", nid);
    int t = 0;
    List<NodeEntry> nodeEntryList = new ArrayList<NodeEntry>();
    //System.out.println(query.toString());
    cur = coll.find(query);


    double lon = 0, lat = 0;
    while(cur.hasNext()) {
      BasicDBObject x = (BasicDBObject) cur.next().get("loc");
      lon = (Double) x.get("lon");
      lat = (Double) x.get("lat");
      t = (Integer) cur.curr().get("t");
      NodeEntry node = new NodeEntry(nid, new LonLat(lon, lat), t);
      nodeEntryList.add(node);
    }
    return nodeEntryList;
  }

  @Override
  public NodeEntry getNodeEntry(int nid, int t1, int t2) {
    BasicDBObject query = new BasicDBObject();
    query.put("nid", nid);
    BasicDBObject tQuery = new BasicDBObject();
    tQuery.put("$gte", t1);
    tQuery.put("$lt", t2);
    query.put("t", tQuery);
    //System.out.println(query.toString());
    cur = coll.find(query);
    if(cur.size()==0)
      return null;

    double lon = 0, lat = 0;
    int t = t1;
    while(cur.hasNext()) {
      //Date d = (Date)cur.next().get("timestamp");
      t = (Integer) cur.next().get("t");
      BasicDBObject x = (BasicDBObject) cur.curr().get("loc");
      lon = (Double) x.get("lon");
      lat = (Double) x.get("lat");

    }
    //so you get the last entry
    NodeEntry node = new NodeEntry(nid, new LonLat(lon, lat), t);

    return node;
  }

  /**
   * Find out the list of nodes between DateTimes d1 and d2
   * Also, when multiple entries for the same node are found,
   * use only the last entry.
   */
  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t1, int t2) {
    BasicDBObject query = new BasicDBObject();
    BasicDBObject tQuery = new BasicDBObject();
    tQuery.put("$gte", t1);
    tQuery.put("$lt", t2);
    query.put("t", tQuery);
    cur = coll.find(query);
    BasicDBObject sortQuery = new BasicDBObject();
    sortQuery.put("nid", 1);
    cur.sort(sortQuery);
    HashSet<Integer> hs = new HashSet<Integer>();
    List<NodeEntry> nodeEntryList = new ArrayList<NodeEntry>();
    while(cur.hasNext()) {
      int nid = (Integer) cur.next().get("nid");
      if(hs.add(nid)) {
        // the node was not already added. So add it to the list.
        BasicDBObject x = (BasicDBObject) cur.curr().get("loc");
        NodeEntry node = new NodeEntry(nid,
            new LonLat((Double) x.get("lon"), (Double) x.get("lat")),
            t1);
        nodeEntryList.add(node);
      }
    }
    return nodeEntryList;
  }

  @Override
  public HashMap<Integer, NodeEntry> getNodeEntriesByTimeAsHashMap(
      int t, HashMap<Integer, NodeEntry> locations) {

    //HashMap<Integer, NodeEntry> nodeHashMap = new HashMap<Integer, NodeEntry>();
    BasicDBObject query = new BasicDBObject();
    query.put("t", t);
    cur = coll.find(query);

    while(cur.hasNext()) {
      int nid = (Integer) cur.next().get("nid");
      BasicDBObject x = (BasicDBObject) cur.curr().get("loc");
      NodeEntry node = new NodeEntry(nid,
            new LonLat((Double) x.get("lon"), (Double) x.get("lat")), t);

      locations.put(nid, node);
    }
    return locations;
  }

  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t) {

    BasicDBObject query = new BasicDBObject();
    query.put("t", t);
    cur = coll.find(query);
    HashSet<Integer> hs = new HashSet<Integer>();
    List<NodeEntry> nodeEntryList = new ArrayList<NodeEntry>();
    while(cur.hasNext()) {
      int nid = (Integer) cur.next().get("nid");
      if(hs.add(nid)) {
        // the node was not already added. So add it to the list.
        BasicDBObject x = (BasicDBObject) cur.curr().get("loc");
        NodeEntry node = new NodeEntry(nid,
            new LonLat((Double) x.get("lon"), (Double) x.get("lat")),
            t);
        nodeEntryList.add(node);
      }
    }
    return nodeEntryList;
  }


  public void testNeighbors(NodeEntry node, double range, DateTime d1, DateTime d2) {
    double lon = node.getLon();
    double lat = node.getLat();
    double maxDistance = range/earthRadius;  //in radians
    String cmd = "{ \"geoNear\" : \""+COLLNAME+"\" , \"near\" : { \"lon\" : "+lon+" , \"lat\" : "+lat+"} , \"spherical\" : true , \"maxDistance\" : "+maxDistance+" , \"query\" : { \"timestamp\" : { \"$gte\" : ISODate(\"2009-01-05T16:08:01.000Z\") , \"$lt\" : ISODate(\"2009-01-05T16:08:31.000Z\")}}}";
    db.command(cmd);
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, double range, int t) {
    return getNeighborsCommon(node, getNeighborCommand(node.getLon(), node.getLat(), range, t));
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, double range, int t1, int t2) {
    return getNeighborsCommon(node, getNeighborCommand(node.getLon(), node.getLat(), range, t1, t2));
  }

  public List<NodeEntry> getNeighborsCommon(NodeEntry node, BasicDBObject cmd) {
    CommandResult cmdResult = db.command(cmd);
    BasicDBList r = (BasicDBList) cmdResult.get("results");
    Iterator<Object> iterator = r.iterator();
    List<NodeEntry> list = new ArrayList<NodeEntry>();
    HashSet<Integer> hs = new HashSet<Integer>();
    while(iterator.hasNext()) {
      BasicDBObject result = (BasicDBObject) iterator.next();
      BasicDBObject obj = (BasicDBObject) result.get("obj");
      int nid = (Integer) obj.get("nid");
      // ignore self
      //System.out.println(nid);
      if(nid==node.getNodeID())
        continue;
      if(hs.add(nid)) {
        BasicDBObject loc = (BasicDBObject) obj.get("loc");
        int t = (Integer) obj.get("t");
        NodeEntry nodeTemp = new NodeEntry(nid, new LonLat((Double) loc.get("lon"), (Double)loc.get("lat")), t);
        nodeTemp.setDistance((Double)result.get("dis")*earthRadius); //so in meters
        list.add(nodeTemp);
      }
    }
    return list;
  }

  private BasicDBObject getNeighborCommand(
      double lon, double lat, double range, int t1, int t2) {
    //range is in meters

    double maxDistance = range/earthRadius;  //in radians
    BasicDBObject query = new BasicDBObject();
    query.put("geoNear", COLLNAME);
    BasicDBObject point = new BasicDBObject();
    point.put("lon", lon);
    point.put("lat", lat);
    query.put("near", point);
    query.put("spherical",true);
    query.put("maxDistance", maxDistance);

    query.put("query",
        QueryBuilder.start().put("t").greaterThanEquals(t1).lessThan(t2).get());
    //System.out.println(query.toString());

    return query;

    /*
     * The following should be faster but just doesn't seem to work
     *
    String cmd = "{ \"geoNear\" : \""+COLLNAME+"\" , \"near\" : { \"lon\" : "+lon+" , \"lat\" : "+lat+"} , \"spherical\" : true , \"maxDistance\" : "+maxDistance+" , \"query\" : { \"t\" : { \"$gte\" : "+t1+" , \"$lt\" : "+t2+"}}}";
    return cmd;
     */
  }

  public BasicDBObject getNeighborCommand(
      double lon, double lat, double range, int t) {
    //range is in meters

    double maxDistance = range/earthRadius;  //in radians
    BasicDBObject query = new BasicDBObject();
    query.put("geoNear", COLLNAME);
    BasicDBObject point = new BasicDBObject();
    point.put("lon", lon);
    point.put("lat", lat);
    query.put("near", point);
    query.put("spherical",true);
    query.put("maxDistance", maxDistance);

    query.put("query",
        QueryBuilder.start().put("t").is(t).get());

    return query;

  }

  @Override
  public List<ContactEpisode> getEasyContactEpisodes(int timer) {
    BasicDBObject query = new BasicDBObject();
    query.put("timer", timer);
    cur = contactTimeColl.find(query);
    List<ContactEpisode> contactEpisodeList = new ArrayList<ContactEpisode>();
    while(cur.hasNext()) {
      int sink = (Integer) cur.next().get("sink");
      int dest = (Integer) cur.curr().get("dest");
      double contactDuration = (Double) cur.curr().get("contact");
      ContactEpisode contactEpisode =
          new ContactEpisode(sink, dest, timer, contactDuration);
      contactEpisodeList.add(contactEpisode);
    }
    return contactEpisodeList;
  }
}