/**
 *
 */
package edu.usc.anrg.vanetsim.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class LocationManagerDB extends LocationManager{
  private final Configuration configuration;
  private final DBManager dbManager;
  private boolean isNormalized = false;
  private final HashMap<Integer, NodeEntry> locations;

  public LocationManagerDB(Configuration config, boolean isNormalized) {
    this.configuration = config;
    dbManager = Application.getDBManager();
    this.range = configuration.getRange();
    this.earthRadius = configuration.getEarthRadius();
    this.isNormalized = isNormalized;
    this.locations = new HashMap<Integer, NodeEntry>();
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, TimeManager t) {
    return getNeighbors(node, range, t);
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node,
      double newRange, TimeManager t) {
    if(isNormalized)
      return dbManager.getNeighbors(node, newRange, t.getSeconds());
    else
      return dbManager.getNeighbors(node, newRange, t.getSeconds(),
          t.getNextSeconds());
  }

  @Override
  public NodeEntry getNodeEntry(int nodeID, TimeManager t) {
    if(isNormalized)
      return dbManager.getNodeEntry(nodeID, t.getSeconds());
    else
    return dbManager.getNodeEntry(nodeID, t.getSeconds(), t.getNextSeconds());
  }

  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(TimeManager t) {
    if(isNormalized)
      return dbManager.getNodeEntriesByTimeAsList(t.getSeconds());
    else
    return dbManager.getNodeEntriesByTimeAsList(t.getSeconds(), t.getNextSeconds());
  }


  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t) {
    if(isNormalized)
      return dbManager.getNodeEntriesByTimeAsList(t);
    else
      return dbManager.getNodeEntriesByTimeAsList(t, t + Application.getTimeManager().getInterval());
  }

  @Override
  public HashMap<Integer, NodeEntry> getNodeEntriesByTimeMap(int t) {
    if(isNormalized) {
      dbManager.getNodeEntriesByTimeAsHashMap(t, locations);
      return locations;
    }
    else
      return null; //TODO
  }



//  public HashMap<Integer, NodeEntry> getNodeEntries(Time)

  @Override
  public ContactEpisode getContactEpisode(NodeEntry sinkNodeCurrent,
                              NodeEntry sinkNodeNext,
                              NodeEntry destNodeCurrent,
                              NodeEntry destNodeNext,
                              int tCurrent, int tNext) {

    LonLat pos11 = sinkNodeCurrent.getLonLat();
    LonLat pos12 = sinkNodeNext.getLonLat();
    LonLat pos21 = destNodeCurrent.getLonLat();
    LonLat pos22 = destNodeNext.getLonLat();

    ContactEpisode contactEpisode =
    intersectionTime(pos11.getCartesianPosition(),
                      pos12.getCartesianPosition(),
                      pos21.getCartesianPosition(),
                      pos22.getCartesianPosition(),
                      range,
                      tCurrent, tNext);

    contactEpisode.setNeighbor(destNodeCurrent);

    return contactEpisode;
  }

  @Override
  public List<ContactEpisode> getContactEpisodes(NodeEntry node, NodeEntry nodeNext, TimeManager timer) {

    /*
     * nid, time, neighbors:{destNodeID, distance, contact duration}
     * First find its location in the current slot and next slot
     * then its neighbors within 2km range in the current slot
     * for each of these, find the locations in the current and next slot.
     *
     */

    // Find the possible neighbors
    LonLat pos11 = node.getLonLat();
    LonLat pos12 = nodeNext.getLonLat();
    List<NodeEntry> neighbors = dbManager.getNeighbors(node, getBigRange(), timer.getSeconds());
    List<ContactEpisode> contactEpisodeList = new ArrayList<ContactEpisode>();
    for(NodeEntry neighNode:neighbors) {
      // find its location in the next slot
      NodeEntry neighNodeNext = dbManager.getNodeEntry(neighNode.getNodeID(), timer.getNextSeconds());
      if(neighNodeNext==null) {
        neighNodeNext = new NodeEntry(neighNode.getNodeID(), neighNode.getLonLat(), timer.getNextNextSeconds());
      }
      LonLat pos21 = neighNode.getLonLat();
      LonLat pos22 = neighNodeNext.getLonLat();

      ContactEpisode contactEpisode =  intersectionTime(pos11.getCartesianPosition(),
                        pos12.getCartesianPosition(),
                        pos21.getCartesianPosition(),
                        pos22.getCartesianPosition(),
                        range,
                        timer.getSeconds(), timer.getNextSeconds());
      if(contactEpisode.getContactDuration() > 0) {
        contactEpisode.setNeighbor(neighNode);
        contactEpisodeList.add(contactEpisode);
      }
    }
    return contactEpisodeList;
  }

  @Override
  public List<NodeEntry> getNodeEntriesByNodeID(int nodeID) {
    return dbManager.getNodeEntries(nodeID);
  }
}
