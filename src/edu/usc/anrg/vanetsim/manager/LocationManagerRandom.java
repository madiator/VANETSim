/**
 *
 */
package edu.usc.anrg.vanetsim.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * May be incomplete, but the idea is that there can be a
 * LocationManager that you can depend on for random walk
 * rather than from the datasets.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class LocationManagerRandom extends LocationManager {
  NodeManager nodeManager;

  public LocationManagerRandom(NodeManager nodeManager) {
    this.nodeManager = nodeManager;
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, TimeManager t) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public NodeEntry getNodeEntry(int nodeID, TimeManager t) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(TimeManager t) {
    //select n random nodes to return
    int n = nodeManager.getNumNodes()>10?10:nodeManager.getNumNodes();
    List<NodeEntry> nodeEntryList = new ArrayList<NodeEntry>();
    for(int i = 0; i < n; i++) {
      nodeEntryList.add(new NodeEntry(nodeManager.getRandomNodeID(), null, t.getSeconds()));
    }
    return nodeEntryList;
  }

  @Override
  public List<ContactEpisode> getContactEpisodes(NodeEntry node,
      NodeEntry nodeNext, TimeManager timer) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HashMap<Integer, NodeEntry> getNodeEntriesByTimeMap(int t) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.LocationManager#getNeighbors(edu.usc.anrg.vsim.discrete.support.NodeEntry, double, edu.usc.anrg.vsim.discrete.manager.TimeManager)
   */
  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, double newRange,
      TimeManager t) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.LocationManager#getContactEpisode(edu.usc.anrg.vsim.discrete.support.NodeEntry, edu.usc.anrg.vsim.discrete.support.NodeEntry, edu.usc.anrg.vsim.discrete.support.NodeEntry, edu.usc.anrg.vsim.discrete.support.NodeEntry, int, int)
   */
  @Override
  public ContactEpisode getContactEpisode(NodeEntry sinkNodeCurrent,
      NodeEntry sinkNodeNext, NodeEntry destNodeCurrent,
      NodeEntry destNodeNext, int tCurrent, int tNext) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.LocationManager#getNodeEntriesByTimeAsList(int)
   */
  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.LocationManager#getNodeEntriesByNodeID(int)
   */
  @Override
  public List<NodeEntry> getNodeEntriesByNodeID(int nodeID) {
    // TODO Auto-generated method stub
    return null;
  }

}
