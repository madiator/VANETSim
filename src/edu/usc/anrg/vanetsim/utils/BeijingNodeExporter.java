package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;
import java.util.Iterator;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Store the list of nodes
 * @author mahesh
 *
 */
public class BeijingNodeExporter {
  public static void main(String args[]) throws UnknownHostException, MongoException {
    Mongo m = new Mongo();
    DB db = m.getDB( "chicagobus" );

    DBCollection coll = db.getCollection("nodes");
    BasicDBObject query = new BasicDBObject();
    query.put("distinct", "locations");
    query.put("key", "nid");
    CommandResult cmdResult = db.command(query);
    //List<Integer> nodeList = new ArrayList<Integer>();
    BasicDBList r = (BasicDBList) cmdResult.get("values");
    Iterator<Object> iterator = r.iterator();
    while(iterator.hasNext()) {
      int nodeID = (Integer) iterator.next();
      BasicDBObject  doc = new BasicDBObject();
      doc.put("nid", nodeID);
      coll.insert(doc);
    }
  }
}
