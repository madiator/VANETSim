package edu.usc.anrg.vanetsim.manager;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;
/**
 * An abstract class that defines a set of methods to connect
 * to databases.
 * Currently, only {@link MongoManager} is functional.
 * @author Maheswaran Sathiamoorthy
 *
 */
public abstract class DBManager {
  /**
   * Get just a single {@link NodeEntry} for the
   * node whose node id is nid
   * @param nid the node's id
   * @return
   */
  public abstract NodeEntry getOneNodeEntry(int nid);
  /**
   * Get the neighbors of a node specified using NodeEntry node,
   * at time t and within specified range (in meters)
   * @param node
   * @param range
   * @param t
   * @return
   */
  public abstract List<NodeEntry> getNeighbors(NodeEntry node,
      double range, int t);
  /**
   * Get the neighbors of a node specified using NodeEntry node,
   * between times t1 and t2 and within specified range (in meters)
   * @param node
   * @param range
   * @param t1
   * @param t2
   * @return
   */
  public abstract List<NodeEntry> getNeighbors(NodeEntry node,
      double range, int t1, int t2);
  /**
   * Get the first datetime.
   * This assumes that there is an entry in your mongodb collection
   * similar to this:
   * { "_id" : ObjectId("5100801...3"),
   * "endingtime" : ISODate("2010-11-02T22:08:20Z"),
   * "startingseconds" : 0,
   * "startingtime" : ISODate("2010-11-01T16:06:37Z") }
   * @return Return the first DateTime if it exists otherwise null.
   */
  public abstract DateTime getFirstDateTime();
  /**
   * Get the last datetime.
   * This assumes that there is an entry in your mongodb collection
   * similar to this:
   * { "_id" : ObjectId("5100801...3"),
   * "endingtime" : ISODate("2010-11-02T22:08:20Z"),
   * "startingseconds" : 0,
   * "startingtime" : ISODate("2010-11-01T16:06:37Z") }
   * @return Return the last DateTime if it exists otherwise null.
   */
  public abstract DateTime getLastDateTime();
  /**
   * Get a list of all the nodes
   * @return
   */
  public abstract List<Integer> getAllNodesList();
  /**
   * Get the NodeEntry of node with id nid between times t1 and t2
   * Useful for unnormalized data.
   * @param nid
   * @param t1
   * @param t2
   * @return
   */
  public abstract NodeEntry getNodeEntry(int nid, int t1, int t2);
  /**
   * Get the NodeEntry of node with id nid at time t
   * @param nid
   * @param t
   * @return
   */
  public abstract NodeEntry getNodeEntry(int nid, int t);
  /**
   * Get all the NodeEntries of node with id nid.
   * @param nid
   * @return
   */
  public abstract List<NodeEntry> getNodeEntries(int nid);
  /**
   * Return a list of nodeentries between times t1 and t2.
   * @param t1
   * @param t2
   * @return
   */
  public abstract List<NodeEntry> getNodeEntriesByTimeAsList(int t1, int t2);
  public abstract HashMap<Integer, NodeEntry> getNodeEntriesByTimeAsHashMap(
      int t, HashMap<Integer, NodeEntry> locations);
  public abstract List<NodeEntry> getNodeEntriesByTimeAsList(int t);
  /**
   * This relies on having pre-computed contacts
   * between nodes stored offline.
   * @param timer
   * @return
   */
  public abstract List<ContactEpisode> getEasyContactEpisodes(int timer);
}
